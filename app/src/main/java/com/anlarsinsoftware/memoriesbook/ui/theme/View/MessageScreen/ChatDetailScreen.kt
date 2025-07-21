package com.anlarsinsoftware.memoriesbook.ui.theme.View.MessageScreen

import android.graphics.drawable.ColorDrawable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.colorspace.ColorSpace
import androidx.compose.ui.graphics.colorspace.ColorSpaces
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.anlarsinsoftware.memoriesbook.R
import com.anlarsinsoftware.memoriesbook.ui.theme.Model.Message
import com.anlarsinsoftware.memoriesbook.ui.theme.ViewModel.ChatDetailViewModel
import com.anlarsinsoftware.memoriesbook.ui.theme.ViewModel.formatLastSeen
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.collections.isNotEmpty

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailScreen(
    navController: NavController,
    chatDetailViewModel: ChatDetailViewModel = viewModel()
) {
    val friendProfile by chatDetailViewModel.friendProfile.collectAsState()
    val userStatus by chatDetailViewModel.userStatus.collectAsState()
    val messages by chatDetailViewModel.messages.collectAsState()
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = friendProfile?.username ?: "Yükleniyor...",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        val lastSeenText = formatLastSeen(userStatus)
                        if (lastSeenText.isNotBlank()) {
                            Text(
                                text = lastSeenText,
                                fontSize = 12.sp,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                    }
                },
                navigationIcon = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri")
                        }
                        AsyncImage(
                            model = friendProfile?.photoUrl,
                            contentDescription = "${friendProfile?.username} profil fotoğrafı",
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                        )
                    }
                },

                actions = {
                    IconButton(onClick = { navController.navigate("settings_screen") }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            tint = MaterialTheme.colorScheme.primary,
                            contentDescription = "Seçenekler Menüsü"
                        )
                    }
                },

                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .imePadding()
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(items = messages, key = { it.messageId }) { message ->
                    MessageItem(
                        message = message,
                        // Mesajı mevcut kullanıcı mı gönderdi?
                        isSentByCurrentUser = message.senderId == Firebase.auth.currentUser?.uid
                    )
                }
            }
            MessageInput(
                onSendMessage = { text ->
                    chatDetailViewModel.sendMessage(text)
                }
            )
        }
    }
}

private fun Calendar.isSameDayAs(other: Calendar): Boolean {
    return this.get(Calendar.YEAR) == other.get(Calendar.YEAR) &&
            this.get(Calendar.DAY_OF_YEAR) == other.get(Calendar.DAY_OF_YEAR)
}

private fun formatMessageTimestamp(timestamp: com.google.firebase.Timestamp?): String {
    if (timestamp == null) return ""

    val messageDate = timestamp.toDate()

    val messageCal = Calendar.getInstance().apply { time = messageDate }
    val todayCal = Calendar.getInstance()
    val yesterdayCal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
    val weekAgoCal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -7) }

    if (messageCal.before(weekAgoCal)) {
        val fullDateFormatter = SimpleDateFormat("dd.MM.yyyy", Locale("tr"))
        return fullDateFormatter.format(messageDate)
    }

    val timeFormatter = SimpleDateFormat("HH:mm", Locale("tr"))
    val timeStr = timeFormatter.format(messageDate)

    val dayStr = when {

        messageCal.isSameDayAs(todayCal) -> "Bugün"
        messageCal.isSameDayAs(yesterdayCal) -> "Dün"
        else -> {
            val dayFormatter = SimpleDateFormat("E", Locale("tr")) // "E" -> Pzt, Sal...
            dayFormatter.format(messageDate)
        }
    }

    return "$dayStr, $timeStr"
}



@Composable
fun MessageItem(message: Message, isSentByCurrentUser: Boolean) {
    val alignment = if (isSentByCurrentUser) Alignment.CenterEnd else Alignment.CenterStart
    val bubbleColor = if (isSentByCurrentUser) colorResource(R.color.sendMessage)
    else  colorResource(R.color.receiverMessage)

    val formattedTimestamp = remember(message.timestamp) {
        formatMessageTimestamp(message.timestamp)
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isSentByCurrentUser) Alignment.End else Alignment.Start
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = bubbleColor,
            modifier = Modifier.padding(
                start = if (isSentByCurrentUser) 48.dp else 0.dp,
                end = if (isSentByCurrentUser) 0.dp else 48.dp
            )
        ) {
            Text(
                text = message.text,
                color = Color.White,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
        Text(
            text = formattedTimestamp,
            style = MaterialTheme.typography.bodySmall,
            color = Color.DarkGray,
            modifier = Modifier.padding(top = 4.dp, start = 8.dp, end = 8.dp)
        )
    }
}


@Composable
fun MessageInput(onSendMessage: (String) -> Unit) {
    var text by remember { mutableStateOf("") }

    Row(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            label = { Text("Mesaj yaz...") },
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(8.dp))
        IconButton(
            onClick = {
                if (text.isNotBlank()) {
                    onSendMessage(text)
                    text = "" // Gönderdikten sonra metin alanını temizle
                }
            }
        ) {
            Icon(Icons.Default.Send, contentDescription = "Mesajı Gönder")
        }
    }
}