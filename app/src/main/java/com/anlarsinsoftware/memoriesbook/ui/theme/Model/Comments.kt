package com.anlarsinsoftware.memoriesbook.ui.theme.Model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName

data class Comments(
    val comment: String = "",
    @get:PropertyName("timestamp") @set:PropertyName("timestamp")
    var date: Timestamp= Timestamp.now(),
    val user: String = "",
    val documentId: String = "",
    val postId: String = "",
    var isLiked: Boolean = false
)