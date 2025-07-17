package com.anlarsinsoftware.memoriesbook.ui.theme.Model

class ConnectionsModel {
}
// Arayüzde gösterilecek kullanıcı modeli
data class SearchResultUser(
    val uid: String = "",
    val username: String = "",
    val email: String = ""
    // İsteğe bağlı olarak requestStatus: "sent", "friends", "none" gibi bir alan eklenebilir
)

//profil için takipçiler ve takip edilenler
data class Followers(
    val uid: String = "",
    val username: String = "",
    val email: String = ""
)

data class Following(
    val uid: String = "",
    val username: String = "",
    val email: String = ""
)

//takip isteklerini görüntüleme
data class FriendRequest(
    val senderId: String = "",
    val receiverId: String = "",
    val status: String = "",
    val documentId: String = "" // isteği kabul/reddetmek için bu ID'ye ihtiyacımız olacak
)

// UI'da gösterilecek son hali: İstek ID'si ve gönderenin profili bir arada
data class PendingRequest(
    val requestId: String,
    val senderProfile: SearchResultUser
)