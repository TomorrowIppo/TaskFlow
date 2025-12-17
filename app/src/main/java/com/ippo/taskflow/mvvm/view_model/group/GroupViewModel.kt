package com.ippo.taskflow.mvvm.view_model.group

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldPath // ✅ [추가] documentId() 조회용
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.ippo.taskflow.mvvm.model.Group
import com.ippo.taskflow.mvvm.model.User
import com.ippo.taskflow.mvvm.view_model.auth.AuthViewModel
import com.ippo.taskflow.mvvm.view_model.task.TaskViewModel
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

    private val _currentGroup = MutableStateFlow<Group?>(null)
    val currentGroup: StateFlow<Group?> = _currentGroup

    private val _currentGroupMembers = MutableStateFlow<List<User>>(emptyList())
    val currentGroupMembers: StateFlow<List<User>> = _currentGroupMembers

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _groupCreationSuccess = MutableStateFlow(false)
    val groupCreationSuccess: StateFlow<Boolean> = _groupCreationSuccess

    private val _createdGroupId = MutableStateFlow<String?>(null)
    val createdGroupId: StateFlow<String?> = _createdGroupId

    private var groupDetailListener: ListenerRegistration? = null
    private var groupMembersListener: ListenerRegistration? = null

    private val currentUserId: String?
        get() = auth.currentUser?.uid

    override fun onCleared() {
        super.onCleared()
        groupDetailListener?.remove()
        groupMembersListener?.remove()
    }

    fun resetGroupCreationStatus() {
        _groupCreationSuccess.value = false
        _createdGroupId.value = null
    }

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
                        document.toObject(Group::class.java)?.copy(groupId = document.id)
                    }
                    _groupList.value = groups
                }
            }
    }

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

                if (group != null) {
                    loadGroupMembers(group.memberUids)
                } else {
                    _currentGroupMembers.value = emptyList()
                }
            }
    }

    private fun loadGroupMembers(memberUids: List<String>) {
        groupMembersListener?.remove()
        if (memberUids.isEmpty()) {
            _currentGroupMembers.value = emptyList()
            return
        }

        groupMembersListener = db.collection("users")
            .whereIn(FieldPath.documentId(), memberUids) // ✅ [수정] uid 필드가 아닌 문서ID 기준 조회
            .addSnapshotListener { snapshot, e ->
                if (e != null || snapshot == null) return@addSnapshotListener
                _currentGroupMembers.value = snapshot.toObjects(User::class.java)
            }
    }

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
        _groupCreationSuccess.value = false
        _createdGroupId.value = null

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
            .addOnSuccessListener { docRef ->
                _isLoading.value = false
                _createdGroupId.value = docRef.id
                _groupCreationSuccess.value = true
            }
            .addOnFailureListener { e ->
                _isLoading.value = false
                _error.value = "그룹 생성에 실패했습니다: ${e.message}"
            }
    }

    fun deleteGroup(groupId: String) {
        if (groupId.isBlank()) {
            _error.value = "그룹 ID가 유효하지 않습니다."
            return
        }

        taskViewModel.deleteAllTasksInGroup(groupId)

        _isLoading.value = true
        _error.value = null

        db.collection("groups").document(groupId)
            .delete()
            .addOnSuccessListener { _isLoading.value = false }
            .addOnFailureListener { e ->
                _isLoading.value = false
                _error.value = "그룹 삭제 실패: ${e.message}"
            }
    }

    fun inviteMemberByEmail(groupId: String, email: String) {
        if (groupId.isBlank()) {
            _error.value = "그룹 ID가 유효하지 않습니다."
            return
        }

        _isLoading.value = true
        _error.value = null

        authViewModel.getUidByEmail(email) { memberUid ->
            if (memberUid != null) {
                addMember(groupId, memberUid)
            } else {
                _isLoading.value = false
                _error.value = "오류: 해당 이메일의 사용자를 찾을 수 없습니다."
            }
        }
    }

    fun addMember(groupId: String, memberId: String) {
        if (groupId.isBlank() || memberId.isBlank()) {
            _error.value = "그룹 ID 또는 멤버 ID가 유효하지 않습니다."
            return
        }

        db.collection("groups").document(groupId)
            .update("memberUids", FieldValue.arrayUnion(memberId))
            .addOnSuccessListener { _isLoading.value = false }
            .addOnFailureListener { e ->
                _isLoading.value = false
                _error.value = "멤버 추가 실패: ${e.message}"
            }
    }

    fun deleteMember(groupId: String, memberId: String) {
        if (groupId.isBlank() || memberId.isBlank()) {
            _error.value = "그룹 ID 또는 멤버 ID가 유효하지 않습니다."
            return
        }

        _isLoading.value = true
        _error.value = null

        db.collection("groups").document(groupId)
            .update("memberUids", FieldValue.arrayRemove(memberId))
            .addOnSuccessListener { _isLoading.value = false }
            .addOnFailureListener { e ->
                _isLoading.value = false
                _error.value = "멤버 삭제 실패: ${e.message}"
            }
    }
}
