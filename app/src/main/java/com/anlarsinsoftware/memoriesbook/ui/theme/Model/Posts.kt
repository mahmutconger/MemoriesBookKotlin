package com.anlarsinsoftware.memoriesbook.ui.theme.Model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName


data class Posts(
    val authorId: String = "",
    val useremail: String = "",
    val comment: String = "",
    @get:PropertyName("downloadurl") @set:PropertyName("downloadurl")
    var downloadUrl: String = "",
    val date: Timestamp? = null,
    val visibility: String = "public",
    val visibleTo: List<String> = emptyList(),
    var isLiked: Boolean = false,
    var documentId: String = "" // documentId için de varsayılan değer eklemek iyidir.
)
