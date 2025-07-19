package com.anlarsinsoftware.memoriesbook.ui.theme.Model

import com.google.firebase.Timestamp

data class Comments(
    val comment: String = "",
    val date: Timestamp = Timestamp.now(),
    val userId: String = "", // 'user' yerine 'userId' ve 'username' kullanalım
    val username: String = "",
    val userPhotoUrl: String = "",
    val documentId: String = "",
    val postId: String = "",
    val likedBy: List<String> = emptyList()
)