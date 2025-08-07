package com.anlarsinsoftware.memoriesbook.ui.theme.View.MessageScreen

import android.os.Build
import android.util.Log
import androidx.compose.animation.core.DecayAnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.animation.splineBasedDecay
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.gestures.snapTo
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
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
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.anlarsinsoftware.memoriesbook.R
import com.anlarsinsoftware.memoriesbook.ui.theme.Model.Message
import com.anlarsinsoftware.memoriesbook.ui.theme.Util.myIconButtonPainter
import com.anlarsinsoftware.memoriesbook.ui.theme.ViewModel.ChatDetailViewModel
import com.anlarsinsoftware.memoriesbook.ui.theme.ViewModel.formatLastSeen
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.collections.isNotEmpty
import kotlin.math.roundToInt

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
                    SwipeableMessageBox(
                        message = message,
                        isSentByCurrentUser = message.senderId == Firebase.auth.currentUser?.uid,
                        onReply = {
                            chatDetailViewModel.onStartReply(it)
                        }
                    )
                }
            }

            val replyingToMessage by chatDetailViewModel.replyingToMessage.collectAsState()
            if (replyingToMessage != null) {
                ReplyPreview(
                    message = replyingToMessage!!,
                    onCancel = { chatDetailViewModel.onCancelReply() }
                )
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
fun MessageItem(
    message: Message,
    isSentByCurrentUser: Boolean,
    modifier: Modifier = Modifier
) {
    val gifImageLoader = rememberGifImageLoader(LocalContext.current)
    val alignment = if (isSentByCurrentUser) Alignment.CenterEnd else Alignment.CenterStart
    val bubbleColor = if (isSentByCurrentUser) colorResource(R.color.sendMessage)
    else colorResource(R.color.receiverMessage)

    val formattedTimestamp = remember(message.timestamp) {
        formatMessageTimestamp(message.timestamp)
    }

    Column(
        modifier = modifier.fillMaxWidth(),
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
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                if (message.replyToMessageId.isNotBlank()) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.Black.copy(alpha = 0.2f),
                        modifier = Modifier.padding(bottom = 4.dp)
                    ) {
                        Column(modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {

                                myIconButtonPainter(R.drawable.ic_reply, 15) {
                                    null
                                }

                                Text(
                                    text = message.replyToSenderUsername,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White.copy(alpha = 0.9f),
                                    fontSize = 13.sp
                                )
                            }

                            Text(
                                text = message.replyToMessageText,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                when (message.type) {
                    "text" -> {
                        Text(
                            text = message.text,
                            color = Color.White
                        )
                    }

                    "gif" -> {
                        AsyncImage(
                            model = message.mediaUrl,
                            contentDescription = "GIF",
                            imageLoader = gifImageLoader,
                            modifier = Modifier
                                .size(150.dp)
                                .clip(RoundedCornerShape(12.dp))
                        )
                    }
                }
            }
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

private enum class DragAnchors {
    Start,
    Reply,
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SwipeableMessageBox(
    message: Message,
    isSentByCurrentUser: Boolean,
    onReply: (Message) -> Unit,
) {
    val density = LocalDensity.current
    val decayAnimationSpec: DecayAnimationSpec<Float> = remember { splineBasedDecay(density) }


    val state = remember {
        AnchoredDraggableState(
            initialValue = DragAnchors.Start,
            anchors = DraggableAnchors {
                DragAnchors.Start at 0f
                DragAnchors.Reply at with(density) { 128.dp.toPx() }
            },
            positionalThreshold = { distance: Float -> distance * 0.5f },
            velocityThreshold = { with(density) { 100.dp.toPx() } },
            snapAnimationSpec = tween(durationMillis = 1000),
            decayAnimationSpec = decayAnimationSpec
        )
    }

    LaunchedEffect(state.currentValue) {
        if (state.currentValue == DragAnchors.Reply) {
            onReply(message)
            state.snapTo(DragAnchors.Start)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp)) // Kırpma efekti taşmayı engeller
    ) {
        // ARKA PLAN: Cevapla ikonu
        Box(
            modifier = Modifier
                .align(if (isSentByCurrentUser) Alignment.CenterEnd else Alignment.CenterStart)
                .padding(horizontal = 16.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_reply),
                contentDescription = "Cevapla",
                modifier = Modifier
                    .scale(
                        state.progress(from = DragAnchors.Start, to = DragAnchors.Reply)
                    )
                    .size(50.dp),
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }

        MessageItem(
            message = message,
            isSentByCurrentUser = isSentByCurrentUser,
            modifier = Modifier
                .offset { IntOffset(x = state.requireOffset().roundToInt(), y = 0) }
                .anchoredDraggable(
                    state = state,
                    orientation = Orientation.Horizontal,
                    reverseDirection = isSentByCurrentUser
                )
        )
    }
}

@Composable
fun ReplyPreview(message: Message, onCancel: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(8.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                message.replyToSenderUsername,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                fontSize = 14.sp
            )
            Text(
                message.text,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontSize = 14.sp
            )
        }
        IconButton(onClick = onCancel) {
            Icon(Icons.Default.Close, contentDescription = "Cevaplamayı İptal Et")
        }
    }
}