package com.anlarsinsoftware.memoriesbook.ui.theme.View.ProfileScreen // veya PostDetailScreen'in bulunduğu paket

import CommentBottomSheetContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.anlarsinsoftware.memoriesbook.ui.theme.Model.Posts
import com.anlarsinsoftware.memoriesbook.ui.theme.Util.showToast
import com.anlarsinsoftware.memoriesbook.ui.theme.View.CreatePostScreen.FriendSelectorBottomSheet
import com.anlarsinsoftware.memoriesbook.ui.theme.View.HomeScreen.BottomSheetContent
import com.anlarsinsoftware.memoriesbook.ui.theme.View.HomeScreen.EditPostBottomSheet
import com.anlarsinsoftware.memoriesbook.ui.theme.View.HomeScreen.FullScreenVideoPlayer
import com.anlarsinsoftware.memoriesbook.ui.theme.View.HomeScreen.HomeLazyItems.LikersItem
import com.anlarsinsoftware.memoriesbook.ui.theme.View.HomeScreen.HomeLazyItems.PostItem
import com.anlarsinsoftware.memoriesbook.ui.theme.ViewModel.*
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostDetailScreen(
    navController: NavController,
    postDetailViewModel: PostDetailViewModel,
    homeViewModel: HomeViewModel,
    commentsViewModel: CommentsViewModel,
    userViewModel: UserViewModel
) {
    // --- STATE'LER VE DEĞİŞKENLER ---
    val post by postDetailViewModel.post.collectAsState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val currentUser = Firebase.auth.currentUser

    // Açılır pencereler için state'ler
    val optionsSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val commentSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val likersSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val editPostSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val visibilitySheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Görünürlükleri yöneten Boolean state'ler
    var showOptionsSheet by remember { mutableStateOf(false) }
    var showCommentSheet by remember { mutableStateOf(false) }
    var showLikersSheet by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showEditPostSheet by remember { mutableStateOf(false) }
    var showVisibilitySheet by remember { mutableStateOf(false) }
    var fullScreenVideoUrl by remember { mutableStateOf<String?>(null) }

    val comments by commentsViewModel.comments.collectAsState()
    val likers by homeViewModel.postLikers.collectAsState()
    val friends by userViewModel.myFriends.collectAsState()
    val followers by userViewModel.myFollowers.collectAsState()

    // --- ANA ARAYÜZ (SCAFFOLD) ---
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(post?.authorUsername ?: "Gönderi") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            contentAlignment = Alignment.Center
        ) {
            if (post == null) {
                CircularProgressIndicator()
            } else {
                PostItem(
                    post = post!!,
                    onMenuClick = { showOptionsSheet = true },
                    onLikeClick = { homeViewModel.onPostLikeClicked(post!!) },
                    onCommentClick = {
                        commentsViewModel.fetchCommentsForPost(post!!.documentId)
                        showCommentSheet = true
                    },
                    onShowLikersClick = {
                        homeViewModel.fetchLikersForPost(post!!)
                        showLikersSheet = true
                    },
                    onMediaClick = { p, index -> navController.navigate("media_viewer/${p.documentId}/$index") },
                    onVideoFullScreen = { videoUrl -> fullScreenVideoUrl = videoUrl },
                    contentScale = ContentScale.Fit,
                    onAuthorClick = { authorId ->
                        val route = if (authorId == currentUser?.uid) {
                            "profile_screen/me"
                        } else {
                            "profile_screen/$authorId"
                        }
                        navController.navigate(route)
                    }
                )
            }
        }
    }

    if (showOptionsSheet) {
        ModalBottomSheet(
            onDismissRequest = { showOptionsSheet = false },
            sheetState = optionsSheetState
        ) {
            post?.let {
                BottomSheetContent(
                    post = it,
                    currentUserId = currentUser?.uid,
                    onHide = {
                        scope.launch { optionsSheetState.hide() }
                            .invokeOnCompletion { showOptionsSheet = false }
                    },
                    onDeleteClick = {
                        scope.launch { optionsSheetState.hide() }.invokeOnCompletion {
                            showOptionsSheet = false
                            showDeleteDialog = true
                        }
                    },
                    onEditClick = {
                        scope.launch { optionsSheetState.hide() }.invokeOnCompletion {
                            showOptionsSheet = false
                            showEditPostSheet = true
                        }
                    },
                    onSetVisibilityClick = {
                        scope.launch { optionsSheetState.hide() }.invokeOnCompletion {
                            showOptionsSheet = false
                            showVisibilitySheet = true
                        }
                    },
                    onAddToFavoritesClick = { homeViewModel.addToFavorites(it.documentId) }
                )
            }
        }
    }

    // Yorumlar
    if (showCommentSheet) {
        ModalBottomSheet(
            onDismissRequest = { showCommentSheet = false },
            sheetState = commentSheetState
        ) {
            post?.let {
                CommentBottomSheetContent(
                    post = it,
                    commentList = comments,
                    onCommentLikeClicked = { likedComment ->
                        commentsViewModel.onCommentLikeClicked(likedComment)
                    },
                    onAddCommentClicked = { commentText ->
                        commentsViewModel.addComment(it.documentId, commentText)
                    },
                    onHide = {
                        scope.launch { commentSheetState.hide() }
                    }
                )
            }
        }
    }

    // Beğenenler Listesi
    if (showLikersSheet) {
        ModalBottomSheet(
            onDismissRequest = { showLikersSheet = false },
            sheetState = likersSheetState
        ) {
            Text(
                "Beğenenler",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(16.dp)
            )
            LazyColumn(contentPadding = PaddingValues(16.dp)) {
                items(items = likers) { liker ->
                    LikersItem(photoUrl = liker.photoUrl, userName = liker.username)
                }
            }
        }
    }

    // Post Düzenleme
    if (showEditPostSheet) {
        ModalBottomSheet(
            onDismissRequest = { showEditPostSheet = false },
            sheetState = editPostSheetState
        ) {
            post?.let { postToEdit ->
                EditPostBottomSheet(
                    post = postToEdit,
                    onDismiss = { showEditPostSheet = false },
                    onSaveClicked = { newComment ->
                        homeViewModel.updatePostComment(
                            postToEdit.documentId,
                            newComment
                        ) { success ->
                            if (success) showToast(context, "Açıklama güncellendi.")
                            else showToast(context, "Hata: Açıklama güncellenemedi.")
                        }
                        showEditPostSheet = false
                    }
                )
            }
        }
    }

    // Görünürlük Ayarlama
    if (showVisibilitySheet) {
        post?.let { postToEdit ->
            FriendSelectorBottomSheet(
                friends = friends,
                followers = followers,
                initiallySelectedIds = postToEdit.visibleTo,
                onDismiss = { showVisibilitySheet = false },
                onSelectionDone = { selectedIds ->
                    homeViewModel.updatePostVisibility(
                        postToEdit.documentId,
                        "private",
                        selectedIds
                    ) { success ->
                        if (success) showToast(context, "Görünürlük ayarları güncellendi.")
                    }
                    showVisibilitySheet = false
                }
            )
        }
    }

    // Silme Onay Diyaloğu
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Postu Sil") },
            text = { Text("Bu postu kalıcı olarak silmek istediğinizden emin misiniz?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        post?.let {
                            homeViewModel.deletePost(it.documentId, context)
                            showDeleteDialog = false
                            navController.popBackStack()
                        }
                    }
                ) { Text("Evet, Sil") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("İptal") }
            }
        )
    }

    fullScreenVideoUrl?.let { url ->
        FullScreenVideoPlayer(videoUrl = url, onDismiss = { fullScreenVideoUrl = null })
    }
}