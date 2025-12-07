package com.ippo.taskflow.mvvm.view_model.group

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.ippo.taskflow.mvvm.model.Group
import com.ippo.taskflow.mvvm.model.User // User 모델 임포트
import com.ippo.taskflow.view_model.task.TaskViewModel
import com.ippo.taskflow.view_model.auth.AuthViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.Date

class GroupViewModel(
    private val authViewModel: AuthViewModel,
    private val taskViewModel: TaskViewModel
) : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _groupList = MutableStateFlow<List<Group>>(emptyList())
    val groupList: StateFlow<List<Group>> = _groupList

    // ✅ 추가: 현재 선택된 그룹의 상세 정보
    private val _currentGroup = MutableStateFlow<Group?>(null)
    val currentGroup: StateFlow<Group?> = _currentGroup

    // ✅ 추가: 현재 그룹의 멤버 프로필 목록
    private val _currentGroupMembers = MutableStateFlow<List<User>>(emptyList())
    val currentGroupMembers: StateFlow<List<User>> = _currentGroupMembers

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private var groupDetailListener: ListenerRegistration? = null
    private var groupMembersListener: ListenerRegistration? = null

    private val currentUserId: String?
        get() = auth.currentUser?.uid

    override fun onCleared() {
        super.onCleared()
        groupDetailListener?.remove()
        groupMembersListener?.remove()
    }

    /**
     * 현재 사용자가 멤버로 포함된 그룹 목록을 실시간으로 불러옵니다.
     */
    fun loadGroups() {
        val userId = currentUserId ?: return
        _isLoading.value = true

        db.collection("groups")
            .whereArrayContains("memberUids", userId)
            .addSnapshotListener { snapshot, e ->
                _isLoading.value = false
                if (e != null) {
                    _error.value = "그룹 목록 로드 실패: ${e.message}"
                    _groupList.value = emptyList()
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val groups = snapshot.documents.mapNotNull { document ->
                        // DocumentId를 수동으로 설정하여 Group 객체 생성
                        document.toObject(Group::class.java)?.copy(groupId = document.id)
                    }
                    _groupList.value = groups
                }
            }
    }

    /**
     * ✅ 추가: 단일 그룹의 상세 정보를 실시간으로 로드합니다.
     */
    fun loadGroupDetail(groupId: String) {
        groupDetailListener?.remove()
        if (groupId.isBlank()) return

        groupDetailListener = db.collection("groups").document(groupId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    _error.value = "그룹 상세 정보 로드 실패: ${e.message}"
                    _currentGroup.value = null
                    return@addSnapshotListener
                }

                val group = snapshot?.toObject(Group::class.java)?.copy(groupId = snapshot.id)
                _currentGroup.value = group

                // 그룹 정보가 로드되면 멤버 정보도 로드 시작
                if (group != null) {
                    loadGroupMembers(group.memberUids)
                } else {
                    _currentGroupMembers.value = emptyList()
                }
            }
    }

    /**
     * ✅ 추가: 그룹 멤버들의 프로필 정보를 로드합니다.
     */
    private fun loadGroupMembers(memberUids: List<String>) {
        groupMembersListener?.remove()
        if (memberUids.isEmpty()) {
            _currentGroupMembers.value = emptyList()
            return
        }

        // Firestore in 쿼리는 최대 10개 제한이 있으므로, 더 복잡한 로직이 필요할 수 있으나,
        // 여기서는 최대 10명 이내로 가정하고 in 쿼리를 사용합니다.
        groupMembersListener = db.collection("users")
            .whereIn("uid", memberUids)
            .addSnapshotListener { snapshot, e ->
                if (e != null || snapshot == null) return@addSnapshotListener

                val members = snapshot.toObjects(User::class.java)
                _currentGroupMembers.value = members
            }
    }

    /**
     * 새로운 그룹을 생성합니다.
     */
    fun createGroup(name: String, description: String) {
        val userId = currentUserId ?: run {
            _error.value = "로그인이 필요합니다."
            return
        }

        if (name.isBlank()) {
            _error.value = "그룹 이름을 입력해주세요."
            return
        }

        _isLoading.value = true
        _error.value = null

        val newGroup = Group(
            name = name,
            description = description,
            ownerId = userId,
            memberUids = listOf(userId),
            isPrivate = true,
            createdAt = Date()
        )

        db.collection("groups")
            .add(newGroup)
            .addOnSuccessListener {
                _isLoading.value = false
            }
            .addOnFailureListener { e ->
                _isLoading.value = false
                _error.value = "그룹 생성에 실패했습니다: ${e.message}"
            }
    }

    /**
     * 그룹 삭제 함수 (Task 종속 삭제 호출)
     */
    fun deleteGroup(groupId: String) {
        if (groupId.isBlank()) {
            _error.value = "그룹 ID가 유효하지 않습니다."
            return
        }

        // 🚨 1. TaskViewModel에게 해당 그룹의 모든 Task 삭제를 명령합니다. (Cascading Delete)
        taskViewModel.deleteAllTasksInGroup(groupId)

        // 2. 그룹 문서 삭제 실행
        _isLoading.value = true
        _error.value = null

        db.collection("groups").document(groupId)
            .delete()
            .addOnSuccessListener {
                _isLoading.value = false
            }
            .addOnFailureListener { e ->
                _isLoading.value = false
                _error.value = "그룹 삭제 실패: ${e.message}"
            }
    }

    /**
     * 이메일을 UID로 검색 후 그룹에 추가를 요청합니다.
     */
    fun inviteMemberByEmail(groupId: String, email: String) {
        if (groupId.isBlank()) {
            _error.value = "그룹 ID가 유효하지 않습니다."
            return
        }

        _isLoading.value = true
        _error.value = null

        // 1. AuthViewModel을 통해 UID 검색 요청
        authViewModel.getUidByEmail(email) { memberUid ->
            if (memberUid != null) {
                // 2. UID 검색 성공 시, 멤버 추가 실행
                addMember(groupId, memberUid)
            } else {
                // 3. 사용자 검색 실패 시
                _isLoading.value = false
                _error.value = "오류: 해당 이메일의 사용자를 찾을 수 없습니다."
            }
        }
    }

    /**
     * 멤버 UID를 배열에 추가합니다.
     */
    fun addMember(groupId: String, memberId: String) {
        if (groupId.isBlank() || memberId.isBlank()) {
            _error.value = "그룹 ID 또는 멤버 ID가 유효하지 않습니다."
            return
        }

        // 💡 isLoading은 inviteMemberByEmail 또는 deleteMember에서 설정되므로 여기서 다시 설정할 필요는 없습니다.
        db.collection("groups").document(groupId)
            .update("memberUids", FieldValue.arrayUnion(memberId))
            .addOnSuccessListener {
                _isLoading.value = false
            }
            .addOnFailureListener { e ->
                _isLoading.value = false
                _error.value = "멤버 추가 실패: ${e.message}"
            }
    }

    /**
     * 멤버 UID를 배열에서 제거합니다.
     */
    fun deleteMember(groupId: String, memberId: String) {
        if (groupId.isBlank() || memberId.isBlank()) {
            _error.value = "그룹 ID 또는 멤버 ID가 유효하지 않습니다."
            return
        }

        _isLoading.value = true
        _error.value = null

        db.collection("groups").document(groupId)
            .update("memberUids", FieldValue.arrayRemove(memberId))
            .addOnSuccessListener {
                _isLoading.value = false
            }
            .addOnFailureListener { e ->
                _isLoading.value = false
                _error.value = "멤버 삭제 실패: ${e.message}"
            }
    }
}