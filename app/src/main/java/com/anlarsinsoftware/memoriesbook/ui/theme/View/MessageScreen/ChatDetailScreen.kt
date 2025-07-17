package com.anlarsinsoftware.memoriesbook.ui.theme.View.MessageScreen

import MessagesViewModel
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.anlarsinsoftware.memoriesbook.ui.theme.Model.FriendProfile
import com.anlarsinsoftware.memoriesbook.ui.theme.ViewModel.ChatDetailViewModel
import com.anlarsinsoftware.memoriesbook.ui.theme.ViewModel.ChatDetailViewModelFactory
import com.anlarsinsoftware.memoriesbook.ui.theme.ViewModel.formatLastSeen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailScreen(
    navController: NavController,
    chatDetailViewModel: ChatDetailViewModel = viewModel()
) {
    val friendProfile by chatDetailViewModel.friendProfile.collectAsState()
    val userStatus by chatDetailViewModel.userStatus.collectAsState()
    val messages by chatDetailViewModel.messages.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    // Başlık kısmına Column ekleyerek alt alta iki metin koyuyoruz
                    Column {
                        Text(
                            text = friendProfile?.username ?: "Yükleniyor...",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        // Tarihi formatlayan fonksiyonu burada çağırıyoruz
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
                            modifier = Modifier.size(40.dp).clip(CircleShape)
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
        Column(modifier = Modifier.padding(innerPadding)) {

        }
    }
}