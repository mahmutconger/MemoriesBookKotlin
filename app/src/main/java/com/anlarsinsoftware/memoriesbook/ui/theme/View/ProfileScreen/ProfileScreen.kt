import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.anlarsinsoftware.memoriesbook.R
import com.anlarsinsoftware.memoriesbook.ui.theme.Tools.myBrush
import com.anlarsinsoftware.memoriesbook.ui.theme.Tools.myIconButton
import com.anlarsinsoftware.memoriesbook.ui.theme.Tools.mySpacer
import com.anlarsinsoftware.memoriesbook.ui.theme.Tools.myText
import com.google.firebase.auth.FirebaseAuth
import com.anlarsinsoftware.memoriesbook.ui.theme.Model.Followers
import com.anlarsinsoftware.memoriesbook.ui.theme.Model.Following
import com.anlarsinsoftware.memoriesbook.ui.theme.Model.PendingRequest
import com.anlarsinsoftware.memoriesbook.ui.theme.Tools.ProfileScaffold
import com.anlarsinsoftware.memoriesbook.ui.theme.Tools.showToast
import com.anlarsinsoftware.memoriesbook.ui.theme.View.MessageScreen.FriendItem
import com.anlarsinsoftware.memoriesbook.ui.theme.ViewModel.ConnectionsViewModel
import com.anlarsinsoftware.memoriesbook.ui.theme.ViewModel.ProfileViewModel
import com.anlarsinsoftware.memoriesbook.ui.theme.ViewModel.UpdateProfileUiState
import kotlinx.coroutines.launch


@SuppressLint("CoroutineCreationDuringComposition")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    connectionsViewModel: ConnectionsViewModel = viewModel(),
    profileViewModel: ProfileViewModel =viewModel()
) {
    val currentUser = FirebaseAuth.getInstance().currentUser
    val context = LocalContext.current
    val followerSheetState = rememberModalBottomSheetState()
    val followingSheetState = rememberModalBottomSheetState()
    val friendsreqSheetState = rememberModalBottomSheetState()
    val friendsSheetState = rememberModalBottomSheetState()
    val editProfileSheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    val followers by connectionsViewModel.followers.collectAsState()
    val following by connectionsViewModel.following.collectAsState()
    val requests by connectionsViewModel.pendingRequests.collectAsState()
    val friends by connectionsViewModel.friends.collectAsState()

    val updateUiState by profileViewModel.uiState.collectAsState()

    // --- Yan Etkileri Yönetme (Toast ve Navigasyon) ---
    LaunchedEffect(updateUiState) {
        if (updateUiState is UpdateProfileUiState.Success) {
            showToast(context, "Profil başarıyla güncellendi!")
            scope.launch { editProfileSheetState.hide() }.invokeOnCompletion {
                profileViewModel.resetUiState() // State'i sıfırla
            }
        } else if (updateUiState is UpdateProfileUiState.Error) {
            showToast(context, (updateUiState as UpdateProfileUiState.Error).message, isLengthLong = true)
            profileViewModel.resetUiState()
        }
    }

    ProfileScaffold(
        navController,
        context,
        onClickNotify = {
            scope.launch { friendsreqSheetState.show() }
        }
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AsyncImage(
                model = currentUser?.photoUrl,
                "Profil resmi",
                error = painterResource(id = R.drawable.default_user),
                placeholder = painterResource(id = R.drawable.default_user),
                modifier = Modifier
                    .size(125.dp)
                    .clip(CircleShape)
                    .border(2.dp, myBrush(), CircleShape)
            )
            mySpacer(8)
            myText(currentUser?.displayName.toString(), 21, FontWeight.Medium)
            mySpacer(8)
            Text("Profili düzenle", modifier = Modifier.clickable {
                scope.launch { editProfileSheetState.show() }
            }, fontWeight = FontWeight.Light)

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 15.dp, vertical = 15.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Text("    ${followers.size} Takipçi    ", modifier = Modifier.clickable {
                    scope.launch { followerSheetState.show() }
                })
                Text("|    ${following.size} Takip    ", modifier = Modifier.clickable {
                    scope.launch { followingSheetState.show() }
                })
                Text("|    ${friends.size} Arkadaş    ", modifier = Modifier.clickable {
                    scope.launch { friendsSheetState.show() }
                })

            }

        }
    }

    if (editProfileSheetState.isVisible) {
        ModalBottomSheet(
            sheetState = editProfileSheetState,
            onDismissRequest = { scope.launch { editProfileSheetState.hide() } }
        ) {
            EditProfileContent(
                currentUsername = currentUser?.displayName ?: "",
                currentPhotoUri = currentUser?.photoUrl,
                uiState = updateUiState,
                onSave = { newName, newImageUri ->
                    profileViewModel.updateUserProfile(newName, newImageUri)
                }
            )
        }
    }

    if (followerSheetState.isVisible) {
        ModalBottomSheet(
            sheetState = followerSheetState,
            onDismissRequest = {
                scope.launch { followerSheetState.hide() }
            }
        ) {
            // BottomSheet içeriği
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Text(
                        "Takipçiler",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                items(items = followers) { user ->
                    UserItemForFollowers(user = user)
                }
            }
        }
    }

    //following bottom sheet

    if (followingSheetState.isVisible) {
        ModalBottomSheet(
            sheetState = followingSheetState,
            onDismissRequest = {
                scope.launch { followingSheetState.hide() }
            }
        ) {
            // BottomSheet içeriği
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Text(
                        "Takip edilenler",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                items(items = following) { user ->
                    UserItemForFollowing(user = user)
                }
            }
        }
    }

    if (friendsSheetState.isVisible) {
        ModalBottomSheet(
            sheetState = friendsSheetState,
            onDismissRequest = {
                scope.launch { friendsSheetState.hide() }
            }
        ) {
            // BottomSheet içeriği
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Text(
                        "Arkadaşlar",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                items(items = friends) { friend ->
                    FriendItem(
                        friend = friend,
                        onClick = {
                        }
                    )
                }
            }
        }
    }
    //request bottom sheet

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
fun UserItemForFollowers(user: Followers) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(user.username, fontWeight = FontWeight.Bold)
                Text(user.email, style = MaterialTheme.typography.bodySmall)
            }

        }
    }
}

@Composable
fun UserItemForFollowing(user: Following) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(user.username, fontWeight = FontWeight.Bold)
                Text(user.email, style = MaterialTheme.typography.bodySmall)
            }
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
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
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
        modifier = Modifier.padding(16.dp).fillMaxWidth(),
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


@Preview(showBackground = true)
@Composable
private fun prev_prof() {
    ProfileScreen(rememberNavController())
}