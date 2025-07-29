package com.anlarsinsoftware.memoriesbook.ui.theme.Model

class Friends {
}
data class FriendProfile(
    val uid: String = "",
    val username: String = "",
    val photoUrl: String = "",
    val fcmToken: String = "",
    val lastSignIn: com.google.firebase.Timestamp? = null
)