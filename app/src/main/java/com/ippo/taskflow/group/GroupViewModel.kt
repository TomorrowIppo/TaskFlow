package com.ippo.taskflow.group

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.ippo.taskflow.data.Group // 💡 Group 모델 Import
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class GroupViewModel : ViewModel() {

    // Firestore 및 Auth 인스턴스
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // 💡 1. 상태: 그룹 목록
    private val _groupList = MutableStateFlow<List<Group>>(emptyList())
    val groupList: StateFlow<List<Group>> = _groupList

    // 💡 2. 상태: 로딩 및 에러
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    // 현재 로그인된 사용자 UID
    private val currentUserId: String?
        get() = auth.currentUser?.uid

    /**
     * 현재 사용자가 멤버로 포함된 그룹 목록을 실시간으로 불러옵니다.
     * memberUids 배열에 현재 사용자 UID가 포함된 그룹을 쿼리합니다.
     */
    fun loadGroups() {
        val userId = currentUserId ?: return
        _isLoading.value = true

        db.collection("groups")
            .whereArrayContains("memberUids", userId) // 💡 현재 사용자 UID가 멤버 목록에 포함된 그룹만 필터링
            .addSnapshotListener { snapshot, e ->
                _isLoading.value = false
                if (e != null) {
                    _error.value = "그룹 목록 로드 실패: ${e.message}"
                    _groupList.value = emptyList()
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    // Firestore 문서를 Group 데이터 클래스로 변환
                    val groups = snapshot.documents.mapNotNull { document ->
                        document.toObject(Group::class.java)
                    }
                    _groupList.value = groups
                }
            }
    }

    /**
     * 새로운 그룹을 생성하고 현재 사용자를 owner 및 첫 번째 멤버로 추가합니다.
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
            memberUids = listOf(userId), // 생성자가 첫 멤버
            isPrivate = true
            // createdAt은 Firestore에서 자동으로 채워짐
        )

        // Firestore에 새 그룹 추가
        db.collection("groups")
            .add(newGroup)
            .addOnSuccessListener {
                _isLoading.value = false
                // 성공 시 자동으로 loadGroups의 SnapshotListener가 groupList를 업데이트함
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

        _isLoading.value = true
        _error.value = null

        db.collection("groups").document(groupId)
            .delete()
            .addOnSuccessListener {
                _isLoading.value = false
                // 💡 삭제 성공 후, SnapshotListener가 자동으로 groupList를 업데이트합니다.
                // Log.d("GroupTest", "SUCCESS: 그룹 삭제 완료.")
            }
            .addOnFailureListener { e ->
                _isLoading.value = false
                _error.value = "그룹 삭제 실패: ${e.message}"
            }
    }

    // TODO: addMember(groupId: String, email: String) 구현 (가장 복잡한 로직)
    // [GroupViewModel.kt] 파일 내부에 추가

// 🚨 필요한 import가 없다면 추가해 주세요
// import com.google.firebase.firestore.FieldValue

    // 🚨🚨 수정된 함수 1: addMember (필드명 memberUids로 수정)
    fun addMember(groupId: String, memberId: String) {
        if (groupId.isBlank() || memberId.isBlank()) {
            _error.value = "그룹 ID 또는 멤버 ID가 유효하지 않습니다."
            return
        }

        _isLoading.value = true
        _error.value = null

        db.collection("groups").document(groupId)
            // 🚨 필드 이름을 'memberUids'로 수정
            .update("memberUids", FieldValue.arrayUnion(memberId))
            .addOnSuccessListener {
                _isLoading.value = false
            }
            .addOnFailureListener { e ->
                _isLoading.value = false
                _error.value = "멤버 추가 실패: ${e.message}"
            }
    }

    // 🚨🚨 수정된 함수 2: deleteMember (필드명 memberUids로 수정)
    fun deleteMember(groupId: String, memberId: String) {
        if (groupId.isBlank() || memberId.isBlank()) {
            _error.value = "그룹 ID 또는 멤버 ID가 유효하지 않습니다."
            return
        }

        _isLoading.value = true
        _error.value = null

        db.collection("groups").document(groupId)
            // 🚨 필드 이름을 'memberUids'로 수정
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