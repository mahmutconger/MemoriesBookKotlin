package com.anlarsinsoftware.memoriesbook.ui.theme.View
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.anlarsinsoftware.memoriesbook.ui.theme.Tools.myBrush

@Composable
fun RegisterScreen(navController: NavController) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = myBrush()),
        contentAlignment = Alignment.Center
    ) {
        Text("Burası Kayıt Ekranı")
    }
}