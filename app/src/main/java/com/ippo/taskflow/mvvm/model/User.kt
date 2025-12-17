package com.ippo.taskflow.mvvm.model

import com.google.firebase.firestore.DocumentId
import java.util.Date

data class User(
    @DocumentId val uid: String = "",
    val email: String = "",
    val name: String = "",
    // 추가: 사용자의 상태 메시지 (선택적)
    val statusMsg: String? = null,
    val createdAt: Date = Date()
)