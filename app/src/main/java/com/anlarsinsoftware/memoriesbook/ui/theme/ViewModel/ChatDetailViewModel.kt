package com.anlarsinsoftware.memoriesbook.ui.theme.ViewModel

import androidx.compose.runtime.Composable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
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

// Bir mesajı temsil eden data class
data class Message(
    val senderId: String = "",
    val text: String = "",
    val timestamp: com.google.firebase.Timestamp = com.google.firebase.Timestamp.now()
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

    private fun fetchMessages() {
        val currentUser = auth.currentUser ?: return

        // İki kullanıcı arasında her zaman aynı olan benzersiz bir sohbet odası ID'si oluşturuyoruz
        val chatRoomId = if (currentUser.uid > friendId) {
            "${currentUser.uid}_$friendId"
        } else {
            "${friendId}_${currentUser.uid}"
        }

        firestore.collection("chats").document(chatRoomId).collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { value, error ->
                if (value != null) {
                    _messages.value = value.toObjects(Message::class.java)
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