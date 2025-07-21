package com.anlarsinsoftware.memoriesbook.ui.theme.ViewModel

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anlarsinsoftware.memoriesbook.ui.theme.Model.Message
import com.anlarsinsoftware.memoriesbook.ui.theme.Model.UserStatus
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Locale

// UI'da gösterilecek arkadaş profili
data class FriendProfile(
    val uid: String = "",
    val username: String = "",
    val photoUrl: String = "",
    val lastSignIn: com.google.firebase.Timestamp? = null
)



class ChatDetailViewModel(
    private val friendId: String,
    private val homeViewModel: HomeViewModel
) : ViewModel() {
    private val firestore = Firebase.firestore
    private val auth = Firebase.auth

    private val _userStatus = MutableStateFlow(UserStatus())
    val userStatus: StateFlow<UserStatus> = _userStatus.asStateFlow()

    private fun observeStatus() {
        viewModelScope.launch {
            homeViewModel.observeUserStatus(friendId).collect { status ->
                _userStatus.value = status
            }
        }
    }

    private val _friendProfile = MutableStateFlow<FriendProfile?>(null)
    val friendProfile: StateFlow<FriendProfile?> = _friendProfile.asStateFlow()

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    init {
        if (friendId.isNotBlank()) {
            loadFriendProfile()
            fetchMessages()
            observeStatus() // Durumu dinlemeyi başlat
        }
    }

    private fun loadFriendProfile() {
        firestore.collection("Users").document(friendId).get()
            .addOnSuccessListener { document ->
                _friendProfile.value = document.toObject(FriendProfile::class.java)
            }
    }

    fun sendMessage(text: String) {
        val currentUser = auth.currentUser
        if (currentUser == null || text.isBlank()) {
            return // Kullanıcı giriş yapmamışsa veya mesaj boşsa gönderme
        }

        val chatRoomId = getChatRoomId(currentUser.uid, friendId)
        val messageRef = firestore.collection("chats").document(chatRoomId).collection("messages")

        val message = Message(
            senderId = currentUser.uid,
            text = text
            // timestamp, @ServerTimestamp sayesinde otomatik eklenecek
        )

        // Yeni mesajı Firestore'a ekliyoruz.
        messageRef.add(message)
            .addOnSuccessListener {
                Log.d("SendMessage", "Message sent successfully!")
            }
            .addOnFailureListener { e ->
                Log.e("SendMessage", "Error sending message", e)
            }
    }

    // fetchMessages fonksiyonundaki chatRoomId mantığını bir yardımcı fonksiyona taşıyalım
    private fun getChatRoomId(uid1: String, uid2: String): String {
        return if (uid1 > uid2) "${uid1}_$uid2" else "${uid2}_$uid1"
    }

    private fun fetchMessages() {
        val currentUser = auth.currentUser ?: return
        val chatRoomId = getChatRoomId(currentUser.uid, friendId)

        firestore.collection("chats").document(chatRoomId).collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { value, error ->
                if (value != null) {
                    // documentId'yi de alarak map'leyelim
                    _messages.value = value.documents.map { doc ->
                        doc.toObject(Message::class.java)!!.copy(messageId = doc.id)
                    }
                }
            }
    }

}

@Composable
fun formatLastSeen(status: UserStatus): String {
    if (status.isOnline) return "Çevrimiçi"

    val lastSeenMillis = status.lastSeen ?: return "Çevrimdışı"
    val currentTimeMillis = System.currentTimeMillis()
    val differenceMinutes = (currentTimeMillis - lastSeenMillis) / 1000 / 60

    return when {
        differenceMinutes < 60 -> "$differenceMinutes dk önce"
        differenceMinutes < 1440 -> "${differenceMinutes / 60} saat önce"
        else -> "Dün veya daha önce"
    }
}

// ViewModel'e HomeViewModel'i yollamak için bir Factory
class ChatDetailViewModelFactory(  private val friendId: String,private val homeViewModel: HomeViewModel) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ChatDetailViewModel(friendId, homeViewModel) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}