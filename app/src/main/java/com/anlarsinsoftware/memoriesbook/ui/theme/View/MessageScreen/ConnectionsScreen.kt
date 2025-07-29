package com.anlarsinsoftware.memoriesbook.ui.theme.View.MessageScreen

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.anlarsinsoftware.memoriesbook.R
import com.anlarsinsoftware.memoriesbook.ui.theme.Model.SearchResultUser
import com.anlarsinsoftware.memoriesbook.ui.theme.Util.myBrush
import com.anlarsinsoftware.memoriesbook.ui.theme.ViewModel.ConnectionsViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConnectionsScreen(
    navController: NavController,
    connectionsViewModel: ConnectionsViewModel = viewModel()
) {
    val searchQuery by connectionsViewModel.searchQuery.collectAsState()
    val searchResults by connectionsViewModel.searchResults.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {

            TopAppBar(title = {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { connectionsViewModel.searchQuery.value = it },
                    placeholder = { Text("Kullanıcı adı ile ara...") },
                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) }
                )
            })
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(items = searchResults) { user ->
                UserItem(
                    user = user,
                    onAddFriendClicked = {
                        connectionsViewModel.sendFriendRequest(
                            user.uid,
                            context = context
                        )
                    }
                )
            }
        }
    }
}

@Composable
fun UserItem(user: SearchResultUser, onAddFriendClicked: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = user.photoUrl,
                "Profil resmi",
                error = painterResource(id = R.drawable.default_user),
                placeholder = painterResource(id = R.drawable.default_user),
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .border(2.dp, myBrush(), CircleShape)
            )
            Spacer(Modifier.weight(0.1f))
            Column(modifier = Modifier.weight(1f)) {
                Text(user.username, fontWeight = FontWeight.Bold)
                Text(user.email, style = MaterialTheme.typography.bodySmall)
            }
            IconButton(onClick = onAddFriendClicked) {
                Icon(Icons.Default.Add, contentDescription = "Arkadaş Ekle")
            }
        }
    }
}




@Preview(showBackground = true)
@Composable
private fun con_prev() {
    ConnectionsScreen(rememberNavController())
}