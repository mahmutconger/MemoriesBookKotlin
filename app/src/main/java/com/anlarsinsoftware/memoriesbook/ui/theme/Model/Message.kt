package com.anlarsinsoftware.memoriesbook.ui.theme.Model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp

data class Message(
    val replyToMessageId: String = "",
    val replyToMessageText: String = "",
    val replyToSenderUsername: String = "",
    val messageId: String = "",
    val senderId: String = "",
    val text: String = "",
    val type: String = "text",
    val mediaUrl: String? = null,
    @ServerTimestamp
    val timestamp: Timestamp = Timestamp.now()
)