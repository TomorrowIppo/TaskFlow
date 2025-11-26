package com.ippo.taskflow.data

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

/**
 * Groups Collection 문서 구조를 위한 데이터 클래스
 */
data class Group(
    // 💡 Firestore 문서 ID와 매핑됩니다.
    @DocumentId
    val groupId: String = "",

    val name: String = "",
    val description: String = "",

    // 그룹 생성자의 UID (리더 역할)
    val ownerId: String = "",

    // 그룹 멤버 UID 목록
    val memberUids: List<String> = emptyList(),

    val isPrivate: Boolean = false,

    // 💡 서버 타임스탬프와 매핑됩니다. (문서 생성 시점)
    @ServerTimestamp
    val createdAt: Date? = null
)