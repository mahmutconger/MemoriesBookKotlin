import android.icu.text.SimpleDateFormat
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anlarsinsoftware.memoriesbook.R
import com.anlarsinsoftware.memoriesbook.ui.theme.Model.Comments
import com.anlarsinsoftware.memoriesbook.ui.theme.Model.Posts
import com.anlarsinsoftware.memoriesbook.ui.theme.Tools.myImageButton
import com.google.firebase.Timestamp
import java.util.Locale

@Composable
fun CommentBottomSheetContent(
    post: Posts,
    commentList: List<Comments>,
    onHide: () -> Unit,
    onCommentLikeClicked: (Comments) -> Unit
) {
    var commentTextState by remember { mutableStateOf("") }

    Column(
        Modifier
            .padding(16.dp)
            .imePadding()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().imePadding(),
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
                .imePadding()
        ) {
            items(items = commentList) { item ->
                CommentItem(
                    comment = item,
                    onLikeClick = { onCommentLikeClicked(item) }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth().imePadding(),
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
                // Yorum gönderme mantığı buraya eklenecek.
            }) {
                Icon(Icons.Default.Send, contentDescription = "Yorumu Gönder")
            }
        }
    }
}

@Composable
fun CommentItem(comment: Comments, onLikeClick: () -> Unit) {
    Card(
        Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier
            .fillMaxWidth()
            .padding(5.dp)) {
            Row() {
                Text(comment.user, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(Modifier.weight(1f))
                val formattedDate = remember(comment.date) {
                    try {
                        val sdf = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
                        sdf.format(comment.date.toDate())
                    } catch (e: Exception) {
                        // Bir sorun olursa varsayılan metin göster
                        "Tarih bilgisi yok"
                    }
                }
                Text(formattedDate, fontSize = 12.sp, color = Color.Gray)
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(comment.comment, fontSize = 14.sp)

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                val likeIcon =
                    if (comment.isLiked) R.drawable.like_selected else R.drawable.like_unselected

                myImageButton(
                    id = likeIcon,
                    imageSize = 25,
                    onClick = onLikeClick
                )

                Spacer(Modifier.width(15.dp))
            }


        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CommentBottomSheetContent_Preview() {
    val samplePost = Posts("user@test.com", "", "", "1", Timestamp.now())
    val sampleComments = listOf(
        Comments("Harika bir paylaşım!", Timestamp.now(), "ahmet", "c1", "1", true),
        Comments("Ben de oraya gitmek istiyorum.", Timestamp.now(), "zeynep", "c2", "1", false),
        Comments("Fotoğraf çok güzel çıkmış.", Timestamp.now(), "ali", "c3", "1", false)
    )
    CommentBottomSheetContent(
        post = samplePost,
        commentList = sampleComments,
        onHide = {}, onCommentLikeClicked = {}
    )
}