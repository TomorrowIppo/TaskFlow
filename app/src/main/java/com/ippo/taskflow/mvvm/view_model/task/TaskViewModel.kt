package com.ippo.taskflow.mvvm.view_model.task

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
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

    private val _selectedTask = MutableStateFlow<Task?>(null)
    val selectedTask: StateFlow<Task?> = _selectedTask

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
    private var taskDetailListener: ListenerRegistration? = null

    // ✅ 그룹별 통계 캐시 (GroupTaskScreen에서 여러 그룹 동시에 표시 가능)
    private val groupMetricsFlows = mutableMapOf<String, MutableStateFlow<TaskMetrics>>()
    private val groupCompletionFlows = mutableMapOf<String, MutableStateFlow<Int>>()
    private val groupMetricsListeners = mutableMapOf<String, ListenerRegistration>()

    override fun onCleared() {
        super.onCleared()
        taskListener?.remove()
        metricsListener?.remove()
        taskDetailListener?.remove()

        // ✅ 그룹별 리스너들 정리
        groupMetricsListeners.values.forEach { it.remove() }
        groupMetricsListeners.clear()
        groupMetricsFlows.clear()
        groupCompletionFlows.clear()
    }

    fun clearError() {
        _error.value = null
    }

    // ✅ 단일 Task 실시간 로드 (EditTaskScreen 안정화 핵심)
    fun loadTaskById(taskId: String) {
        if (taskId.isBlank()) return

        _isLoading.value = true
        _error.value = null

        taskDetailListener?.remove()
        taskDetailListener = taskCollection.document(taskId)
            .addSnapshotListener { snapshot, e ->
                _isLoading.value = false
                if (e != null) {
                    _error.value = "Task 로드 실패: ${e.message}"
                    _selectedTask.value = null
                    return@addSnapshotListener
                }
                _selectedTask.value = snapshot?.toObject(Task::class.java)
            }
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
            // ⚠️ 너가 준 로직 그대로 유지 (precursor 있으면 TODO / 없으면 BLOCKED)
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

    // R: Read/Load Tasks (Priority 정렬 적용)
    fun loadTasks(groupId: String?) {
        _isLoading.value = true
        _error.value = null
        taskListener?.remove()

        if (groupId.isNullOrBlank()) {
            _taskList.value = emptyList()
            _isLoading.value = false
            _completionPercentage.value = 0
            return
        }

        taskListener = taskCollection
            .whereEqualTo("groupId", groupId)
            .orderBy("priority", Query.Direction.ASCENDING)
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

        val completionRatio =
            if (totalTasks == 0) 0f else completedTasks.toFloat() / totalTasks.toFloat()
        _completionPercentage.value = (completionRatio * 100).toInt()
    }

    fun loadTaskMetrics(groupId: String) {
        metricsListener?.remove()

        metricsListener = taskCollection
            .whereEqualTo("groupId", groupId)
            .addSnapshotListener { snapshot, e ->
                if (e != null || snapshot == null) return@addSnapshotListener

                val tasks = snapshot.toObjects(Task::class.java)
                _taskMetrics.value = TaskMetrics(
                    total = tasks.size,
                    todo = tasks.count { it.status == TaskStatus.TODO },
                    inProgress = tasks.count { it.status == TaskStatus.IN_PROGRESS },
                    done = tasks.count { it.status == TaskStatus.DONE },
                    blocked = tasks.count { it.status == TaskStatus.BLOCKED }
                )
            }
    }

    // ✅ 그룹별 통계 구독 (GroupTaskScreen용)
    fun observeGroupMetrics(groupId: String): StateFlow<TaskMetrics> {
        if (groupId.isBlank()) return MutableStateFlow(TaskMetrics())

        val flow = groupMetricsFlows.getOrPut(groupId) { MutableStateFlow(TaskMetrics()) }

        if (!groupMetricsListeners.containsKey(groupId)) {
            val reg = taskCollection
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
                    flow.value = metrics

                    val total = metrics.total
                    val done = metrics.done
                    val percent = if (total == 0) 0 else ((done.toFloat() / total.toFloat()) * 100).toInt()
                    groupCompletionFlows.getOrPut(groupId) { MutableStateFlow(0) }.value = percent
                }

            groupMetricsListeners[groupId] = reg
        }

        return flow
    }

    fun observeGroupCompletionPercentage(groupId: String): StateFlow<Int> {
        if (groupId.isBlank()) return MutableStateFlow(0)
        observeGroupMetrics(groupId) // completion 갱신을 위해 metrics 구독 보장
        return groupCompletionFlows.getOrPut(groupId) { MutableStateFlow(0) }
    }

    fun releaseGroupMetrics(groupId: String) {
        groupMetricsListeners.remove(groupId)?.remove()
        groupMetricsFlows.remove(groupId)
        groupCompletionFlows.remove(groupId)
    }

    // U: Update Task Status
    fun updateTaskStatus(taskId: String, newStatus: String) {
        if (taskId.isBlank()) return

        _isLoading.value = true
        _error.value = null

        taskCollection.document(taskId)
            .update(mapOf("status" to newStatus))
            .addOnSuccessListener { _isLoading.value = false }
            .addOnFailureListener { e ->
                _isLoading.value = false
                _error.value = "상태 업데이트 실패: ${e.message}"
            }
    }

    // ✅ Update Task (Edit 저장)
    fun updateTask(task: Task) {
        _isLoading.value = true
        _error.value = null

        taskCollection.document(task.taskId).set(task)
            .addOnSuccessListener {
                _isLoading.value = false
                _selectedTask.value = task
                _taskList.value = _taskList.value.map { if (it.taskId == task.taskId) task else it }
            }
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

    fun loadMyTasks(uid: String) {
        _isLoading.value = true
        _error.value = null
        taskListener?.remove()

        if (uid.isBlank()) {
            _taskList.value = emptyList()
            _isLoading.value = false
            return
        }

        taskListener = taskCollection
            .whereEqualTo("assignedToUid", uid)
            .orderBy("dueDate") // dueDate null 많은 경우 createdAt으로 바꿔도 됨
            .addSnapshotListener { snapshot, e ->
                _isLoading.value = false

                if (e != null) {
                    _error.value = "내 작업 로드 실패: ${e.message}"
                    _taskList.value = emptyList()
                    return@addSnapshotListener
                }

                val tasks = snapshot?.toObjects(Task::class.java) ?: emptyList()
                _taskList.value = tasks

                // (선택) 프로필에서도 전체 완료율 쓰고 싶으면 유지 가능
                calculateCompletionPercentage(tasks)
            }
    }
}

