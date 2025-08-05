import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.anlarsinsoftware.memoriesbook.R
import com.anlarsinsoftware.memoriesbook.ui.theme.Util.myIconButton
import com.google.firebase.auth.FirebaseAuth
import com.anlarsinsoftware.memoriesbook.ui.theme.Model.PendingRequest
import com.anlarsinsoftware.memoriesbook.ui.theme.Model.Posts
import com.anlarsinsoftware.memoriesbook.ui.theme.Model.Users
import com.anlarsinsoftware.memoriesbook.ui.theme.Util.ProfileScaffold
import com.anlarsinsoftware.memoriesbook.ui.theme.Util.showToast
import com.anlarsinsoftware.memoriesbook.ui.theme.View.ProfileScreen.ProfileHeader
import com.anlarsinsoftware.memoriesbook.ui.theme.ViewModel.ConnectionsViewModel
import com.anlarsinsoftware.memoriesbook.ui.theme.ViewModel.ProfileViewModel
import com.anlarsinsoftware.memoriesbook.ui.theme.ViewModel.UpdateProfileUiState
import com.anlarsinsoftware.memoriesbook.ui.theme.ViewModel.UserViewModel
import kotlinx.coroutines.launch

private enum class PostTab(val title: String) { PUBLIC("Herkese Açık"), PRIVATE("Özel") }


@SuppressLint("CoroutineCreationDuringComposition")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    profileViewModel: ProfileViewModel,
    connectionsViewModel: ConnectionsViewModel,
) {

    var openedSheetIndex by remember { mutableStateOf<Int?>(null) }
    val uiState by profileViewModel.uiState.collectAsState()
    var selectedTab by remember { mutableStateOf(PostTab.PUBLIC) }
    val sheetState = rememberModalBottomSheetState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()


    val friendsreqSheetState = rememberModalBottomSheetState()
    val editProfileSheetState = rememberModalBottomSheetState()

    val requests by connectionsViewModel.pendingRequests.collectAsState()

    LaunchedEffect(uiState.updateProfileState) {
        val updateState = uiState.updateProfileState
        if (updateState is UpdateProfileUiState.Success) {
            showToast(context, "Profil başarıyla güncellendi!")
            scope.launch { editProfileSheetState.hide() }.invokeOnCompletion {
                profileViewModel.resetUpdateState() // Doğru ViewModel'i çağırıyoruz.
            }
        } else if (updateState is UpdateProfileUiState.Error) {
            showToast(context, updateState.message, isLengthLong = true)
            profileViewModel.resetUpdateState() // Doğru ViewModel'i çağırıyoruz.
        }
    }

    val gridState = rememberLazyGridState()
    LaunchedEffect(gridState, selectedTab) {
        snapshotFlow { gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .collect { lastIndex ->
                val list = if (selectedTab == PostTab.PUBLIC) uiState.publicPosts else uiState.privatePosts
                val isLoadingMore = if (selectedTab == PostTab.PUBLIC) uiState.isLoadingMorePublicPosts else uiState.isLoadingMorePrivatePosts
                val isEnded = if (selectedTab == PostTab.PUBLIC) uiState.publicPostsEnded else uiState.privatePostsEnded

                if (lastIndex != null && list.isNotEmpty() &&
                    lastIndex >= list.size - 6 && !isLoadingMore && !isEnded) {
                    if (selectedTab == PostTab.PUBLIC) {
                        profileViewModel.loadMorePublicPosts()
                    } else {
                        profileViewModel.loadMorePrivatePosts()
                    }
                }
            }
    }

    ProfileScaffold(
        navController,
        context,
        onClickNotify = {
            scope.launch { friendsreqSheetState.show() }
        }
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else{
                ProfileHeader(
                    user = uiState.profileUser,
                    followersCount = uiState.followers.size,
                    followingCount = uiState.following.size,
                    friendsCount = uiState.friends.size,
                    isCurrentUserProfile = uiState.isCurrentUserProfile,
                    onFollowersClick = { openedSheetIndex = 0 },
                    onFollowingClick = { openedSheetIndex = 1 },
                    onFriendsClick = { openedSheetIndex = 2 },
                    onEditProfileClick = { scope.launch { editProfileSheetState.show() } }
                )
        }
            TabRow(selectedTabIndex = selectedTab.ordinal) {
                if (uiState.isCurrentUserProfile) {
                    PostTab.values().forEach { tab ->
                        Tab(
                            selected = selectedTab == tab,
                            onClick = { selectedTab = tab },
                            text = { Text(tab.title) }
                        )
                    }
                } else {
                    Tab(selected = true, onClick = {}, text = { Text(PostTab.PUBLIC.title) })
                }
            }

            val listToShow = if (selectedTab == PostTab.PUBLIC) uiState.publicPosts else uiState.privatePosts
            val isLoadingMore = if (selectedTab == PostTab.PUBLIC) uiState.isLoadingMorePublicPosts else uiState.isLoadingMorePrivatePosts


            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                state = gridState,
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                items(items = listToShow, key = { it.documentId }) { post ->
                    ProfileGridItem(
                        post = post,
                        onClick = {
                            navController.navigate("post_detail/${post.documentId}")
                        }
                    )
                }
                if (isLoadingMore) {
                    item(span = { GridItemSpan(3) }) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }
            }
        }
    }






    if (editProfileSheetState.isVisible) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        ModalBottomSheet(
            sheetState = editProfileSheetState,
            onDismissRequest = { scope.launch { editProfileSheetState.hide() } }
        ) {
            EditProfileContent(
                currentUsername = currentUser?.displayName ?: "",
                currentPhotoUri = currentUser?.photoUrl,
                uiState = uiState.updateProfileState,
                onSave = { newName, newImageUri ->
                  profileViewModel.updateUserProfile(newName,newImageUri)
                }
            )
        }
    }

    if (openedSheetIndex != null) {
        ModalBottomSheet(
            sheetState = sheetState,
            onDismissRequest = { openedSheetIndex = null }
        ) {
            SwipeableFollowerScreen(
                followers = uiState.followers,
                following = uiState.following,
                friends = uiState.friends,
                initialTabIndex = openedSheetIndex ?: 0
            )
        }
    }

    if (friendsreqSheetState.isVisible) {
        ModalBottomSheet(
            sheetState = friendsreqSheetState,
            onDismissRequest = {
                scope.launch { friendsreqSheetState.hide() }
            }
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Text(
                        "Takip istekleri",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                items(items = requests) { request ->
                    UserItemForRequests(
                        user = request,
                        context,
                        onAccept = {
                            connectionsViewModel.acceptFriendRequest(request)
                        },
                        onDecline = {
                            connectionsViewModel.declineFriendRequest(request.requestId)
                        }
                    )
                }
            }
        }
    }

}

@Composable
fun UserItem(user: Users) {
    Row(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = user.photoUrl,
            contentDescription = "${user.username} profil fotoğrafı",
            error = painterResource(id = R.drawable.default_user),
            placeholder = painterResource(id = R.drawable.default_user),
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(user.username, fontWeight = FontWeight.Bold)
            Text(user.email, style = MaterialTheme.typography.bodySmall)
        }
    }

}

@Composable
fun UserItemForRequests(
    user: PendingRequest,
    context: Context,
    onAccept: () -> Unit,
    onDecline: () -> Unit
) {
    val senderUser = user.senderProfile
    Row(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                "${senderUser.username} sizi takip etmek istiyor.",
                fontWeight = FontWeight.Bold
            )
            Text(senderUser.email, style = MaterialTheme.typography.bodySmall)
        }
        myIconButton(Icons.Default.Check) {
            showToast(context = context, "${senderUser.username} artık sizi takip ediyor.")
            onAccept()
        }
        myIconButton(Icons.Default.Close) {
            showToast(context = context, "${senderUser.username} İsteği reddedildi")
            onDecline()
        }
    }

}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SwipeableFollowerScreen(
    followers: List<Users>,
    following: List<Users>,
    friends: List<Users>,
    initialTabIndex: Int = 0
) {

    val tabItems = listOf("Takipçiler", "Takip Edilenler", "Arkadaşlar")
    val pagerState = rememberPagerState(initialPage = initialTabIndex) { tabItems.size }
    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(
            selectedTabIndex =pagerState.currentPage
        ) {
            tabItems.forEachIndexed { index, title ->
                Tab(
                    selected = pagerState.currentPage == index,
                    onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(index)
                        }
                    },
                    text = { Text(title) }
                )
            }
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) { pageIndex ->
            when (pageIndex) {
                0 -> UserList(users = followers, emptyMessage = "Hiç takipçin yok.")
                1 -> UserList(users = following, emptyMessage = "Kimseyi takip etmiyorsun.")
                2 -> UserList(users = friends, emptyMessage = "Hiç arkadaşın yok.")
            }
        }
    }
}

@Composable
fun UserList(
    users: List<Users>,
    emptyMessage: String
) {
    // DEĞİŞİKLİK BURADA: Listeyi LazyColumn'a vermeden önce filtreliyoruz.
    val validUsers = users.filter { it.uid.isNotBlank() }

    if (validUsers.isEmpty()) { // Filtrelenmiş listeyi kontrol et
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(emptyMessage)
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Filtrelenmiş ve güvenli listeyi kullan
            items(items = validUsers, key = { it.uid }) { user ->
                UserItem(user = user)
            }
        }
    }
}


@Composable
fun EditProfileContent(
    currentUsername: String,
    currentPhotoUri: Uri?,
    uiState: UpdateProfileUiState,
    onSave: (String, Uri?) -> Unit
) {
    var newImageUri by remember { mutableStateOf<Uri?>(null) }
    var newUsername by remember { mutableStateOf(currentUsername) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> uri?.let { newImageUri = it } }
    )

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Profili Düzenle", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))

        AsyncImage(
            model = newImageUri ?: currentPhotoUri,
            contentDescription = "Profil Fotoğrafı",
            error = painterResource(id = R.drawable.default_user),
            placeholder = painterResource(id = R.drawable.default_user),
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .clickable {
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                }
        )
        Text("Fotoğrafı değiştirmek için tıkla", style = MaterialTheme.typography.bodySmall)

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = newUsername,
            onValueChange = { newUsername = it },
            label = { Text("Kullanıcı Adı") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { onSave(newUsername, newImageUri) },
            enabled = uiState !is UpdateProfileUiState.Loading,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (uiState is UpdateProfileUiState.Loading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
            } else {
                Text("Değişiklikleri Kaydet")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun ProfileGridItem(post: Posts, onClick: () -> Unit) {
    val thumbnailUrl =
        if (post.mediaType == "video") post.thumbnailUrl else post.mediaUrls.firstOrNull()

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clickable(onClick = onClick)
    ) { // 1:1 kare oran
        AsyncImage(
            model = thumbnailUrl,
            contentDescription = "Profil Gönderisi",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop, // Resmi kutuyu dolduracak şekilde kırp
            placeholder = painterResource(id = R.drawable.memories)
        )
        // Eğer video ise sağ üste bir video ikonu ekleyebilirsin
        if (post.mediaType == "video") {

            Icon(
                painter = painterResource(id = R.drawable.ic_play_video),
                contentDescription = "Video",
                tint = Color.White,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
            )
        }
    }
}


