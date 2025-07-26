package com.anlarsinsoftware.memoriesbook.ui.theme.View.CreatePostScreen

import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.anlarsinsoftware.memoriesbook.ui.theme.ViewModel.ConnectionsViewModel
import com.anlarsinsoftware.memoriesbook.ui.theme.ViewModel.CreatePostViewModel
import com.anlarsinsoftware.memoriesbook.ui.theme.ViewModel.UploadUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePostScreen(
    navController: NavController,
    createPostViewModel: CreatePostViewModel,
    connectionsViewModel: ConnectionsViewModel
) {
    val context = LocalContext.current


    var visibility by rememberSaveable { mutableStateOf("public") }
    var showMediaChoiceDialog by remember { mutableStateOf(false) }
    var visibleToList by rememberSaveable { mutableStateOf<List<String>>(emptyList()) }
    var showFriendSelector by remember { mutableStateOf(false) }
    val uiState by createPostViewModel.uiState.collectAsState()

    val imageUris by createPostViewModel.selectedImageUris.collectAsState()
    val videoUri by createPostViewModel.selectedVideoUri.collectAsState()
    val thumbnailBitmap by createPostViewModel.thumbnailBitmap.collectAsState()
    val comment by createPostViewModel.comment.collectAsState()

    val friends by connectionsViewModel.friends.collectAsState()
    val followers by connectionsViewModel.followers.collectAsState()


    val scope = rememberCoroutineScope()
    val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(savedStateHandle, videoUri) {
        savedStateHandle?.getLiveData<Long>("selected_thumbnail_time")
            ?.observe(lifecycleOwner) { timeMs ->
                if (timeMs > 0 && videoUri != null) {
                    scope.launch {
                        val bitmap = withContext(Dispatchers.IO) {
                            val retriever = MediaMetadataRetriever()
                            try {
                                retriever.setDataSource(context, videoUri)
                                retriever.getFrameAtTime(timeMs * 1000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                            } catch (e: Exception) {
                                null
                            } finally {
                                retriever.release()
                            }
                        }
                        createPostViewModel.onThumbnailCreated(bitmap)
                    }
                    savedStateHandle.remove<Long>("selected_thumbnail_time")
                }
            }
    }

    val multiplePhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(5),
        onResult = { uris -> createPostViewModel.onImagesSelected(uris) }
    )
    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> createPostViewModel.onVideoSelected(uri) }
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
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, Color.Gray, RoundedCornerShape(12.dp))
                    .clickable { showMediaChoiceDialog = true },
                contentAlignment = Alignment.Center
            ) {
                if (thumbnailBitmap != null) {
                    Image(
                        bitmap = thumbnailBitmap!!.asImageBitmap(),
                        contentDescription = "Seçilen Video Kapağı",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                // 2. Eğer resimler seçildiyse ilkini göster
                else if (imageUris.isNotEmpty()) {
                    AsyncImage(
                        model = imageUris.first(),
                        contentDescription = "Seçilen Resimler (${imageUris.size})",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                // 3. Hiçbir şey seçilmediyse varsayılan yazıyı göster
                else {
                    Text("Medya Seçmek İçin Tıkla")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = comment,
                onValueChange = { createPostViewModel.onCommentChange(it) },
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
                    createPostViewModel.createPost(visibility, visibleToList)
                },
                enabled = uiState !is UploadUiState.Loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
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

            if (videoUri != null) {
                OutlinedButton(
                    onClick = {
                        val encodedUri = Uri.encode(videoUri.toString())
                        navController.navigate("thumbnail_selector/$encodedUri")
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Eğer kapak seçildiyse metni değiştir
                    Text(if (thumbnailBitmap == null) "Kapak Fotoğrafı Seç" else "Kapak Fotoğrafını Değiştir")
                }
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