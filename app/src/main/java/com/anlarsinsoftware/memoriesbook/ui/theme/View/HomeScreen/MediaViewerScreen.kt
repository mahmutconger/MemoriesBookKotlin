package com.anlarsinsoftware.memoriesbook.ui.theme.View.HomeScreen

import VideoPlayer

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.anlarsinsoftware.memoriesbook.ui.theme.ViewModel.HomeViewModel

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MediaViewerScreen(
    navController: NavController,
    homeViewModel: HomeViewModel,
    postId: String,
    initialIndex: Int
) {
    // ViewModel'deki post listesinden doğru postu bul
    val post by remember(postId) {
        derivedStateOf {
            homeViewModel.posts.value.find { it.documentId == postId }
        }
    }

    Scaffold(
        containerColor = Color.Black, // Tam ekran için arkaplan siyah
        topBar = {
            TopAppBar(
                title = { Text(post?.useremail ?: "", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            post?.let {
                when (it.mediaType) {
                    "image" -> {
                        val pagerState = rememberPagerState(
                            initialPage = initialIndex,
                            pageCount = { it.mediaUrls.size }
                        )
                        HorizontalPager(state = pagerState) { page ->
                            AsyncImage(
                                model = it.mediaUrls[page],
                                contentDescription = "Full Screen Image ${page + 1}",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit // Tam ekranda resmi sığdır
                            )
                        }
                    }
                    "video" -> {
                        VideoPlayer(videoUrl = it.mediaUrls.firstOrNull(), modifier = Modifier.fillMaxSize())
                    }
                }
            }
        }
    }
}