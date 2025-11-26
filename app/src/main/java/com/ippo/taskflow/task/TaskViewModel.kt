// com.ippo.taskflow.task/TaskViewModel.kt (핵심 함수 재점검)
package com.ippo.taskflow.task


import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.ippo.taskflow.data.Task // 🚨 [수정] Task 모델 Import (기존 TaskModel 대신)
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.Date

class TaskViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val taskCollection = db.collection("tasks")

    private val _taskList = MutableStateFlow<List<Task>>(emptyList()) // 🚨 [수정] List<Task>
    val taskList: StateFlow<List<Task>> = _taskList

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private var taskListener: ListenerRegistration? = null

    init {
        loadTasks()
    }

    override fun onCleared() {
        super.onCleared()
        taskListener?.remove()
    }

    // C: Create Task (작업 생성)
    fun createTask(groupId: String, title: String, assignedToUid: String, dueDate: Date?, priority: Int) {
        if (groupId.isBlank() || title.isBlank()) {
            _error.value = "그룹 ID와 제목은 필수입니다."
            return
        }
        _isLoading.value = true
        _error.value = null

        val newDocRef = taskCollection.document()
        val taskId = newDocRef.id
        val task = Task( // 🚨 [수정] Task 클래스 사용
            taskId = taskId,
            groupId = groupId,
            title = title,
            assignedToUid = assignedToUid,
            status = "TODO", // 기본 상태
            priority = priority, // 우선순위 사용
            dueDate = dueDate
            // description, createdByUid 등은 임시로 생략 가능
        )

        newDocRef.set(task)
            .addOnSuccessListener { _isLoading.value = false }
            .addOnFailureListener { e ->
                _isLoading.value = false
                _error.value = "작업 생성 실패: ${e.message}"
            }
    }

    // R: Read/Load Tasks (Snapshot Listener 사용)
    fun loadTasks(groupId: String? = null) {
        _isLoading.value = true
        _error.value = null
        taskListener?.remove()

        var query = taskCollection.orderBy("createdAt")

        if (groupId != null && groupId.isNotBlank()) {
            query = query.whereEqualTo("groupId", groupId)
        }

        taskListener = query.addSnapshotListener { snapshot, e ->
            _isLoading.value = false
            if (e != null) {
                _error.value = "작업 목록 로드 실패: ${e.message}"
                _taskList.value = emptyList()
                return@addSnapshotListener
            }

            if (snapshot != null) {
                val tasks = snapshot.toObjects(Task::class.java) // 🚨 [수정] Task 클래스 사용
                _taskList.value = tasks
            } else {
                _taskList.value = emptyList()
            }
        }
    }

    // U: Update Task (상태 변경)
    fun updateTaskStatus(taskId: String, newStatus: String) {
        if (taskId.isBlank()) return

        val updates = mapOf("status" to newStatus) // 🚨 [수정] isCompleted 대신 status 필드를 사용

        _isLoading.value = true
        _error.value = null

        taskCollection.document(taskId)
            .update(updates)
            .addOnSuccessListener { _isLoading.value = false }
            .addOnFailureListener { e ->
                _isLoading.value = false
                _error.value = "상태 업데이트 실패: ${e.message}"
            }
    }

    // D: Delete Task
    fun deleteTask(taskId: String) {
        // ... (기존 로직 유지) ...
        if (taskId.isBlank()) return

        _isLoading.value = true
        _error.value = null

        taskCollection.document(taskId)
            .delete()
            .addOnSuccessListener { _isLoading.value = false }
            .addOnFailureListener { e ->
                _isLoading.value = false
                _error.value = "작업 삭제 실패: ${e.message}"
            }
    }
}