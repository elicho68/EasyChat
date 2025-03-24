package com.example.easychat.ui.models

import com.google.firebase.Timestamp

data class Message(
    val senderId: String = "",
    val receiverId: String = "",
    val content: String = "",
    val messageType: String = "text",
    val timestamp: Timestamp = Timestamp.now()
)