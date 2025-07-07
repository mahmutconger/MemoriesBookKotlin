package com.anlarsinsoftware.memoriesbook.ui.theme.View.HomeScreen

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.anlarsinsoftware.memoriesbook.ui.theme.Model.Posts

@Composable
fun BottomSheetContent(post: Posts, onHide: () -> Unit) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Post Seçenekleri: ${post.email}", style = MaterialTheme.typography.titleLarge)
        BottomSheetItem(icon = Icons.Default.Edit, text = "Postu Düzenle") {
            Log.d("BottomSheet", "Düzenle tıklandı: ${post.comment}")
            onHide()
        }
        BottomSheetItem(icon = Icons.Default.Settings, text = "Ayarlar") {
            Log.d("BottomSheet", "Ayarlar tıklandı.")
            onHide()
        }
        BottomSheetItem(icon = Icons.Default.ExitToApp, text = "Çıkış Yap") {
            Log.d("BottomSheet", "Çıkış Yap tıklandı.")
            onHide()
        }
    }
}
@Composable
fun BottomSheetItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    onClick: () -> Unit
) {
    TextButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = text)
            Spacer(modifier = Modifier.width(16.dp))
            Text(text)
        }
    }
}