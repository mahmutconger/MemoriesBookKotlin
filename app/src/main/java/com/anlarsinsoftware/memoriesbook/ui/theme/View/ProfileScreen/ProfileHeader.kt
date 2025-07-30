package com.anlarsinsoftware.memoriesbook.ui.theme.View.ProfileScreen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.anlarsinsoftware.memoriesbook.R
import com.anlarsinsoftware.memoriesbook.ui.theme.Model.FriendProfile
import com.anlarsinsoftware.memoriesbook.ui.theme.Model.User

// ProfileHeader'ın yeni ve daha temiz imzası
@Composable
fun ProfileHeader(
    user: User?,
    followersCount: Int,
    followingCount: Int,
    friendsCount: Int,
    onFollowersClick: () -> Unit,
    onFollowingClick: () -> Unit,
    onFriendsClick: () -> Unit,
    onEditProfileClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AsyncImage(
            model = user?.photoUrl,
            contentDescription = "Profil Fotoğrafı",
            modifier = Modifier.size(100.dp).clip(CircleShape),
            error = painterResource(id = R.drawable.default_user)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(user?.username ?: "", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))

        Text(
            "Profili düzenle",
            modifier = Modifier.clickable(onClick = onEditProfileClick),
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable(onClick = onFollowersClick)) {
                Text("$followersCount", fontWeight = FontWeight.Bold)
                Text("Takipçi")
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable(onClick = onFollowingClick)) {
                Text("$followingCount", fontWeight = FontWeight.Bold)
                Text("Takip")
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable(onClick = onFriendsClick)) {
                Text("$friendsCount", fontWeight = FontWeight.Bold)
                Text("Arkadaş")
            }
        }
    }
}