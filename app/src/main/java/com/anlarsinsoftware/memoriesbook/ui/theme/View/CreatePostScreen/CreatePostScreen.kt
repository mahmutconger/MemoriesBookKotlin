package com.anlarsinsoftware.memoriesbook.ui.theme.View.CreatePostScreen

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Face
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.anlarsinsoftware.memoriesbook.ui.theme.ViewModel.ConnectionsViewModel
import com.anlarsinsoftware.memoriesbook.ui.theme.ViewModel.CreatePostViewModel
import com.anlarsinsoftware.memoriesbook.ui.theme.ViewModel.UploadUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePostScreen(
    navController: NavController,
    createPostViewModel: CreatePostViewModel,
    connectionsViewModel: ConnectionsViewModel
) {
    val context = LocalContext.current

    var imageUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var videoUri by remember { mutableStateOf<Uri?>(null) }
    var showMediaChoiceDialog by remember { mutableStateOf(false) }
    var comment by remember { mutableStateOf("") }
    var visibility by remember { mutableStateOf("public") }
    var visibleToList by remember { mutableStateOf<List<String>>(emptyList()) }
    var showFriendSelector by remember { mutableStateOf(false) }
    val uiState by createPostViewModel.uiState.collectAsState()

    val friends by connectionsViewModel.friends.collectAsState()
    val followers by connectionsViewModel.followers.collectAsState()

    val multiplePhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(5),
        onResult = { uris ->
            videoUri = null
            imageUris = uris
        }
    )
    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            imageUris = emptyList()
            videoUri = uri
        }
    )

    LaunchedEffect(key1 = uiState) {
        when (val state = uiState) {
            is UploadUiState.Success -> {
                Toast.makeText(context, "Post başarıyla paylaşıldı!", Toast.LENGTH_SHORT).show()
                navController.popBackStack()
            }

            is UploadUiState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
            }

            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Anı Oluştur",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri Git")
                    }
                },
                actions = { Spacer(modifier = Modifier.width(48.dp)) }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.fillMaxWidth().height(250.dp).clip(RoundedCornerShape(12.dp))
                    .border(1.dp, Color.Gray, RoundedCornerShape(12.dp))
                    .clickable { showMediaChoiceDialog = true },
                contentAlignment = Alignment.Center
            ) {
                if (videoUri != null) {
                    Icon(Icons.Default.Check, contentDescription = "Video Seçildi", modifier = Modifier.size(80.dp))
                } else if (imageUris.isNotEmpty()) {
                    AsyncImage(
                        model = imageUris.first(),
                        contentDescription = "Seçilen Resimler (${imageUris.size})",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text("Medya Seçmek İçin Tıkla")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = comment,
                onValueChange = { comment = it },
                label = { Text("Açıklama Ekle...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = visibility == "public", onClick = { visibility = "public" })
                Text("Herkese Açık")
                Spacer(Modifier.width(16.dp))
                RadioButton(
                    selected = visibility == "private",
                    onClick = { visibility = "private" })
                Text("Özel")
            }
            if (visibility == "private") {
                OutlinedButton(
                    onClick = { showFriendSelector = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Görecek Kişileri Seç (${visibleToList.size})")
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // --- PAYLAŞ BUTONU ---
            Button(
                onClick = {
                    val urisToUpload = if (imageUris.isNotEmpty()) imageUris else videoUri?.let { listOf(it) }
                    val mediaType = if (imageUris.isNotEmpty()) "image" else "video"

                    if (!urisToUpload.isNullOrEmpty()) {
                        createPostViewModel.createPost(urisToUpload, comment, mediaType, visibility, visibleToList)
                    } else {
                        Toast.makeText(context, "Lütfen bir fotoğraf veya video seçin.", Toast.LENGTH_SHORT).show()
                    }
                },
                enabled = uiState !is UploadUiState.Loading,
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                if (uiState is UploadUiState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )
                } else {
                    Text("Paylaş", fontSize = 16.sp)
                }
            }

            if (showFriendSelector) {
                FriendSelectorBottomSheet(
                    friends = friends,
                    followers = followers,
                    initiallySelectedIds = visibleToList,
                    onDismiss = { showFriendSelector = false },
                    onSelectionDone = { selectedIds ->
                        visibleToList = selectedIds
                        showFriendSelector = false
                    }
                )
            }

            if (showMediaChoiceDialog) {
                AlertDialog(
                    onDismissRequest = { showMediaChoiceDialog = false },
                    title = { Text("Medya Türü Seç") },
                    text = { Text("Ne paylaşmak istersin?") },
                    confirmButton = {
                        TextButton(onClick = {
                            showMediaChoiceDialog = false
                            multiplePhotoPickerLauncher.launch(
                                PickVisualMediaRequest(

                                )
                            )
                        }) { Text("Fotoğraf (Çoklu)") }
                    },
                    dismissButton = {
                        TextButton(onClick = {
                            showMediaChoiceDialog = false
                            videoPickerLauncher.launch(
                                PickVisualMediaRequest(
                                    // DOĞRU YOL: Tam adını kullanıyoruz
                                    ActivityResultContracts.PickVisualMedia.VideoOnly
                                )
                            )
                        }) { Text("Video") }
                    }
                )
            }
        }
    }
}