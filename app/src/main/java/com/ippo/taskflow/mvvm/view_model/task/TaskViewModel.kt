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

/**
 * 그룹 태스크 목록의 통계 정보를 담는 데이터 클래스
 */
data class TaskMetrics(
    val total: Int = 0,
    val todo: Int = 0,
    val inProgress: Int = 0,
    val done: Int = 0,
    val blocked: Int = 0
)

// 필터링 옵션을 위한 Enum (명시적인 상태 사용)
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

    // Task 목록 (전체 데이터)
    private val _taskList = MutableStateFlow<List<Task>>(emptyList())
    val taskList: StateFlow<List<Task>> = _taskList

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    // ✅ 추가: 그룹 태스크 통계
    private val _taskMetrics = MutableStateFlow(TaskMetrics())
    val taskMetrics: StateFlow<TaskMetrics> = _taskMetrics

    // ✅ 추가: 현재 필터 상태
    private val _currentFilter = MutableStateFlow(TaskFilter.ALL)
    val currentFilter: StateFlow<TaskFilter> = _currentFilter

    // ⭐️ 추가: 완료율 상태 (ViewModel에서 계산)
    private val _completionPercentage = MutableStateFlow(0)
    val completionPercentage: StateFlow<Int> = _completionPercentage


    private var taskListener: ListenerRegistration? = null
    private var metricsListener: ListenerRegistration? = null

    override fun onCleared() {
        super.onCleared()
        taskListener?.remove()
        metricsListener?.remove()
    }

    // C: Create Task
    fun createTask(
        groupId: String,
        title: String,
        assignedToUid: String,
        dueDate: Date?,
        priority: Int,
        precursorTaskId: String? = null // ✅ 추가
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
            status = if (precursorTaskId != null) TaskStatus.BLOCKED else TaskStatus.TODO, // 선행 Task가 있으면 BLOCKED
            priority = priority,
            dueDate = dueDate,
            precursorTaskId = precursorTaskId, // ✅ 추가
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
     * R: Read/Load Tasks (특정 Group ID로 필터링된 목록을 실시간으로 가져옵니다.)
     */
    fun loadTasks(groupId: String?) {
        _isLoading.value = true
        _error.value = null
        taskListener?.remove()

        if (groupId == null || groupId.isBlank()) {
            _taskList.value = emptyList()
            _isLoading.value = false
            _completionPercentage.value = 0 // 그룹 ID가 없으면 0%
            return
        }

        // Task 목록 실시간 리스너 설정
        taskListener = taskCollection
            .whereEqualTo("groupId", groupId)
            .orderBy("createdAt")
            .addSnapshotListener { snapshot, e ->
                _isLoading.value = false
                if (e != null) {
                    _error.value = "작업 목록 로드 실패: ${e.message}"
                    _taskList.value = emptyList()
                    _completionPercentage.value = 0 // 에러 발생 시 0%
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val allTasks = snapshot.toObjects(Task::class.java)
                    _taskList.value = allTasks

                    // ⭐️ 핵심: ViewModel에서 완료율 계산
                    calculateCompletionPercentage(allTasks)
                } else {
                    _taskList.value = emptyList()
                    _completionPercentage.value = 0
                }
            }

        // 통계 리스너도 함께 로드
        loadTaskMetrics(groupId)
    }

    /**
     * ⭐️ 추가: Task 목록을 기반으로 완료율을 계산하여 StateFlow를 업데이트합니다. (MVVM 원칙 준수)
     */
    private fun calculateCompletionPercentage(tasks: List<Task>) {
        val totalTasks = tasks.size

        // TaskStatus Enum 객체와 직접 비교
        val completedTasks = tasks.count { it.status == TaskStatus.DONE }

        val completionRatio = if (totalTasks == 0) 0f else completedTasks.toFloat() / totalTasks.toFloat()
        val percentage = (completionRatio * 100).toInt()

        _completionPercentage.value = percentage
    }


    /**
     * ✅ 추가: 그룹 내 태스크 통계를 실시간으로 로드합니다.
     */
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
                    blocked = tasks.count { it.status == TaskStatus.BLOCKED } // ✅ BLOCKED 상태 카운트 추가
                )
                _taskMetrics.value = metrics
            }
    }

    /**
     * U: Update Task (상태 변경 및 precursorTask 체크)
     */
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

    // U: Update Task (전체 Task 객체 업데이트)
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

    /**
     * Task 일괄 삭제 기능 (Cascading Delete Executor)
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

    /**
     * ✅ 추가: 태스크 목록 필터를 설정합니다.
     */
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

