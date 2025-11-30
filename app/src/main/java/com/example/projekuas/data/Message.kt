package com.example.projekuas.data

import com.google.firebase.firestore.PropertyName

data class Message(
    val id: String = "",
    val senderId: String = "",
    val text: String = "",
    val timestamp: Long = System.currentTimeMillis()
)