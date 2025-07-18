package com.anlarsinsoftware.memoriesbook.ui.theme.View.HomeScreen

import android.util.Log
import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.anlarsinsoftware.memoriesbook.R
import com.anlarsinsoftware.memoriesbook.ui.theme.Model.Posts
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun BottomSheetContent(
    post: Posts,
    currentUserId: String?,
    onHide: () -> Unit,
    onDeleteClick: () -> Unit,
    onEditClick: () -> Unit,
    onSetVisibilityClick: () -> Unit,
    onAddToFavoritesClick: () -> Unit
) {
    // Tarihi formatlamak için yardımcı değişken
    val formattedDate = remember(post.date) {
        post.date?.toDate()?.let {
            SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale("tr")).format(it)
        } ?: "Bilinmiyor"
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            "Post Seçenekleri",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        if (currentUserId != null && currentUserId == post.authorId) {
            BottomSheetItem(painterId = R.drawable.ic_edit, text = "Açıklamayı Düzenle", onClick = onEditClick)
            BottomSheetItem(painterId = R.drawable.ic_eye, text = "Kimler Görebilir?", onClick = onSetVisibilityClick)
            BottomSheetItem(painterId = R.drawable.ic_delete, text = "Postu Sil", onClick = onDeleteClick)

        } else {
            // --- EĞER POST SAHİBİ DEĞİLSE BU SEÇENEKLERİ GÖSTER ---

            BottomSheetItem(painterId = R.drawable.ic_star, text = "Favorilere Ekle", onClick = onAddToFavoritesClick)

            // Tıklanabilir olmayan bir bilgi satırı
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = Icons.Default.DateRange, modifier = Modifier.size(30.dp), contentDescription = "Paylaşım Tarihi")
                Spacer(modifier = Modifier.width(16.dp))
                Text("Paylaşım: $formattedDate")
            }
        }
    }
}

@Composable
fun BottomSheetItem(
    painterId: Int,
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
            Icon(painter =painterResource(painterId),modifier = Modifier.size(30.dp), tint = MaterialTheme.colorScheme.primary, contentDescription = text)
            Spacer(modifier = Modifier.width(16.dp))
            Text(text)
        }
    }
}