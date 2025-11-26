package com.ippo.taskflow.data

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

/**
 * Tasks Collection 문서 구조를 위한 데이터 클래스
 */
data class Task(
    // 💡 Firestore 문서 ID와 매핑됩니다.
    @DocumentId
    val taskId: String = "",

    // 이 태스크가 속한 그룹의 ID (Groups Collection 참조)
    val groupId: String = "",

    val title: String = "",
    val description: String = "",

    // 상태: "TODO", "IN_PROGRESS", "DONE"
    val status: String = "TODO",

    // 우선순위: 1(높음) ~ 3(낮음)
    val priority: Int = 3,

    // 태스크가 할당된 사용자의 UID
    val assignedToUid: String = "",

    // 태스크를 생성한 사용자의 UID
    val createdByUid: String = "",

    // 마감 기한
    val dueDate: Date? = null,

    // 💡 서버 타임스탬프와 매핑됩니다. (문서 생성 시점)
    @ServerTimestamp
    val createdAt: Date? = null
)