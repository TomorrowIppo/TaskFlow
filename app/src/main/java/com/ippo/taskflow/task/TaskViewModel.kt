package com.ippo.taskflow.task

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.ippo.taskflow.data.Task // 💡 Task 모델 Import
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Date

class TaskViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // 💡 1. 상태: 태스크 목록 (현재 선택된 그룹 기준)
    private val _taskList = MutableStateFlow<List<Task>>(emptyList())
    val taskList: StateFlow<List<Task>> = _taskList

    // 💡 2. 상태: 로딩 및 에러
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val currentUserId: String?
        get() = auth.currentUser?.uid

    /**
     * 특정 그룹 ID에 해당하는 태스크 목록을 실시간으로 불러옵니다.
     * (TaskScreen에서 그룹 선택 시 호출됨)
     */
    fun loadTasks(groupId: String) {
        if (groupId.isBlank()) return

        _isLoading.value = true

        db.collection("tasks")
            .whereEqualTo("groupId", groupId) // 💡 특정 그룹의 태스크만 필터링
            .addSnapshotListener { snapshot, e ->
                _isLoading.value = false
                if (e != null) {
                    _error.value = "태스크 목록 로드 실패: ${e.message}"
                    _taskList.value = emptyList()
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val tasks = snapshot.documents.mapNotNull { document ->
                        document.toObject(Task::class.java)
                    }
                    _taskList.value = tasks.sortedByDescending { it.priority } // 우선순위별 정렬
                }
            }
    }

    /**
     * 특정 그룹에 새로운 태스크를 생성합니다.
     */
    fun createTask(groupId: String, title: String, assignedToUid: String, dueDate: Date?) {
        val userId = currentUserId ?: run {
            _error.value = "로그인이 필요합니다."
            return
        }

        if (title.isBlank() || groupId.isBlank()) {
            _error.value = "제목과 그룹 ID는 필수입니다."
            return
        }

        _isLoading.value = true
        _error.value = null

        val newTask = Task(
            groupId = groupId,
            title = title,
            assignedToUid = assignedToUid,
            createdByUid = userId,
            dueDate = dueDate,
            status = "TODO", // 기본 상태
            priority = 3 // 기본 우선순위
        )

        db.collection("tasks")
            .add(newTask)
            .addOnSuccessListener {
                _isLoading.value = false
            }
            .addOnFailureListener { e ->
                _isLoading.value = false
                _error.value = "태스크 생성에 실패했습니다: ${e.message}"
            }
    }

    /**
     * 태스크 상태를 변경합니다.
     */
    fun updateTaskStatus(taskId: String, newStatus: String) {
        if (taskId.isBlank()) return

        db.collection("tasks").document(taskId)
            .update("status", newStatus)
            .addOnFailureListener { e ->
                _error.value = "상태 업데이트 실패: ${e.message}"
            }
    }
}