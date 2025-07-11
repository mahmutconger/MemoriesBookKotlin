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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
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
import com.anlarsinsoftware.memoriesbook.R

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
fun myButton(text: String, isOutLined: Boolean, onClick: () -> Unit) {
    if (isOutLined) {
        ElevatedButton(onClick) {
            Text(text)
        }
    } else {
        Button(onClick) {
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
) {
    IconButton(onClick = onClick) {
        Icon(
            imageVector = imageVector,
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
    homeTint:Color=MaterialTheme.colorScheme.primary,
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

        myImageButton(
            id = R.drawable.home_icon,
            tintColor = homeTint
        ) {
            homeClick()
        }
        myImageButton(
            id = R.drawable.person_ico,
            tintColor = MaterialTheme.colorScheme.primary,
            bgColor = bgColorProfile
        ) {
            profileClick()
        }
        myImageButton(id = R.drawable.add_post,
            tintColor = MaterialTheme.colorScheme.primary,
            bgColor = bgColorCreatePost
        ) {
            createPostClick()
        }
        myImageButton(
            id = R.drawable.message_circle2,
            tintColor = MaterialTheme.colorScheme.primary,
            bgColor = bgColorMessage
        ) {
            messageClick()
        }
    }
}
