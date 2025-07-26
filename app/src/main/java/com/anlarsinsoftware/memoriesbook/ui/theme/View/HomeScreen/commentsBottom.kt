import android.icu.text.SimpleDateFormat
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.anlarsinsoftware.memoriesbook.R
import com.anlarsinsoftware.memoriesbook.ui.theme.Model.Comments
import com.anlarsinsoftware.memoriesbook.ui.theme.Model.Posts
import com.anlarsinsoftware.memoriesbook.ui.theme.Tools.ExpandableText
import com.anlarsinsoftware.memoriesbook.ui.theme.Tools.myIconButtonPainter
import com.anlarsinsoftware.memoriesbook.ui.theme.Tools.myImageButton
import com.anlarsinsoftware.memoriesbook.ui.theme.Tools.rememberFormattedTimestamp
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import java.util.Locale

@Composable
fun CommentBottomSheetContent(
    post: Posts,
    commentList: List<Comments>,
    onHide: () -> Unit,
    onCommentLikeClicked: (Comments) -> Unit,
    onAddCommentClicked: (String) -> Unit
) {
    var commentTextState by remember { mutableStateOf("") }
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

    Column(
        Modifier
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Yorumlar", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            IconButton(onClick = onHide) {
                Icon(Icons.Default.Close, contentDescription = "Yorumları Kapat")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.fillMaxWidth()
        ) {
            items(items = commentList) { item ->
                CommentItem(
                    comment = item,
                    currentUserId = currentUserId,
                    onLikeClick = { onCommentLikeClicked(item) },
                    onShowLikersClick = {}
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                value = commentTextState,
                onValueChange = { new -> commentTextState = new },
                label = { Text("Yorum Ekle") },
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(onClick = {
                onAddCommentClicked(commentTextState)
                commentTextState = ""
            }) {
                Icon(Icons.Default.Send, contentDescription = "Yorumu Gönder")
            }
        }
    }
}

@Composable
fun CommentItem(
    comment: Comments,
    currentUserId: String?,
    onLikeClick: () -> Unit,
    onShowLikersClick: () -> Unit
) {

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        AsyncImage(
            model = comment.userPhotoUrl,
            contentDescription = "${comment.username} profil fotoğrafı",
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape),
            error = painterResource(id = R.drawable.default_user),
            placeholder = painterResource(id = R.drawable.default_user)
        )
        Spacer(modifier = Modifier.width(8.dp))

        Column {
            // 2. Başlık Satırı: İsim ve tarih
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(comment.username, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(Modifier.weight(1f)) // Aradaki tüm boşluğu doldurarak tarihi en sağa iter
                val formattedDate = rememberFormattedTimestamp(comment.date)
                Text(formattedDate, fontSize = 12.sp, color = Color.Gray)
            }

            Spacer(modifier = Modifier.height(4.dp))

            // 3. Yorum Metni
            ExpandableText(
                text = comment.comment,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 15.sp),
                collapsedMaxLines = 4 // Biraz daha fazla satır gösterebiliriz
            )

            // 4. Aksiyon Satırı: Yanıtla, Beğen butonu ve sayısı
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp) // Butonlar arasına boşluk
            ) {
                Text(
                    text = "Yanıtla",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray,
                    modifier = Modifier.clickable { /* TODO: Yanıtlama mantığı eklenecek */ }
                )

                val isLikedByMe = currentUserId in comment.likedBy
                val likeIcon = if (isLikedByMe) R.drawable.like_selected else R.drawable.like_unselected

                myImageButton (
                    id = likeIcon,
                    imageSize = 20,
                    onClick = onLikeClick
                )

                if (comment.likedBy.isNotEmpty()) {
                    Text(
                        text = "${comment.likedBy.size}",
                        modifier = Modifier.clickable(onClick = onShowLikersClick),
                        fontWeight = FontWeight.Normal,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}


/*
val isLikedByMe = currentUserId in comment.likedBy
            val likeIcon = if (isLikedByMe) R.drawable.like_selected else R.drawable.like_unselected

            myIconButtonPainter(resourcesId = likeIcon, imageSize = 25, onClick = onLikeClick)

            if (comment.likedBy.isNotEmpty()) {
                Text(
                    text = "${comment.likedBy.size} beğeni",
                    modifier = Modifier
                        .clickable(onClick = onShowLikersClick)
                        .padding(start = 8.dp),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.weight(1f))


             */


