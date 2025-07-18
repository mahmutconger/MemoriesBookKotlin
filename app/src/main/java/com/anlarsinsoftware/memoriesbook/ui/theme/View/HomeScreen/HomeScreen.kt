import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.anlarsinsoftware.memoriesbook.R
import com.anlarsinsoftware.memoriesbook.ui.theme.Model.Comments
import com.anlarsinsoftware.memoriesbook.ui.theme.Model.Posts
import com.anlarsinsoftware.memoriesbook.ui.theme.Tools.BottomNavigationBar
import com.anlarsinsoftware.memoriesbook.ui.theme.Tools.myImageButton
import com.anlarsinsoftware.memoriesbook.ui.theme.Tools.mySpacer
import com.anlarsinsoftware.memoriesbook.ui.theme.Tools.showToast
import com.anlarsinsoftware.memoriesbook.ui.theme.Util.url1
import com.anlarsinsoftware.memoriesbook.ui.theme.View.CreatePostScreen.FriendSelectorBottomSheet
import com.anlarsinsoftware.memoriesbook.ui.theme.View.HomeScreen.BottomSheetContent
import com.anlarsinsoftware.memoriesbook.ui.theme.View.HomeScreen.EditPostBottomSheet
import com.anlarsinsoftware.memoriesbook.ui.theme.ViewModel.CommentsViewModel
import com.anlarsinsoftware.memoriesbook.ui.theme.ViewModel.ConnectionsViewModel
import com.anlarsinsoftware.memoriesbook.ui.theme.ViewModel.HomeViewModel
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    homeViewModel: HomeViewModel,
    commentsViewModel: CommentsViewModel,
    connectionsViewModel: ConnectionsViewModel,
    postList: List<Posts>,
    commentList: List<Comments>,
    onPostLikeClicked: (Posts) -> Unit,
    onCommentLikeClicked: (Comments) -> Unit
) {
    val context = LocalContext.current
    val optionsSheetState = rememberModalBottomSheetState()
    val commentSheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    var selectedPost by remember { mutableStateOf<Posts?>(null) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    val editPostSheetState = rememberModalBottomSheetState()
    var showEditPostSheet by remember { mutableStateOf(false) }
    val friends by connectionsViewModel.friends.collectAsState()
    val followers by connectionsViewModel.followers.collectAsState()
    var showVisibilitySheet by remember { mutableStateOf(false) }


    val currentUser = FirebaseAuth.getInstance().currentUser

    Scaffold(
        topBar = {
            TopAppBar(title = {
                Text("Memories Book", fontWeight = FontWeight.Medium)
            }, actions = {
                IconButton(onClick = {}) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        tint = MaterialTheme.colorScheme.primary,
                        contentDescription = "Seçenekler Menüsü"
                    )
                }
            })
        },
        bottomBar = {
            BottomNavigationBar(
                context = context,
                createPostClick = {
                    navController.navigate("createPost_screen")
                },
                profileClick = {
                    navController.navigate("profile_screen")
                }, messageClick = {
                    navController.navigate("messages_screen")
                }, homeClick = {

                }, homeTint = MaterialTheme.colorScheme.primary
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            items(items = postList) { post ->
                PostItem(
                    post = post,
                    onMenuClick = {
                        selectedPost = post
                        scope.launch { optionsSheetState.show() }
                    },
                    onLikeClick = {
                        onPostLikeClicked(post)
                    },
                    onCommentClick = {
                        Log.d("CommentClick", "Yorumlar için tıklandı. Post ID: ${post.documentId}")
                        commentsViewModel.fetchCommentsForPost(post.documentId)
                        selectedPost = post
                        scope.launch { commentSheetState.show() }
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
                            // Kapandıktan sonra onay diyaloğunu göster
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
                        showEditPostSheet = false // Kaydettikten sonra kapat
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
                    commentList = commentList,
                    onCommentLikeClicked = { likedComment ->
                        selectedPost?.let { post ->
                            onCommentLikeClicked(likedComment)
                        }
                    },
                    onHide = { scope.launch { commentSheetState.hide() } }
                )
            }
        }
    }
}

@Composable
fun PostItem(
    post: Posts,
    onMenuClick: () -> Unit,
    onLikeClick: () -> Unit,
    onCommentClick: () -> Unit
) {
    Card(
        modifier = Modifier

            .fillMaxWidth()

            .padding(horizontal = 16.dp, vertical = 8.dp),

        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth()) {
                Text(text = post.useremail, fontWeight = FontWeight.Bold)
                Spacer(Modifier.weight(1f))
                myImageButton(
                    R.drawable.menu_vertical,
                    imageSize = 20,
                    onClick = onMenuClick,
                    tintColor = MaterialTheme.colorScheme.primary
                )
            }
            val formattedDate = remember(post.date) {
                val sdf = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
                sdf.format(post.date?.toDate())
            }

            Text(
                formattedDate,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color.LightGray
            )
            Spacer(modifier = Modifier.height(8.dp))
            AsyncImage(
                model = post.downloadUrl,
                contentDescription = "Post Image",
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = post.comment)
            mySpacer(10)

            Row(Modifier.fillMaxWidth()) {
                val likeIcon =
                    if (post.isLiked) R.drawable.like_selected else R.drawable.like_unselected

                myImageButton(
                    id = likeIcon,
                    imageSize = 25,
                    onClick = onLikeClick,
                    tintColor = MaterialTheme.colorScheme.primary
                )

                Spacer(Modifier.width(15.dp))
                myImageButton(
                    R.drawable.comment_icon,
                    25,
                    tintColor = MaterialTheme.colorScheme.primary,
                    onClick = onCommentClick
                )
            }

        }
    }
}


@Preview(showBackground = true)
@Composable
fun prev_Home() {
    val post1 = Posts("mahmutconger@gmail.com", "06.07.2025", "Bu ilk post ", url1, Timestamp.now())
    val post2 = Posts("user@test.com", "06.07.2025", "Bu da ikinci postum!", url1, Timestamp.now())
    val postList = arrayListOf(post1, post2)
    val comment5 = Comments("bgun daha ne yapacağızz", Timestamp.now(), "as Conger", "1", "1")
    val commentList = arrayListOf(comment5)

    val homeViewModel: HomeViewModel = viewModel()
    val commentsViewModel: CommentsViewModel = viewModel()
    val connectionsViewModel: ConnectionsViewModel = viewModel()

    HomeScreen(
        rememberNavController(),
        homeViewModel,
        commentsViewModel,
        connectionsViewModel,
        commentList = commentList,
        onPostLikeClicked = {},
        onCommentLikeClicked = {},
        postList = postList
    )
}

