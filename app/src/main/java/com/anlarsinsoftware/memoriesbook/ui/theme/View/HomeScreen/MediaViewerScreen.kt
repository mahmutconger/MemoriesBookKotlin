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
    var isImageZoomed by remember { mutableStateOf(false) }
    val post by remember(postId) {
        derivedStateOf {
            homeViewModel.posts.value.find { it.documentId == postId }
        }
    }

    Scaffold(
        containerColor = Color.Black,
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
                        HorizontalPager(
                            state = pagerState,
                            userScrollEnabled = !isImageZoomed
                        ) { page ->
                            ZoomableImage(
                                model = it.mediaUrls[page],
                                contentDescription = "Full Screen Image ${page + 1}",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit,
                                onZoomChanged = { isZoomed ->
                                    isImageZoomed = isZoomed
                                }
                            )
                        }
                    }
                    "video" -> {
                        VideoPlayer(
                            videoUrl = it.mediaUrls.firstOrNull(),
                            modifier = Modifier.fillMaxSize(),
                            autoPlay = true 
                        )
                    }
                }
            }
        }
    }
}