package com.anlarsinsoftware.memoriesbook.ui.theme.View.CreatePostScreen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.anlarsinsoftware.memoriesbook.ui.theme.Model.Followers
import com.anlarsinsoftware.memoriesbook.ui.theme.Model.FriendProfile

// ... (gerekli diğer importlar)

// BottomSheet içindeki sekmeleri tanımlamak için
private enum class FriendListTab(val title: String) {
    FRIENDS("Arkadaşlar"),
    FOLLOWERS("Takipçiler")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendSelectorBottomSheet(
    friends: List<FriendProfile>,
    followers: List<Followers>,
    initiallySelectedIds: List<String>,
    onDismiss: () -> Unit,
    onSelectionDone: (List<String>) -> Unit
) {
    var selectedTab by remember { mutableStateOf(FriendListTab.FRIENDS) }
    var searchQuery by remember { mutableStateOf("") }
    // Değişiklikleri geçici bir listede tutuyoruz, "Tamam"a basınca ana state'i güncelliyoruz.
    val selectedIds =
        remember { mutableStateListOf<String>().also { it.addAll(initiallySelectedIds) } }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .heightIn(max = 500.dp)
                .padding(horizontal = 16.dp)
        ) {

            TabRow(selectedTabIndex = selectedTab.ordinal) {
                FriendListTab.values().forEach { tab ->
                    Tab(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        text = { Text(tab.title) }
                    )
                }
            }

            // --- ARAMA ÇUBUĞU ---
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Ara...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )

            // --- KULLANICI LİSTESİ ---
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                // --- Arkadaşlar Sekmesi ---
                if (selectedTab == FriendListTab.FRIENDS) {
                    val filteredList =
                        friends.filter { it.username.contains(searchQuery, ignoreCase = true) }
                    items(items = filteredList, key = { it.uid }) { user ->
                        SelectableUserItem(
                            name = user.username,
                            isSelected = user.uid in selectedIds,
                            onToggle = {
                                if (user.uid in selectedIds) selectedIds.remove(user.uid)
                                else selectedIds.add(user.uid)
                            }
                        )
                    }
                }
                // --- Takipçiler Sekmesi ---
                else {
                    val filteredList =
                        followers.filter { it.username.contains(searchQuery, ignoreCase = true) }
                    items(items = filteredList, key = { it.uid }) { user ->
                        SelectableUserItem(
                            name = user.username,
                            isSelected = user.uid in selectedIds,
                            onToggle = {
                                if (user.uid in selectedIds) selectedIds.remove(user.uid)
                                else selectedIds.add(user.uid)
                            }
                        )
                    }
                }

                // DEĞİŞİKLİK BURADA: Butonu LazyColumn'un son elemanı olarak ekliyoruz
                item {
                    Button(
                        onClick = { onSelectionDone(selectedIds.toList()) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp)
                    ) {
                        Text("Tamam")
                    }
                }
            }
        }
    }
}

// Listede gösterilecek her bir seçilebilir kullanıcı satırı
@Composable
fun SelectableUserItem(name: String, isSelected: Boolean, onToggle: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(checked = isSelected, onCheckedChange = { onToggle() })
        Spacer(modifier = Modifier.width(16.dp))
        Text(name)
    }
}