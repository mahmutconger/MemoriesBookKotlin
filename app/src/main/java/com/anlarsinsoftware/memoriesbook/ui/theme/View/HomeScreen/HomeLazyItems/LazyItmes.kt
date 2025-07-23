package com.anlarsinsoftware.memoriesbook.ui.theme.View.HomeScreen.HomeLazyItems

import VideoPlayer
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.anlarsinsoftware.memoriesbook.R
import com.anlarsinsoftware.memoriesbook.ui.theme.Model.Posts
import com.anlarsinsoftware.memoriesbook.ui.theme.Tools.ExpandableText
import com.anlarsinsoftware.memoriesbook.ui.theme.Tools.myIconButtonPainter
import com.anlarsinsoftware.memoriesbook.ui.theme.Tools.mySpacer
import com.anlarsinsoftware.memoriesbook.ui.theme.Tools.rememberFormattedTimestamp
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

@Composable
fun LikersItem(photoUrl: String, userName: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {


        AsyncImage(
            model = photoUrl,
            contentDescription = "User Image",
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop,
            error = painterResource(id = R.drawable.default_user),
            placeholder = painterResource(id = R.drawable.default_user)
        )
        Spacer(Modifier.weight(0.2f))
        Text(userName, fontSize = 21.sp, fontWeight = FontWeight.Bold)

        Spacer(Modifier.weight(1f))
        Image(
            painter = painterResource(R.drawable.like_selected),
            contentDescription = "Beğenildi butonu"
        )
    }
}

@Composable
fun PostItem(
    post: Posts,
    onMenuClick: () -> Unit,
    onLikeClick: () -> Unit,
    onCommentClick: () -> Unit,
    onShowLikersClick: () -> Unit,
    onMediaClick: (post: Posts, startIndex: Int) -> Unit,
    onVideoFullScreen: (videoUrl: String) -> Unit
) {
    val currentUser = Firebase.auth.currentUser
    val isLikedByMe by remember(post.likedBy, currentUser) {
        mutableStateOf(currentUser?.uid in post.likedBy)
    }
    Card(
        modifier = Modifier

            .fillMaxWidth()

            .padding(horizontal = 16.dp, vertical = 8.dp),

        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Row(Modifier.fillMaxWidth()) {

                Column() {
                    Text(text = post.useremail, fontWeight = FontWeight.Bold)
                    val formattedDate =rememberFormattedTimestamp(timestamp = post.date)
                    Text(
                        formattedDate,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )


                }

                Spacer(Modifier.weight(1f))
                myIconButtonPainter(
                    R.drawable.menu_vertical,
                    imageSize = 20,
                    onClick = onMenuClick,
                    tintColor = MaterialTheme.colorScheme.primary
                )
            }


            Spacer(modifier = Modifier.height(8.dp))

            when (post.mediaType) {
                "image" -> {
                    Box(contentAlignment = Alignment.TopEnd) {
                        val pagerState = rememberPagerState(pageCount = { post.mediaUrls.size })
                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier
                                .height(400.dp)
                                .clip(RoundedCornerShape(8.dp))
                        ) { page ->
                            AsyncImage(
                                model = post.mediaUrls[page],
                                contentDescription = "Post Image ${page + 1}",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clickable { onMediaClick(post, page) },
                                contentScale = ContentScale.Crop
                            )
                        }
                        if (post.mediaUrls.size > 1) {
                            Text(
                                text = "${pagerState.currentPage + 1} / ${post.mediaUrls.size}",
                                modifier = Modifier
                                    .padding(8.dp)
                                    .background(
                                        Color.Black.copy(alpha = 0.5f),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                color = Color.White,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }

                "video" -> {
                    Box(contentAlignment = Alignment.Center) {
                        VideoPlayer(
                            videoUrl = post.mediaUrls.firstOrNull(), modifier = Modifier.padding()
                        )
                        IconButton(
                            onClick = {
                                post.mediaUrls.firstOrNull()?.let { onVideoFullScreen(it) }
                            },
                            modifier = Modifier.background(
                                Color.Black.copy(alpha = 0.5f),
                                CircleShape
                            )
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_fullscreen),
                                contentDescription = "Tam Ekran",
                                tint = Color.White
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            ExpandableText(
                text = post.comment,
                style = MaterialTheme.typography.bodyMedium
            )
            mySpacer(10)

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onLikeClick() }
                ) {
                    val likeIcon =
                        if (isLikedByMe) R.drawable.like_selected else R.drawable.like_unselected

                    myIconButtonPainter(
                        likeIcon,
                        tintColor = Color.Unspecified,
                        onClick = onLikeClick
                    )


                    if (post.likedBy.isNotEmpty()) {
                        Text(
                            text = "${post.likedBy.size} beğeni",
                            modifier = Modifier
                                .clickable { onShowLikersClick() }
                                .padding(start = 4.dp, end = 8.dp),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .clickable { onCommentClick() }
                        .padding(start = 15.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    myIconButtonPainter(
                        resourcesId = R.drawable.comment_icon,
                        onClick = onCommentClick,
                        tintColor = MaterialTheme.colorScheme.primary
                    )
                    if (post.commentCount > 0) {
                        Text(
                            text = "${post.commentCount} yorum",
                            modifier = Modifier.padding(start = 4.dp),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }

}
