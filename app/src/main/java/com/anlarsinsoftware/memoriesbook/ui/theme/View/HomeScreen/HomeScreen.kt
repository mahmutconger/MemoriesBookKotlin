import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.anlarsinsoftware.memoriesbook.R
import com.anlarsinsoftware.memoriesbook.ui.theme.Model.Posts
import com.anlarsinsoftware.memoriesbook.ui.theme.Tools.BottomNavigationBar
import com.anlarsinsoftware.memoriesbook.ui.theme.Tools.ExpandableText
import com.anlarsinsoftware.memoriesbook.ui.theme.Tools.MyScaffold
import com.anlarsinsoftware.memoriesbook.ui.theme.Tools.myIconButtonPainter
import com.anlarsinsoftware.memoriesbook.ui.theme.Tools.mySpacer
import com.anlarsinsoftware.memoriesbook.ui.theme.Tools.rememberFormattedTimestamp
import com.anlarsinsoftware.memoriesbook.ui.theme.Tools.showToast
import com.anlarsinsoftware.memoriesbook.ui.theme.View.CreatePostScreen.FriendSelectorBottomSheet
import com.anlarsinsoftware.memoriesbook.ui.theme.View.HomeScreen.BottomSheetContent
import com.anlarsinsoftware.memoriesbook.ui.theme.View.HomeScreen.EditPostBottomSheet
import com.anlarsinsoftware.memoriesbook.ui.theme.View.HomeScreen.FullScreenVideoPlayer
import com.anlarsinsoftware.memoriesbook.ui.theme.View.HomeScreen.HomeLazyItems.LikersItem
import com.anlarsinsoftware.memoriesbook.ui.theme.View.HomeScreen.HomeLazyItems.PostItem
import com.anlarsinsoftware.memoriesbook.ui.theme.ViewModel.CommentsViewModel
import com.anlarsinsoftware.memoriesbook.ui.theme.ViewModel.ConnectionsViewModel
import com.anlarsinsoftware.memoriesbook.ui.theme.ViewModel.HomeViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    homeViewModel: HomeViewModel,
    commentsViewModel: CommentsViewModel,
    connectionsViewModel: ConnectionsViewModel
) {
    val postList by homeViewModel.posts.collectAsState()
    val likers by homeViewModel.postLikers.collectAsState()
    val friends by connectionsViewModel.friends.collectAsState()
    val followers by connectionsViewModel.followers.collectAsState()
    var fullScreenVideoUrl by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val optionsSheetState = rememberModalBottomSheetState()
    val commentSheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    var selectedPost by remember { mutableStateOf<Posts?>(null) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    val editPostSheetState = rememberModalBottomSheetState()
    var showEditPostSheet by remember { mutableStateOf(false) }
    var showVisibilitySheet by remember { mutableStateOf(false) }
    val comments by commentsViewModel.comments.collectAsState()
    var showLikersSheet by remember { mutableStateOf(false) }
    val showLikersSheetState = rememberModalBottomSheetState()


    val currentUser = FirebaseAuth.getInstance().currentUser


    MyScaffold(
        titleText = "Memories Book",
        navController = navController,
        context = context,
        navigationContent = {
        },
        actionIconContent = {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    tint = MaterialTheme.colorScheme.primary,
                    contentDescription = "Menu ikonu"
                )
            }
        }) {

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
        ) {
            items(items = postList) { post ->
                PostItem(
                    post = post,
                    onMenuClick = {
                        selectedPost = post
                        scope.launch { optionsSheetState.show() }
                    },
                    onLikeClick = {
                        homeViewModel.onPostLikeClicked(post)
                    },
                    onShowLikersClick = {
                        homeViewModel.fetchLikersForPost(post)
                        showLikersSheet = true
                    },
                    onCommentClick = {
                        Log.d("CommentClick", "Yorumlar için tıklandı. Post ID: ${post.documentId}")
                        commentsViewModel.fetchCommentsForPost(post.documentId)
                        selectedPost = post
                        scope.launch { commentSheetState.show() }
                    }, onMediaClick = { clickedPost, startIndex ->
                        navController.navigate("media_viewer/${clickedPost.documentId}/${startIndex}")
                    }, onVideoFullScreen = { videoUrl ->
                        fullScreenVideoUrl = videoUrl
                    }
                )
            }
        }
    }

    if (showVisibilitySheet) {
        selectedPost?.let { post ->
            FriendSelectorBottomSheet(
                friends = friends,
                followers = followers,
                initiallySelectedIds = post.visibleTo,
                onDismiss = { showVisibilitySheet = false },
                onSelectionDone = { selectedIds ->
                    homeViewModel.updatePostVisibility(
                        post.documentId,
                        "private",
                        selectedIds
                    ) { success ->
                        if (success) {
                            showToast(context, "Görünürlük ayarları güncellendi.")
                        }
                    }
                    showVisibilitySheet = false
                }
            )
        }
    }

    if (showLikersSheet) {
        selectedPost?.let { posts ->
            ModalBottomSheet(
                sheetState = showLikersSheetState,
                onDismissRequest = {
                    showLikersSheet = false
                }
            ) {
                LazyColumn(contentPadding = PaddingValues(16.dp)) {
                    items(items = likers) { liker ->
                        LikersItem(liker.photoUrl, liker.username)
                    }
                }
            }

        }
    }

    fullScreenVideoUrl?.let { url ->
        FullScreenVideoPlayer(
            videoUrl = url,
            onDismiss = {
                fullScreenVideoUrl = null
            }
        )
    }

    if (optionsSheetState.isVisible) {
        ModalBottomSheet(
            sheetState = optionsSheetState,
            onDismissRequest = {
                scope.launch { optionsSheetState.hide() }
            }
        ) {
            selectedPost?.let { post ->
                BottomSheetContent(
                    post = post,
                    onHide = {
                        scope.launch { optionsSheetState.hide() }
                    },
                    currentUserId = currentUser?.uid,
                    onDeleteClick = {
                        scope.launch { optionsSheetState.hide() }.invokeOnCompletion {
                            showDeleteConfirmDialog = true
                        }
                    },
                    onEditClick = {
                        scope.launch { optionsSheetState.hide() }.invokeOnCompletion {
                            showEditPostSheet = true
                        }
                    },
                    onSetVisibilityClick = {
                        scope.launch { optionsSheetState.hide() }.invokeOnCompletion {
                            showVisibilitySheet = true
                        }
                    },
                    onAddToFavoritesClick = {
                        homeViewModel.addToFavorites(post.documentId)
                        scope.launch { optionsSheetState.hide() }
                    }
                )
            }
        }
    }

    if (showEditPostSheet) {
        ModalBottomSheet(
            sheetState = editPostSheetState,
            onDismissRequest = { showEditPostSheet = false }
        ) {
            selectedPost?.let { post ->
                EditPostBottomSheet(
                    post = post,
                    onDismiss = { showEditPostSheet = false },
                    onSaveClicked = { newComment ->
                        homeViewModel.updatePostComment(post.documentId, newComment) { success ->
                            if (success) {
                                showToast(context, "Açıklama güncellendi.")
                            } else {
                                showToast(context, "Hata: Açıklama güncellenemedi.")
                            }
                        }
                        showEditPostSheet = false
                    }
                )
            }
        }
    }

    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = {
                showDeleteConfirmDialog = false
            },
            title = {
                Text("Postu Sil")
            },
            text = {
                Text("Bu postu kalıcı olarak silmek istediğinizden emin misiniz? Bu işlem geri alınamaz.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        selectedPost?.let { postToDelete ->
                            homeViewModel.deletePost(postToDelete.documentId, context)
                        }
                        showDeleteConfirmDialog = false
                    }
                ) {
                    Text("Evet, Sil")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmDialog = false
                    }
                ) {
                    Text("İptal")
                }
            }
        )
    }

    if (commentSheetState.isVisible) {
        ModalBottomSheet(
            sheetState = commentSheetState,
            onDismissRequest = { scope.launch { commentSheetState.hide() } }
        ) {
            selectedPost?.let { post ->

                CommentBottomSheetContent(
                    post = post,
                    commentList = comments,
                    onCommentLikeClicked = { likedComment ->
                        commentsViewModel.onCommentLikeClicked(likedComment)
                    },
                    onAddCommentClicked = { commentText ->
                        commentsViewModel.addComment(post.documentId, commentText)
                    },
                    onHide = {
                        scope.launch { commentSheetState.hide() }
                    }
                )
            }
        }
    }
}


@Composable
fun VideoPlayer(
    videoUrl: String?,
    modifier: Modifier = Modifier,
    autoPlay: Boolean = false
) {
    if (videoUrl == null) return

    val context = LocalContext.current

    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(videoUrl))
            playWhenReady = autoPlay
            prepare()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }

    AndroidView(
        factory = {
            PlayerView(it).apply {
                player = exoPlayer
                // Kontrollerin her zaman görünmesini sağlamak için (isteğe bağlı)
                // controllerShowTimeoutMs = -1
            }
        },
        modifier = modifier
    )
}



