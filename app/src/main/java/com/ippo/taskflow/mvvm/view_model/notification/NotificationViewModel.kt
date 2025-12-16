package com.ippo.taskflow.mvvm.view_model.notification

import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.ippo.taskflow.mvvm.model.AppNotification
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class NotificationState(
    val isLoading: Boolean = false,
    val items: List<AppNotification> = emptyList(),
    val error: String? = null
)

class NotificationViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()

    private val _state = MutableStateFlow(NotificationState(isLoading = true))
    val state: StateFlow<NotificationState> = _state

    private var listener: ListenerRegistration? = null

    override fun onCleared() {
        super.onCleared()
        listener?.remove()
    }

    fun start(uid: String) {
        _state.value = _state.value.copy(isLoading = true, error = null)
        listener?.remove()

        listener = db.collection("users").document(uid)
            .collection("notifications")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, e ->
                if (e != null) {
                    _state.value = _state.value.copy(isLoading = false, items = emptyList(), error = e.message)
                    return@addSnapshotListener
                }

                val list = snap?.documents?.map { d ->
                    AppNotification(
                        id = d.id,
                        type = d.getString("type") ?: "CHAIN",
                        message = d.getString("message") ?: "",
                        dateText = d.getString("dateText") ?: "",
                        createdAt = d.getLong("createdAt") ?: 0L,
                        isRead = d.getBoolean("isRead") ?: false,
                        taskId = d.getString("taskId"),
                        groupId = d.getString("groupId"),
                        dedupeKey = d.getString("dedupeKey")
                    )
                } ?: emptyList()

                _state.value = _state.value.copy(isLoading = false, items = list, error = null)
            }
    }

    fun markRead(uid: String, notificationId: String) {
        db.collection("users").document(uid)
            .collection("notifications").document(notificationId)
            .update("isRead", true)
    }
}