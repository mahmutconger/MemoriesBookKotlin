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
import com.anlarsinsoftware.memoriesbook.ui.theme.Tools.myImageButton
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import java.util.Locale

@Composable
fun CommentBottomSheetContent(
    post: Posts,
    commentList: List<Comments>,
    onHide: () -> Unit,
    onCommentLikeClicked: (Comments) -> Unit,
    onAddCommentClicked: (String) -> Unit,
    onShowCommentLikers: (Comments) -> Unit
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
                // ViewModel'deki fonksiyonu çağırıyoruz
                onAddCommentClicked(commentTextState)
                // Yorumu gönderdikten sonra metin alanını temizliyoruz
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

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(5.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {

            AsyncImage(
                model = comment.userPhotoUrl,
                contentDescription = "${comment.username} profil fotoğrafı",
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape),
                error = painterResource(id = R.drawable.person_ico)
            )
            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(comment.username, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                val formattedDate = remember(comment.date) {
                    try {
                        val sdf = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
                        sdf.format(comment.date.toDate())
                    } catch (e: Exception) {
                        "Tarih bilgisi yok"
                    }
                }
                Text(formattedDate, fontSize = 12.sp, color = Color.Gray)
            }

        }

        Spacer(modifier = Modifier.height(4.dp))
        ExpandableText(
            text = comment.comment,
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp), // Mevcut stili koru
            collapsedMaxLines = 2 // Yorumlar daha kısa olduğu için 2 satırda kesebiliriz
        )

        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val isLikedByMe = currentUserId in comment.likedBy
            val likeIcon = if (isLikedByMe) R.drawable.like_selected else R.drawable.like_unselected

            myImageButton(id = likeIcon, imageSize = 25, onClick = onLikeClick)

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
        }


    }
}


