package com.ippo.taskflow.task

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.WriteBatch
import com.ippo.taskflow.data.Task
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Date

class TaskViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val taskCollection = db.collection("tasks")

    private val _taskList = MutableStateFlow<List<Task>>(emptyList())
    val taskList: StateFlow<List<Task>> = _taskList

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private var taskListener: ListenerRegistration? = null

    init {
        // TestScreen에서 로드 책임을 위임받았으므로 init 블록은 비워둡니다.
    }

    override fun onCleared() {
        super.onCleared()
        taskListener?.remove()
    }

    // C: Create Task
    fun createTask(groupId: String, title: String, assignedToUid: String, dueDate: Date?, priority: Int) {
        if (groupId.isBlank() || title.isBlank()) {
            _error.value = "그룹 ID와 제목은 필수입니다."
            return
        }
        _isLoading.value = true
        _error.value = null

        val newDocRef = taskCollection.document()
        val taskId = newDocRef.id
        val task = Task(
            taskId = taskId,
            groupId = groupId,
            title = title,
            assignedToUid = assignedToUid,
            status = "TODO",
            priority = priority,
            dueDate = dueDate,
            createdAt = Date()
        )

        newDocRef.set(task)
            .addOnSuccessListener { _isLoading.value = false }
            .addOnFailureListener { e ->
                _isLoading.value = false
                _error.value = "작업 생성 실패: ${e.message}"
            }
    }

    // R: Read/Load Tasks (특정 Group ID로 필터링)
    fun loadTasks(groupId: String?) {
        _isLoading.value = true
        _error.value = null
        taskListener?.remove()

        var query = taskCollection.orderBy("createdAt")

        if (groupId != null && groupId.isNotBlank()) {
            query = query.whereEqualTo("groupId", groupId)
        } else {
            _taskList.value = emptyList()
            _isLoading.value = false
            return
        }

        taskListener = query.addSnapshotListener { snapshot, e ->
            _isLoading.value = false
            if (e != null) {
                _error.value = "작업 목록 로드 실패: ${e.message}"
                _taskList.value = emptyList()
                return@addSnapshotListener
            }

            if (snapshot != null) {
                val tasks = snapshot.toObjects(Task::class.java)
                _taskList.value = tasks
            } else {
                _taskList.value = emptyList()
            }
        }
    }

    // U: Update Task (상태 변경)
    fun updateTaskStatus(taskId: String, newStatus: String) {
        if (taskId.isBlank()) return

        val updates = mapOf("status" to newStatus)

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

    /**
     * 🚨 Task 일괄 삭제 기능 (Cascading Delete Executor)
     */
    fun deleteAllTasksInGroup(groupId: String) {
        if (groupId.isBlank()) return

        viewModelScope.launch {
            db.collection("tasks")
                .whereEqualTo("groupId", groupId)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    val batch: WriteBatch = db.batch()
                    for (document in querySnapshot.documents) {
                        batch.delete(document.reference)
                    }
                    batch.commit()
                        .addOnFailureListener { e ->
                            _error.value = "Task 일괄 삭제 실패 (Commit): ${e.message}"
                        }
                }
                .addOnFailureListener { e ->
                    _error.value = "Task 쿼리 실패: ${e.message}"
                }
        }
    }
}