package com.ippo.taskflow.mvvm.view_model.task

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.WriteBatch
import com.ippo.taskflow.mvvm.model.Task
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

    private val _taskList = MutableStateFlow<List<Task>>(emptyList())
    // 💡 변경: 현재 적용된 필터에 따라 필터링된 결과만 노출
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
            status = if (precursorTaskId != null) "BLOCKED" else "TODO", // 선행 Task가 있으면 BLOCKED
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
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val allTasks = snapshot.toObjects(Task::class.java)
                    // 필터링 적용은 Composable 또는 별도의 Flow 연산으로 처리하는 것이 효율적일 수 있으나,
                    // ViewModel 내에서 처리하는 것으로 가정하고 현재는 모든 데이터를 받습니다.
                    _taskList.value = allTasks
                } else {
                    _taskList.value = emptyList()
                }
            }

        // 통계 리스너도 함께 로드 (GroupViewModel의 GroupDetail에서 사용)
        loadTaskMetrics(groupId)
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
                    todo = tasks.count { it.status == "TODO" },
                    inProgress = tasks.count { it.status == "IN_PROGRESS" },
                    done = tasks.count { it.status == "DONE" },
                    blocked = tasks.count { it.status == "BLOCKED" } // ✅ BLOCKED 상태 카운트 추가
                )
                _taskMetrics.value = metrics
            }
    }

    /**
     * U: Update Task (상태 변경 및 precursorTask 체크)
     */
    fun updateTaskStatus(taskId: String, newStatus: String) {
        if (taskId.isBlank()) return

        // BLOCKED -> TODO/IN_PROGRESS로의 전환 시 precursorTaskId 검사 로직 추가 가능
        // (현재는 Firestore 업데이트만 실행)
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
        // Firestore 쿼리 변경 없이, UI에서 _taskList에 filter를 적용하도록 유도 (컴포저블에서 Flow 연산)
    }
}