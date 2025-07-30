package com.anlarsinsoftware.memoriesbook.ui.theme.Model

data class User(
    val userId: String = "",
    val username: String = "",
    val photoUrl: String? = null,
    val useremail: String = "",
    val fcmToken: String = "",
    val lastSignIn: com.google.firebase.Timestamp? = null,
    val followers: List<String> = emptyList(),
    val following: List<String> = emptyList(),
    val friends: List<String> = emptyList()
)