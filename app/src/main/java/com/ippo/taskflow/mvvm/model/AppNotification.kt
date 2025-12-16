package com.ippo.taskflow.mvvm.model

data class AppNotification(
    val id: String = "",
    val type: String = "CHAIN",          // "CHAIN" | "DEADLINE"
    val message: String = "",
    val dateText: String = "",
    val createdAt: Long = 0L,
    val isRead: Boolean = false,
    val taskId: String? = null,
    val groupId: String? = null,
    val dedupeKey: String? = null
)