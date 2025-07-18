package com.anlarsinsoftware.memoriesbook.ui.theme.View.MessageScreen

import MessagesViewModel
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.anlarsinsoftware.memoriesbook.ui.theme.Model.FriendProfile
import com.anlarsinsoftware.memoriesbook.ui.theme.Tools.BottomNavigationBar
import com.anlarsinsoftware.memoriesbook.ui.theme.ViewModel.ConnectionsViewModel
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.ktx.functions
import com.google.firebase.ktx.Firebase


private lateinit var functions: FirebaseFunctions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessagesScreen(navController: NavController,
                   connectionsViewModel: ConnectionsViewModel=viewModel()) {
    functions = Firebase.functions
    val context = LocalContext.current
    val friends by connectionsViewModel.friends.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Sohbetler",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.popBackStack()
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            tint = MaterialTheme.colorScheme.primary,
                            contentDescription = "Geri Butonu"
                        )
                    }
                },

                actions = {
                    IconButton(onClick = {
                      navController.navigate("connections_screen")
                    }) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            tint = MaterialTheme.colorScheme.primary,
                            contentDescription = "Geri Butonu"
                        )
                    }
                },

                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }, bottomBar = {
            BottomNavigationBar(
                context = context,
                createPostClick = {
                    navController.navigate("createPost_screen")
                }, profileClick = {
                    navController.navigate("profile_screen")
                }, messageClick = {

                }, homeClick = {
                    navController.navigate("home_screen")
                })
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center) {

            LazyColumn(
                modifier = Modifier.padding(innerPadding).fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(items = friends) { friend ->
                    FriendItem(
                        friend = friend,
                        onClick = {
                            // Tıklandığında o kişiye özel sohbet ekranına git
                            navController.navigate("chat_screen/${friend.uid}")
                        }
                    )
                }
            }

        }
    }
}
@Composable
fun FriendItem(friend: FriendProfile, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = friend.photoUrl,
            contentDescription = "${friend.username} profil fotoğrafı",
            modifier = Modifier.size(50.dp).clip(CircleShape)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(friend.username, fontWeight = FontWeight.Bold, fontSize = 18.sp)
    }
}

@Preview(showBackground = true)
@Composable
private fun mes_prev() {
    MessagesScreen(rememberNavController())
}