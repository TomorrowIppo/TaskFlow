package com.ippo.taskflow.mvvm.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

// TaskStatus.kt
enum class TaskStatus(val value: String) {
    TODO("TODO"),
    IN_PROGRESS("IN_PROGRESS"),
    DONE("DONE"),
    BLOCKED("BLOCKED")
}
data class Task(
    // 💡 Firestore 문서 ID와 매핑됩니다.
    @DocumentId
    val taskId: String = "",

    // 이 태스크가 속한 그룹의 ID (Groups Collection 참조)
    val groupId: String = "",

    val title: String = "",
    val description: String = "",

    // 상태: "TODO", "IN_PROGRESS", "DONE", "BLOCKED" (BLOCKED 추가를 위해 String 유지)
    val status: TaskStatus = TaskStatus.IN_PROGRESS,

    // 우선순위: 1(높음) ~ 3(낮음)
    val priority: Int = 3,

    // ✅ 명세 반영: 이 Task가 의존하는 선행 Task의 ID (BLOCKED 상태와 관련됨)
    val precursorTaskId: String? = null,

    // 태스크가 할당된 사용자의 UID
    val assignedToUid: String = "",

    // 태스크를 생성한 사용자의 UID
    val createdByUid: String = "",

    // 마감 기한
    val dueDate: Date? = null,

    // 💡 서버 타임스탬프와 매핑됩니다. (문서 생성 시점)
    @ServerTimestamp
    val createdAt: Date? = null
) {
    // 💡 DONE 상태를 기반으로 완료 여부를 판단하는 Getter
    val isCompleted: Boolean
        get() = this.status == TaskStatus.DONE
}