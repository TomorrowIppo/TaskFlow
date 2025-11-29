package com.ippo.taskflow.data

import com.google.firebase.firestore.DocumentId
import java.util.Date

data class User(
    @DocumentId val uid: String = "",
    val email: String = "",
    val name: String = "",
    val createdAt: Date = Date()
)