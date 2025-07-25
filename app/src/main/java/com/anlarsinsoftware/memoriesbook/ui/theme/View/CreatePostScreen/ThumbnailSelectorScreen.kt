package com.anlarsinsoftware.memoriesbook.ui.theme.View.CreatePostScreen

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThumbnailSelectorScreen(
    navController: NavController,
    videoUriString: String
) {
    val videoUri = Uri.parse(videoUriString)
    val context = LocalContext.current

    var thumbnails by remember { mutableStateOf<List<Pair<Long, Bitmap>>>(emptyList()) }
    var selectedThumbnail by remember { mutableStateOf<Bitmap?>(null) }

    var selectedTimeMs by remember { mutableStateOf(0L) }
    // Büyük önizleme için seçilen Bitmap
    var selectedBitmap by remember { mutableStateOf<Bitmap?>(null) }


    LaunchedEffect(videoUri) {
        withContext(Dispatchers.IO) {
            val retriever = MediaMetadataRetriever()
            try {
                retriever.setDataSource(context, videoUri)
                val durationMs = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0
                val frames = mutableListOf<Pair<Long, Bitmap>>()
                for (i in 0 until 10) {
                    val timeMs = durationMs / 10 * i
                    retriever.getFrameAtTime(timeMs * 1000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                        ?.let { frames.add(timeMs to it) }
                }
                thumbnails = frames
                selectedTimeMs = frames.firstOrNull()?.first ?: 0L
                selectedBitmap = frames.firstOrNull()?.second
            } finally {
                retriever.release()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kapak Seç") },
                actions = {
                    IconButton(onClick = {
                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set("selected_thumbnail_time", selectedTimeMs)
                        navController.popBackStack()
                    }) {
                        Icon(Icons.Default.Check, contentDescription = "Onayla")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier.padding(paddingValues).fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            selectedThumbnail?.let {
                Image(
                    bitmap = it.asImageBitmap(),
                    contentDescription = "Seçili Kapak",
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentScale = ContentScale.Crop
                )
            }

            LazyRow(
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                itemsIndexed(thumbnails) { index, (time, bitmap) ->
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Kare $index",
                        modifier = Modifier
                            .size(80.dp)
                            .border(
                                width = 2.dp,
                                color = if (selectedThumbnail == bitmap) MaterialTheme.colorScheme.primary else Color.Transparent
                            )
                            .clickable { selectedThumbnail = bitmap }
                    )
                }
            }
        }
    }
}