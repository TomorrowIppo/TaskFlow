package com.ippo.taskflow.mvvm.view_model.task

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.WriteBatch
import com.ippo.taskflow.mvvm.model.Task
import com.ippo.taskflow.mvvm.model.TaskStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Date

data class TaskMetrics(
    val total: Int = 0,
    val todo: Int = 0,
    val inProgress: Int = 0,
    val done: Int = 0,
    val blocked: Int = 0
)

enum class TaskFilter(val status: String?) {
    ALL(null),
    TODO("TODO"),
    IN_PROGRESS("IN_PROGRESS"),
    DONE("DONE"),
    BLOCKED("BLOCKED")
}

class TaskViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val taskCollection = db.collection("tasks")

    private val _taskList = MutableStateFlow<List<Task>>(emptyList())
    val taskList: StateFlow<List<Task>> = _taskList

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _taskMetrics = MutableStateFlow(TaskMetrics())
    val taskMetrics: StateFlow<TaskMetrics> = _taskMetrics

    private val _currentFilter = MutableStateFlow(TaskFilter.ALL)
    val currentFilter: StateFlow<TaskFilter> = _currentFilter

    private val _completionPercentage = MutableStateFlow(0)
    val completionPercentage: StateFlow<Int> = _completionPercentage


    private var taskListener: ListenerRegistration? = null
    private var metricsListener: ListenerRegistration? = null

    override fun onCleared() {
        super.onCleared()
        taskListener?.remove()
        metricsListener?.remove()
    }

    fun clearError() {
        _error.value = null
    }

    // C: Create Task
    fun createTask(
        groupId: String,
        title: String,
        assignedToUid: String,
        dueDate: Date?,
        priority: Int,
        precursorTaskId: String? = null
    ) {
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
            status = if (precursorTaskId != null) TaskStatus.TODO else TaskStatus.BLOCKED,
            priority = priority,
            dueDate = dueDate,
            precursorTaskId = precursorTaskId,
            createdAt = Date()
        )

        newDocRef.set(task)
            .addOnSuccessListener { _isLoading.value = false }
            .addOnFailureListener { e ->
                _isLoading.value = false
                _error.value = "작업 생성 실패: ${e.message}"
            }
    }

    /**
     * R: Read/Load Tasks (특정 Group ID로 필터링된 목록을 실시간으로 가져옵니다. Priority 정렬 적용)
     */
    fun loadTasks(groupId: String?) {
        _isLoading.value = true
        _error.value = null
        taskListener?.remove()

        if (groupId == null || groupId.isBlank()) {
            _taskList.value = emptyList()
            _isLoading.value = false
            _completionPercentage.value = 0
            return
        }

        taskListener = taskCollection
            .whereEqualTo("groupId", groupId)
            .orderBy("priority", com.google.firebase.firestore.Query.Direction.ASCENDING)
            .orderBy("createdAt")
            .addSnapshotListener { snapshot, e ->
                _isLoading.value = false
                if (e != null) {
                    _error.value = "작업 목록 로드 실패: ${e.message}"
                    _taskList.value = emptyList()
                    _completionPercentage.value = 0
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val allTasks = snapshot.toObjects(Task::class.java)
                    _taskList.value = allTasks
                    calculateCompletionPercentage(allTasks)
                } else {
                    _taskList.value = emptyList()
                    _completionPercentage.value = 0
                }
            }
        loadTaskMetrics(groupId)
    }

    private fun calculateCompletionPercentage(tasks: List<Task>) {
        val totalTasks = tasks.size
        val completedTasks = tasks.count { it.status == TaskStatus.DONE }

        val completionRatio = if (totalTasks == 0) 0f else completedTasks.toFloat() / totalTasks.toFloat()
        val percentage = (completionRatio * 100).toInt()

        _completionPercentage.value = percentage
    }


    fun loadTaskMetrics(groupId: String) {
        metricsListener?.remove()

        metricsListener = taskCollection
            .whereEqualTo("groupId", groupId)
            .addSnapshotListener { snapshot, e ->
                if (e != null || snapshot == null) return@addSnapshotListener

                val tasks = snapshot.toObjects(Task::class.java)

                val metrics = TaskMetrics(
                    total = tasks.size,
                    todo = tasks.count { it.status == TaskStatus.TODO },
                    inProgress = tasks.count { it.status == TaskStatus.IN_PROGRESS },
                    done = tasks.count { it.status == TaskStatus.DONE },
                    blocked = tasks.count { it.status == TaskStatus.BLOCKED }
                )
                _taskMetrics.value = metrics
            }
    }

    // U: Update Task Status
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

    fun updateTask(task: Task) {
        _isLoading.value = true
        _error.value = null

        taskCollection.document(task.taskId).set(task)
            .addOnSuccessListener { _isLoading.value = false }
            .addOnFailureListener { e ->
                _isLoading.value = false
                _error.value = "작업 업데이트 실패: ${e.message}"
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

    fun setTaskFilter(filter: TaskFilter) {
        _currentFilter.value = filter
    }
}