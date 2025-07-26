package com.anlarsinsoftware.memoriesbook.ui.theme.Model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName


data class Posts(
    val authorId: String = "",
    val useremail: String = "",
    val authorUsername: String = "",
    val authorPhotoUrl: String = "",
    val comment: String = "",
    val date: Timestamp? = null,
    val visibility: String = "public",
    val visibleTo: List<String> = emptyList(),
    val commentCount: Long = 0,
    val likedBy: List<String> = emptyList(),
    val mediaType: String = "image",
    val mediaUrls: List<String> = emptyList(),
    val thumbnailUrl: String = "",
    var documentId: String = ""
)
