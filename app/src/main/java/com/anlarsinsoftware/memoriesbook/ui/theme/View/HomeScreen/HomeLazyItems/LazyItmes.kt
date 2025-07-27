package com.anlarsinsoftware.memoriesbook.ui.theme.View.HomeScreen.HomeLazyItems

import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import coil.compose.AsyncImagePainter
import com.anlarsinsoftware.memoriesbook.R
import com.anlarsinsoftware.memoriesbook.ui.theme.Model.Posts
import com.anlarsinsoftware.memoriesbook.ui.theme.Util.ExpandableText
import com.anlarsinsoftware.memoriesbook.ui.theme.Util.myIconButtonPainter
import com.anlarsinsoftware.memoriesbook.ui.theme.Util.mySpacer
import com.anlarsinsoftware.memoriesbook.ui.theme.Util.rememberFormattedTimestamp
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

@Composable
fun LikersItem(photoUrl: String, userName: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp),
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PostItem(
    post: Posts,
    contentScale: ContentScale,
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

                AsyncImage(
                    model = post.authorPhotoUrl,
                    contentDescription = "${post.authorUsername} profil fotoğrafı",
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape),
                    error = painterResource(id = R.drawable.default_user),
                    placeholder = painterResource(id = R.drawable.default_user)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Column(modifier = Modifier.weight(1f)) {

                    Text(text = post.authorUsername, fontWeight = FontWeight.Bold)
                    val formattedDate = rememberFormattedTimestamp(timestamp = post.date)
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

            val mediaContainerModifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.Black)

            when (post.mediaType) {
                "image" -> {
                    Box(contentAlignment = Alignment.TopEnd) {
                        val pagerState = rememberPagerState(pageCount = { post.mediaUrls.size })
                        HorizontalPager(
                            state = pagerState,
                            modifier = mediaContainerModifier
                        ) { page ->
                            AsyncImage(
                                model = post.mediaUrls[page],
                                contentDescription = "Post Image ${page + 1}",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clickable { onMediaClick(post, page) },
                                contentScale = contentScale
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
                    Box(
                        modifier = mediaContainerModifier
                            .clickable { onMediaClick(post, 0) },
                        contentAlignment = Alignment.Center
                    ) {

                        AsyncImage(
                            model = post.thumbnailUrl,
                            contentDescription = "Video Kapağı",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = contentScale
                        )

                        Icon(
                            painterResource(R.drawable.ic_play_video),
                            contentDescription = "Videoyu Oynat",
                            modifier = Modifier.size(64.dp),
                            tint = Color.White.copy(alpha = 0.9f)
                        )
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
