package com.anlarsinsoftware.memoriesbook.ui.theme.ViewModel

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anlarsinsoftware.memoriesbook.R
import com.anlarsinsoftware.memoriesbook.ui.theme.Model.ChatItem
import com.anlarsinsoftware.memoriesbook.ui.theme.Model.DateSeparator
import com.anlarsinsoftware.memoriesbook.ui.theme.Model.Message
import com.anlarsinsoftware.memoriesbook.ui.theme.Model.MessageListItem
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
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale


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

    private val _messages = MutableStateFlow<List<ChatItem>>(emptyList())
    val messages: StateFlow<List<ChatItem>> = _messages.asStateFlow()

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



    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    private fun groupMessagesByDate(messages: List<Message>): List<ChatItem> {
        val groupedList = mutableListOf<ChatItem>()
        var lastDate: LocalDate? = null

        messages.reversed().forEach { message ->
            val messageInstant = Instant.ofEpochMilli(message.timestamp.toDate().time)
            val messageDate = LocalDate.ofInstant(messageInstant, ZoneId.systemDefault())

            if (lastDate == null || !messageDate.isEqual(lastDate)) {
                groupedList.add(DateSeparator(formatDateSeparator(messageDate)))
                lastDate = messageDate
            }
            groupedList.add(MessageListItem(message))
        }
        return groupedList
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

    fun deleteMessage(messageId: String) {
        val currentUser = auth.currentUser ?: return
        val chatRoomId = getChatRoomId(currentUser.uid, friendId)

        firestore.collection("chats").document(chatRoomId)
            .collection("messages").document(messageId)
            .delete()
            .addOnSuccessListener {
                Log.d("DeleteMessage", "Mesaj başarıyla silindi.")
            }
            .addOnFailureListener { e ->
                Log.e("DeleteMessage", "Mesaj silinirken hata oluştu.", e)
            }
    }


    fun editMessage(messageId: String, newText: String) {
        if (newText.isBlank()) {
            return
        }
        val currentUser = auth.currentUser ?: return
        val chatRoomId = getChatRoomId(currentUser.uid, friendId)
        val messageRef = firestore.collection("chats").document(chatRoomId)
            .collection("messages").document(messageId)

        val updates = mapOf(
            "text" to newText,
            "isEdited" to true
        )

        messageRef.update(updates)
            .addOnSuccessListener {
                Log.d("EditMessage", "Mesaj başarıyla düzenlendi ve 'isEdited' olarak işaretlendi.")
            }
            .addOnFailureListener { e ->
                Log.e("EditMessage", "Mesaj düzenlenirken hata oluştu.", e)
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
                    _messages.value = groupMessagesByDate(messageList)
                }
            }
    }

}

@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
fun formatLastSeenString(lastSeenMillis: Long?): String {
    if (lastSeenMillis == null) return "Çevrimdışı"

    val now = Instant.now()
    val lastSeenInstant = Instant.ofEpochMilli(lastSeenMillis)

    val zoneId = ZoneId.systemDefault()
    val today = LocalDate.now(zoneId)
    val yesterday = today.minusDays(1)

    val lastSeenDate = LocalDate.ofInstant(lastSeenInstant, zoneId)

    val minutesAgo = ChronoUnit.MINUTES.between(lastSeenInstant, now)

    return when {
        minutesAgo < 1 -> "az önce"
        minutesAgo < 60 -> "$minutesAgo dakika önce"
        lastSeenDate.isEqual(today) -> {
            val formatter = DateTimeFormatter.ofPattern("HH:mm", Locale("tr"))
            "bugün, ${lastSeenInstant.atZone(zoneId).format(formatter)}"
        }
        lastSeenDate.isEqual(yesterday) -> {
            val formatter = DateTimeFormatter.ofPattern("HH:mm", Locale("tr"))
            "dün, ${lastSeenInstant.atZone(zoneId).format(formatter)}"
        }
        lastSeenDate.year == today.year -> {
            val formatter = DateTimeFormatter.ofPattern("d MMM, HH:mm", Locale("tr"))
            lastSeenInstant.atZone(zoneId).format(formatter) // Örnek: 5 Ağu, 12:50
        }

        else -> {
            val formatter = DateTimeFormatter.ofPattern("d MMM yyyy, HH:mm", Locale("tr"))
            lastSeenInstant.atZone(zoneId).format(formatter) // Örnek: 5 Ağu 2024, 12:50
        }
    }
}

private fun formatDateSeparator(date: LocalDate): String {
    val today = LocalDate.now()
    val yesterday = today.minusDays(1)

    return when {
        date.isEqual(today) -> "Bugün"
        date.isEqual(yesterday) -> "Dün"
        date.year == today.year -> {
            val formatter = DateTimeFormatter.ofPattern("d MMMM", Locale("tr"))
            date.format(formatter)
        }
        else -> {
            val formatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale("tr"))
            date.format(formatter)
        }
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