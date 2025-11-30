package com.example.projekuas.data

import com.google.firebase.firestore.PropertyName

data class NotificationModel(
    val id: String = "",
    val title: String = "",
    val message: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    @get:PropertyName("isRead")
    @set:PropertyName("isRead")
    var isRead: Boolean = false,
    val type: String = "info" // "chat", "booking", "info"
)