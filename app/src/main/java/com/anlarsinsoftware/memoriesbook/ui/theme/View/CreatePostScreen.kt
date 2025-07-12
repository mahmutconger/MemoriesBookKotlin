package com.anlarsinsoftware.memoriesbook.ui.theme.View

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.anlarsinsoftware.memoriesbook.R
import com.anlarsinsoftware.memoriesbook.ui.theme.Tools.showToast
import com.anlarsinsoftware.memoriesbook.ui.theme.ViewModel.CreatePostViewModel
import com.anlarsinsoftware.memoriesbook.ui.theme.ViewModel.HomeViewModel
import com.anlarsinsoftware.memoriesbook.ui.theme.ViewModel.UploadUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePostScreen(navController: NavController, createPostViewModel: CreatePostViewModel = viewModel()) {
    var postDescription by remember { mutableStateOf("") }
    val context = LocalContext.current

    var comment by remember { mutableStateOf("") }
    var visibility by remember { mutableStateOf("public") } // "public" veya "private"
    var visibleToList by remember { mutableStateOf<List<String>>(emptyList()) }
    var showFriendSelector by remember { mutableStateOf(false) }
    val uiState by createPostViewModel.uiState.collectAsState()
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let { imageUri = it }
    }



    LaunchedEffect(key1 = uiState) {
        if (uiState is UploadUiState.Success) {
            Toast.makeText(context, "Post başarıyla paylaşıldı!", Toast.LENGTH_SHORT).show()
            navController.popBackStack()
        } else if (uiState is UploadUiState.Error) {
            Toast.makeText(context, (uiState as UploadUiState.Error).message, Toast.LENGTH_LONG)
                .show()
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent() // GetContent daha yaygındır
    ) { uri ->
        uri?.let { imageUri = it }
    }

    // İzin isteme sonucunu yakalamak için launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // İzin verildiyse galeriyi aç
            galleryLauncher.launch("image/*")
        } else {
            // İzin reddedildiyse kullanıcıya bilgi ver
            Toast.makeText(context, "İzin verilmedi!", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(topBar = {
        TopAppBar(
            title = {
                Text(
                    "Anı Oluştur",
                    Modifier.fillMaxWidth(),
                    fontSize = 21.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )
            }, navigationIcon = {
               // navController.popBackStack()
            }, actions = {
                Spacer(modifier = Modifier.width(48.dp))
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent
            )
        )


    }) { innerPadding ->
        Column(
            Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            AsyncImage(
                model = imageUri,
                contentDescription = "Seçilen Resim",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .clickable {
                        // İzin kontrolü ve galeri açma mantığı
                        val permissionToRequest =
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                Manifest.permission.READ_MEDIA_IMAGES
                            } else {
                                Manifest.permission.READ_EXTERNAL_STORAGE
                            }
                        permissionLauncher.launch(permissionToRequest)
                    },
                contentScale = ContentScale.Crop
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = visibility == "public", onClick = { visibility = "public" })
                Text("Herkese Açık")
                Spacer(Modifier.width(16.dp))
                RadioButton(
                    selected = visibility == "private",
                    onClick = { visibility = "private" })
                Text("Özel (Seçili Kişiler)")
            }
            OutlinedTextField(
                modifier = Modifier.height(100.dp), value = "",
                label = {
                    Text("Açıklama")
                }, onValueChange = { change ->
                    postDescription = change
                })
            if (visibility == "private") {
                Button(onClick = { showFriendSelector = true }) {
                    Text("Kişileri Seç (${visibleToList.size} kişi)")
                }
            }

            // Paylaş Butonu
            Spacer(modifier = Modifier.height(24.dp))

            Button(onClick = {
                // Butona tıklandığında izin kontrolü yap
                // 1. Android sürümüne göre doğru izni belirle
                val permissionToRequest = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    Manifest.permission.READ_MEDIA_IMAGES
                } else {
                    Manifest.permission.READ_EXTERNAL_STORAGE
                }

                // 2. İzin verilmiş mi diye kontrol et
                if (ContextCompat.checkSelfPermission(context, permissionToRequest) == PackageManager.PERMISSION_GRANTED) {
                    // İzin zaten varsa doğrudan galeriyi aç
                    galleryLauncher.launch("image/*")
                } else {
                    // İzin yoksa kullanıcıdan iste
                    permissionLauncher.launch(permissionToRequest)
                }
            }) {
                Text("Galeriden Resim Seç")
            }

                if (uiState is UploadUiState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else {
                    Text("Paylaş")
                }
            }

            // `showFriendSelector` true ise BottomSheet ile arkadaş listesini göster
            if (showFriendSelector) {
                // ... (Arkadaşların Checkbox ile seçildiği bir ModalBottomSheet kodu)
            }

        }

    }




