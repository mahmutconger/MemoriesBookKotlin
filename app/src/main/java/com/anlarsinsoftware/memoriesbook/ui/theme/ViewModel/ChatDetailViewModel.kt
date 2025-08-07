package com.anlarsinsoftware.memoriesbook.ui.theme.ViewModel

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anlarsinsoftware.memoriesbook.R
import com.anlarsinsoftware.memoriesbook.ui.theme.Model.Message
import com.anlarsinsoftware.memoriesbook.ui.theme.Model.UserStatus
import com.anlarsinsoftware.memoriesbook.ui.theme.Model.Users
import com.anlarsinsoftware.memoriesbook.ui.theme.Util.KISS_EMOJI_GIF
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch




class ChatDetailViewModel(
    private val friendId: String,
    private val homeViewModel: HomeViewModel
) : ViewModel() {
    private val firestore = Firebase.firestore
    private val auth = Firebase.auth

    private val _userStatus = MutableStateFlow(UserStatus())
    val userStatus: StateFlow<UserStatus> = _userStatus.asStateFlow()

    private val _replyingToMessage = MutableStateFlow<Message?>(null)
    val replyingToMessage: StateFlow<Message?> = _replyingToMessage.asStateFlow()


    private fun observeStatus() {
        viewModelScope.launch {
            homeViewModel.observeUserStatus(friendId).collect { status ->
                _userStatus.value = status
            }
        }
    }

    private val _friendProfile = MutableStateFlow<Users?>(null)
    val friendProfile: StateFlow<Users?> = _friendProfile.asStateFlow()

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    init {
        if (friendId.isNotBlank()) {
            loadFriendProfile()
            fetchMessages()
            observeStatus()
        }
    }

    private fun loadFriendProfile() {
        firestore.collection("Users").document(friendId).get()
            .addOnSuccessListener { document ->
                _friendProfile.value = document.toObject(Users::class.java)
            }
    }

    fun onStartReply(message: Message) {
        _replyingToMessage.value = message
    }

    fun onCancelReply() {
        _replyingToMessage.value = null
    }



    fun sendMessage(text: String) {
        val messageToReplyTo = _replyingToMessage.value

        val currentUser = auth.currentUser
        if (currentUser == null || text.isBlank()) {
            return
        }

        val chatRoomId = getChatRoomId(currentUser.uid, friendId)
        val messageRef = firestore.collection("chats").document(chatRoomId).collection("messages")

        val message: Message

        val replyToUsername = if (messageToReplyTo != null) {
            // Eğer cevaplanan mesajı gönderen kişi mevcut kullanıcı ise, onun adını al.
            if (messageToReplyTo.senderId == currentUser.uid) {
                currentUser.displayName
            } else {
                _friendProfile.value?.username
            }
        } else {
            ""
        }

        if (text.trim() == "😘") {
            message = Message(
                senderId = currentUser.uid,
                type = "gif",
                mediaUrl = KISS_EMOJI_GIF,
                text = "😘",
                replyToMessageId = messageToReplyTo?.messageId ?: "",
                replyToMessageText = messageToReplyTo?.text ?: "",
                replyToSenderUsername = replyToUsername ?: "",
            )
        } else {
            message = Message(
                replyToMessageId = messageToReplyTo?.messageId ?: "",
                replyToMessageText = messageToReplyTo?.text ?: "",
                replyToSenderUsername = replyToUsername ?: "",
                senderId = currentUser.uid,
                type = "text",
                text = text
            )
        }

        messageRef.add(message)
            .addOnSuccessListener {
                Log.d("SendMessage", "Message sent successfully!")
                onCancelReply()
            }
            .addOnFailureListener { e ->
                Log.e("SendMessage", "Error sending message", e)
            }
    }

    private fun getChatRoomId(uid1: String, uid2: String): String {
        return if (uid1 > uid2) "${uid1}_$uid2" else "${uid2}_$uid1"
    }

    companion object {
        private const val MESSAGES_LIMIT = 25L
    }


    private fun fetchMessages() {
        val currentUser = auth.currentUser ?: return
        val chatRoomId = getChatRoomId(currentUser.uid, friendId)

        firestore.collection("chats").document(chatRoomId).collection("messages")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(MESSAGES_LIMIT)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    Log.e("FetchMessages", "Error listening for messages", error)
                    return@addSnapshotListener
                }

                if (value != null) {
                    val messageList = value.documents.mapNotNull { doc ->
                        doc.toObject(Message::class.java)?.copy(messageId = doc.id)
                    }
                    _messages.value = messageList.reversed()
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

class ChatDetailViewModelFactory(  private val friendId: String,private val homeViewModel: HomeViewModel) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ChatDetailViewModel(friendId, homeViewModel) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}