package com.ippo.taskflow.group

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.ippo.taskflow.data.Group
import com.ippo.taskflow.task.TaskViewModel
import com.ippo.taskflow.auth.AuthViewModel // 🚨 AuthViewModel Import
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.Date

// 🚨 GroupViewModel은 AuthViewModel과 TaskViewModel을 모두 인자로 받습니다.
class GroupViewModel(
    private val authViewModel: AuthViewModel, // AuthViewModel 주입 (UID 검색용)
    private val taskViewModel: TaskViewModel  // TaskViewModel 주입 (Task 삭제용)
) : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _groupList = MutableStateFlow<List<Group>>(emptyList())
    val groupList: StateFlow<List<Group>> = _groupList

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val currentUserId: String?
        get() = auth.currentUser?.uid

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
                        document.toObject(Group::class.java)?.copy(groupId = document.id)
                    }
                    _groupList.value = groups
                }
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