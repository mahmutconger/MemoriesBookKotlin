package com.anlarsinsoftware.memoriesbook.ui.theme.View.CreatePostScreen

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.anlarsinsoftware.memoriesbook.R
import com.anlarsinsoftware.memoriesbook.ui.theme.Model.Users


private enum class FriendListTab(val title: String) {
    FRIENDS("Arkadaşlar"),
    FOLLOWERS("Takipçiler")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendSelectorBottomSheet(
    friends: List<Users>,
    followers: List<Users>,
    initiallySelectedIds: List<String>,
    onDismiss: () -> Unit,
    onSelectionDone: (List<String>) -> Unit
) {
    var selectedTab by remember { mutableStateOf(FriendListTab.FRIENDS) }
    var searchQuery by remember { mutableStateOf("") }
    val selectedIds =
        remember { mutableStateListOf<String>().also { it.addAll(initiallySelectedIds) } }

    val listToDisplay = remember(selectedTab, friends, followers, searchQuery) {
        val sourceList = if (selectedTab == FriendListTab.FRIENDS) friends else followers
        sourceList.filter {
            it.username.contains(
                searchQuery,
                ignoreCase = true
            ) && it.uid.isNotBlank()
        }
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .heightIn(max = 500.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Değişikliliği iptal et")
                }
                IconButton(onClick = { onSelectionDone(selectedIds.toList()) }) {
                    Icon(Icons.Default.Check, contentDescription = "Değişikliği onayla")
                }
            }
            TabRow(
                selectedTabIndex = selectedTab.ordinal,
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                FriendListTab.values().forEach { tab ->
                    Tab(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        text = { Text(tab.title) }
                    )
                }
            }
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Ara...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                items(items = listToDisplay, key = { it.uid }) { user ->
                    SelectableUserItem(
                        name = user.username,
                        email = user.email,
                        photoUrl = user.photoUrl,
                        isSelected = user.uid in selectedIds,
                        onToggle = {
                            if (user.uid in selectedIds) selectedIds.remove(user.uid)
                            else selectedIds.add(user.uid)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun SelectableUserItem(
    name: String,
    email: String,
    photoUrl: String?,
    isSelected: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(checked = isSelected, onCheckedChange = { onToggle() })
        Spacer(modifier = Modifier.width(16.dp))

        AsyncImage(
            model = photoUrl,
            contentDescription = "$name profil fotoğrafı",
            error = painterResource(id = R.drawable.default_user),
            placeholder = painterResource(id = R.drawable.default_user),
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
                .border(1.dp, Color.LightGray, shape = CircleShape)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(name, fontWeight = FontWeight.Bold)
            Text(email, style = MaterialTheme.typography.bodySmall)
        }
    }
}