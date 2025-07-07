import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.anlarsinsoftware.memoriesbook.R
import com.anlarsinsoftware.memoriesbook.ui.theme.Model.Comments
import com.anlarsinsoftware.memoriesbook.ui.theme.Model.Posts
import com.anlarsinsoftware.memoriesbook.ui.theme.Tools.myImageButton
import com.anlarsinsoftware.memoriesbook.ui.theme.Tools.mySpacer
import com.anlarsinsoftware.memoriesbook.ui.theme.Tools.showToast
import com.anlarsinsoftware.memoriesbook.ui.theme.Util.url1
import com.anlarsinsoftware.memoriesbook.ui.theme.View.HomeScreen.BottomSheetContent
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    postList: List<Posts>,
    commentList: List<Comments>
) {
    val context = LocalContext.current
    val optionsSheetState = rememberModalBottomSheetState()
    val commentSheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    var selectedPost by remember { mutableStateOf<Posts?>(null) }

    var posts by remember { mutableStateOf(postList) }
    var comments by remember { mutableStateOf(commentList) }

    Scaffold(
        topBar = {
            TopAppBar(onOptionsMenuClick = { showToast(context, "Options Tıklandı") })
        },
        bottomBar = {
            BottomNavigationBar(context = context, createPostClick = {
                navController.navigate("createPost_screen")
            })
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            items(items = posts) { post ->
                PostItem(
                    post = post,
                    onMenuClick = {
                        selectedPost = post
                        scope.launch { optionsSheetState.show() }
                    },
                    onLikeClick = {
                        posts = posts.map { p ->
                            if (p.documentId.equals(post.documentId)) {
                                p.copy(isLiked = !p.isLiked)
                            } else {
                                p
                            }
                        }
                    }, onCommentClick = {
                        selectedPost = post
                        scope.launch { commentSheetState.show() }
                    }
                )
            }
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
                    }
                )
            }
        }
    }

    if (commentSheetState.isVisible) {
        ModalBottomSheet(
            sheetState = commentSheetState,
            onDismissRequest = { scope.launch { commentSheetState.hide() } }
        ) {
            selectedPost?.let { post ->
                // 1. Yorumları burada, göndermeden önce FİLTRELİYORUZ.
                val commentsForPost = comments.filter { it.documentId == post.documentId }

                CommentBottomSheetContent(
                    post = post,
                    // 2. Sadece filtrelenmiş listeyi gönderiyoruz.
                    commentList = commentsForPost,
                    onLikeClick = { likedComment ->
                        comments = comments.map { c ->
                            if (c.documentId == likedComment.documentId) {
                                c.copy(isLiked = !c.isLiked)
                            } else {
                                c
                            }
                        }
                    },
                    onHide = { scope.launch { commentSheetState.hide() } }
                )
            }
        }
    }
}

@Composable
fun PostItem(post: Posts, onMenuClick: () -> Unit,onLikeClick:()->Unit,onCommentClick:()->Unit) {
    Card(
        modifier = Modifier

            .fillMaxWidth()

            .padding(horizontal = 16.dp, vertical = 8.dp),

        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth()) {
                Text(text = post.email, fontWeight = FontWeight.Bold)
                Spacer(Modifier.weight(1f))
                myImageButton(
                    R.drawable.menu_vertical,
                    imageSize = 20,
                    onClick = onMenuClick
                )
            }
            Text(post.date)
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
                val likeIcon = if (post.isLiked) R.drawable.like_selected else R.drawable.like_unselected

                myImageButton(
                    id = likeIcon,
                    imageSize = 25,
                    onClick = onLikeClick
                )

                Spacer(Modifier.width(15.dp))
                myImageButton(R.drawable.comment_icon, 25, Color.Black, onClick = onCommentClick)
            }

        }
    }
}

@Composable
fun TopAppBar(onOptionsMenuClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "Memories Book",
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        Spacer(modifier = Modifier.weight(1f))
        myImageButton(id = R.drawable.options_ico, onClick = onOptionsMenuClick)
    }
}

@Composable
fun BottomNavigationBar(context: Context,createPostClick:()->Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        myImageButton(id = R.drawable.home_icon, tintColor = Color.Black) {
            showToast(
                context,
                "Home Tıklandı"
            )
        }
        myImageButton(id = R.drawable.person_ico, tintColor = Color.Black) {
            showToast(
                context,
                "Person Tıklandı"
            )
        }
        myImageButton(id = R.drawable.add_post, tintColor = Color.Black) {
            createPostClick()
        }
        myImageButton(id = R.drawable.message_circle2, tintColor = Color.Black) {
            showToast(
                context,
                "Message Tıklandı"
            )
        }
    }
}


@Preview(showBackground = true)
@Composable
fun prev_Home() {
    val post1 = Posts("mahmutconger@gmail.com", "06.07.2025","Bu ilk post ", url1, "1")
    val post2 = Posts("user@test.com", "06.07.2025","Bu da ikinci postum!", url1, "2")
    val postList = arrayListOf(post1, post2)
    val comment5=Comments("bgun daha ne yapacağızz","25.11.2021","as Conger","1","1")
    val commentList= arrayListOf(comment5)
    HomeScreen(rememberNavController(),postList, commentList =commentList )
}

