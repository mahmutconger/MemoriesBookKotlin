package com.anlarsinsoftware.memoriesbook.ui.theme.Model

data class Users(
    val uid: String = "",
    val username: String = "",
    val photoUrl: String? = null,
    val email: String = "",
    val fcmToken: String = "",
    val lastSignIn: com.google.firebase.Timestamp? = null,
    val followers: List<String> = emptyList(),
    val following: List<String> = emptyList(),
    val friends: List<String> = emptyList()
)