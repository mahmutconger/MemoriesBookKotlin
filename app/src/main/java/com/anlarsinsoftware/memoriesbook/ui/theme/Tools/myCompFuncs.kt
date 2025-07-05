package com.anlarsinsoftware.memoriesbook.ui.theme.Tools

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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
fun myText(text: String,
           fontSize: Int,
           fontWeight: FontWeight,
           color: Color = Color.Black,
           textAlign: TextAlign= TextAlign.Start) {
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
fun myImageButton(id: Int,imageSize: Int=30,tintColor: Color=Color.Black, onClick: () -> Unit) {
    Image(
        modifier = Modifier
            .size(imageSize.dp)
            .clickable(onClick = onClick),
        painter = painterResource(id = id),
        contentDescription = "Action Icon",
        colorFilter = ColorFilter.tint(tintColor)
    )
}
@Composable
fun myImageButton(id: Int,imageSize: Int=30, onClick: () -> Unit){
    Image(
        modifier = Modifier
            .size(imageSize.dp)
            .clickable(onClick = onClick),
        painter = painterResource(id = id),
        contentDescription = "Action Icon"
    )
}
