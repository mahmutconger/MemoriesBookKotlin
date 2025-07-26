package com.anlarsinsoftware.memoriesbook.ui.theme.Tools

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.anlarsinsoftware.memoriesbook.R
import com.google.common.io.Resources
import kotlinx.coroutines.launch

@Composable
fun myBrush(): Brush {
    val infiniteTransition = rememberInfiniteTransition(label = "background_gradient_animation")
    val startColor by infiniteTransition.animateColor(
        initialValue = Color(0xFF4A148C), targetValue = Color(0xFF0D47A1),
        animationSpec = infiniteRepeatable(
            animation = tween(4000),
            repeatMode = RepeatMode.Reverse
        ), label = ""
    )
    val endColor by infiniteTransition.animateColor(
        initialValue = Color(0xFFAD1457), targetValue = Color(0xFF1565C0),
        animationSpec = infiniteRepeatable(
            animation = tween(4000),
            repeatMode = RepeatMode.Reverse
        ), label = ""
    )
    return Brush.verticalGradient(colors = listOf(startColor, endColor))
}

@Composable
fun myText(
    text: String,
    fontSize: Int,
    fontWeight: FontWeight,
    color: Color = MaterialTheme.colorScheme.primary,
    textAlign: TextAlign = TextAlign.Start
) {
    Text(
        text = text,
        fontSize = fontSize.sp,
        fontWeight = fontWeight,
        color = color,
        textAlign = textAlign
    )
}

@Composable
fun mySpacer(height: Int) {
    Spacer(Modifier.height(height.dp))
}


@Composable
fun myButton(text: String, isOutLined: Boolean, enabled: Boolean = true, onClick: () -> Unit) {
    if (isOutLined) {
        ElevatedButton(onClick, enabled = enabled) {
            Text(text)
        }
    } else {
        Button(onClick, enabled = enabled) {
            Text(text)
        }
    }
}

fun showToast(context: Context, msg: String, isLengthLong: Boolean = false) {
    val duration = if (isLengthLong) Toast.LENGTH_LONG else Toast.LENGTH_SHORT
    Toast.makeText(context, msg, duration).show()
}

@Composable
fun myTextField(
    value: String,
    titleText: String,
    isOutLined: Boolean,
    onValueChange: (String) -> Unit
) {
    if (isOutLined) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            Modifier.fillMaxWidth(),
            label = {
                Text(titleText)
            })
    } else {
        TextField(
            value = value,
            onValueChange = onValueChange,
            Modifier.fillMaxWidth(),
            label = {
                Text(titleText)
            })
    }
}

@Composable
fun myImageButton(
    id: Int,
    imageSize: Int = 30,
    tintColor: Color = MaterialTheme.colorScheme.primary,
    bgColor: Color = Color.Transparent,
    onClick: () -> Unit
) {

    Image(
        modifier = Modifier
            .size(imageSize.dp)
            .clickable(onClick = onClick)
            .background(bgColor, ShapeDefaults.Large),
        painter = painterResource(id = id),
        contentDescription = "Action Icon",
        colorFilter = ColorFilter.tint(tintColor)
    )
}

@Composable
fun myIconButton(
    imageVector: ImageVector,
    imageSize: Int = 30,
    tintColor: Color = MaterialTheme.colorScheme.primary,
    onClick: () -> Unit
) = IconButton(onClick = onClick) {
    Icon(
        imageVector = imageVector,
        contentDescription = "Action Icon",
        modifier = Modifier.size(imageSize.dp),
        tint = tintColor
    )
}

@Composable
fun myIconButtonPainter(
    resourcesId: Int,
    imageSize: Int = 30,
    tintColor: Color = MaterialTheme.colorScheme.primary,
    onClick: () -> Unit
) {
    IconButton(onClick = onClick) {
        Icon(
            painter = painterResource(resourcesId),
            contentDescription = "Action Icon",
            modifier = Modifier.size(imageSize.dp),
            tint = tintColor
        )
    }
}

@Composable
fun myImageButton(id: Int, imageSize: Int = 30, onClick: () -> Unit) {
    Image(
        modifier = Modifier
            .size(imageSize.dp)
            .clickable(onClick = onClick),
        painter = painterResource(id = id),
        contentDescription = "Action Icon"
    )
}

@Composable
fun BottomNavigationBar(
    context: Context,
    createPostClick: () -> Unit,
    profileClick: () -> Unit,
    homeClick: () -> Unit,
    messageClick: () -> Unit,
    bgColorProfile: Color = Color.Transparent,
    bgColorCreatePost: Color = Color.Transparent,
    bgColorMessage: Color = Color.Transparent,
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(bottom = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceAround
    ) {

        myIconButtonPainter (
            resourcesId = R.drawable.home_icon,
            tintColor = MaterialTheme.colorScheme.primary
        ) {
            homeClick()
        }
        myIconButtonPainter(
            resourcesId = R.drawable.person_ico,
            tintColor = MaterialTheme.colorScheme.primary
        ) {
            profileClick()
        }
        myIconButtonPainter(
            resourcesId = R.drawable.add_post,
            tintColor = MaterialTheme.colorScheme.primary
        ) {
            createPostClick()
        }
        myIconButtonPainter(
            resourcesId = R.drawable.message_circle2,
            tintColor = MaterialTheme.colorScheme.primary
        ) {
            messageClick()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScaffold(
    navController: NavController,
    context: Context,
    onClickNotify: () -> Unit,
    content: @Composable () -> Unit
) {

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },

                navigationIcon = {
                    myIconButtonPainter(R.drawable.add_user) {
                        navController.navigate("connections_screen")
                    }
                },
                actions = {
                    myIconButton(Icons.Default.Notifications) {
                        onClickNotify()
                    }
                    myIconButtonPainter(R.drawable.memories) {
                        // Tıklama olayı
                    }
                    myIconButton(Icons.Default.Settings) {
                        navController.navigate("settings_screen")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }, bottomBar = {
            BottomNavigationBar(
                context = context,
                createPostClick = {
                    navController.navigate("createPost_screen")
                }, profileClick = {
                    navController.navigate("profile_screen")
                }, messageClick = {
                    navController.navigate("messages_screen")
                }, homeClick = {
                    navController.navigate("home_screen")
                })
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) { content(/*TODO BURADA COLMN / ROW TANIMLAMAK DAHA KULLANIŞLI HALE GETİRECEKTİR.*/) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyScaffold(
    titleText: String,
    navController: NavController,
    context: Context,
    actionIconContent: @Composable () -> Unit,
    navigationContent: @Composable () -> Unit,
    content: @Composable () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        titleText,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    navigationContent
                },

                actions = {
                    actionIconContent
                },

                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }, bottomBar = {
            BottomNavigationBar(
                context = context,
                createPostClick = {
                    navController.navigate("createPost_screen")
                }, profileClick = {
                    navController.navigate("profile_screen")
                }, messageClick = {
                    navController.navigate("messages_screen")
                }, homeClick = {
                    navController.navigate("home_screen")
                })
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) { content(/*TODO BURADA COLMN / ROW TANIMLAMAK DAHA KULLANIŞLI HALE GETİRECEKTİR.*/) }
    }
}
