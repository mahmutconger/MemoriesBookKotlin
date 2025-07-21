package com.anlarsinsoftware.memoriesbook.ui.theme.Model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp

data class Message(
    val messageId: String = "",
    val senderId: String = "",
    val text: String = "",
    @ServerTimestamp
    val timestamp: Timestamp = Timestamp.now()
)