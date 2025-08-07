package com.anlarsinsoftware.memoriesbook.ui.theme.Util

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SegmentedButtonDefaults.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anlarsinsoftware.memoriesbook.R


val DarkPurple = Color(0xFF2C2C3E)
val AccentGreen = Color(0xFF50E3C2) // Tasarımdaki yeşile daha yakın bir renk
val TextWhite = Color(0xFFFFFFFF)
val TextGray = Color(0xFF9B9B9B)
val LightGray = Color(0xFF4A4A58)

@Composable
fun AuthScreenLayout(title: String, content: @Composable ColumnScope.() -> Unit) {
    Surface(color = DarkPurple, modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                color = TextWhite,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth().padding(top = 32.dp, bottom = 16.dp)
            )
            content()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    leadingIcon: @Composable (() -> Unit)? = null,
    isSuccess: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = TextGray) },
        leadingIcon = leadingIcon,
        trailingIcon = { if (isSuccess) Icon(Icons.Filled.Check, contentDescription = "Success", tint = AccentGreen) },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = AccentGreen,
            unfocusedBorderColor = LightGray,
            focusedTextColor = TextWhite,
            unfocusedTextColor = TextWhite,
            cursorColor = AccentGreen,
            focusedContainerColor = LightGray,
            unfocusedContainerColor = LightGray,
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
    )
}

@Composable
fun PasswordTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isSuccess: Boolean = false
) {
    var passwordVisible by remember { mutableStateOf(false) }
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = TextGray) },
        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            val image = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
            val description = if (passwordVisible) "Hide password" else "Show password"
            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                if (isSuccess && !passwordVisible) {
                    Icon(Icons.Default.Check, contentDescription = "Success", tint = AccentGreen)
                } else {
                    Icon(image, description, tint = TextGray)
                }
            }
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = AccentGreen,
            unfocusedBorderColor = LightGray,
            focusedTextColor = TextWhite,
            unfocusedTextColor = TextWhite,
            cursorColor = AccentGreen,
            focusedContainerColor = LightGray,
            unfocusedContainerColor = LightGray,
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
    )
}

@Composable
fun AuthButton(text: String, onClick: () -> Unit,enabled : Boolean) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = AccentGreen),
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
    ) {
        Text(text, color = DarkPurple, fontSize = 18.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun OrSeparator() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Divider(color = LightGray, modifier = Modifier.weight(1f))
        Text(
            text = "or",
            color = TextGray,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Divider(color = LightGray, modifier = Modifier.weight(1f))
    }
}

@Composable
fun SocialLogins(onGoogleClick: () -> Unit,
                 onFacebookClick: () -> Unit,
                 onTwitterClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally)
    ) {
        SocialLoginButton(R.drawable.google_ico, onClick = onGoogleClick)
        SocialLoginButton(R.drawable.memories, onClick = onFacebookClick)
        SocialLoginButton(R.drawable.memories, onClick = onTwitterClick)
    }
}

@Composable
fun SocialLoginButton(iconRes: Int, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        shape = CircleShape,
        modifier = Modifier.size(50.dp),
        contentPadding = PaddingValues(0.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextGray),
        border = ButtonDefaults.outlinedButtonBorder.copy(brush = SolidColor(LightGray))
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = "Social Login",
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
fun AuthBottomLink(text: String, linkText: String, onClick: () -> Unit) {
    val annotatedString = buildAnnotatedString {
        withStyle(style = SpanStyle(color = TextGray)) {
            append("$text ")
        }
        pushStringAnnotation(tag = "link", annotation = "link")
        withStyle(style = SpanStyle(color = AccentGreen, fontWeight = FontWeight.Bold)) {
            append(linkText)
        }
        pop()
    }
    ClickableText(
        text = annotatedString,
        onClick = { offset ->
            annotatedString.getStringAnnotations(tag = "link", start = offset, end = offset)
                .firstOrNull()?.let {
                    onClick()
                }
        }
    )
}