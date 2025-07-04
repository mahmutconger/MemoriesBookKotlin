package com.anlarsinsoftware.memoriesbook.ui.theme.View

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.anlarsinsoftware.memoriesbook.ui.theme.Tools.myBrush

@Composable
fun LoginScreen(navController: NavController) {
    Box(Modifier
        .fillMaxSize()
        .background(brush = myBrush()),
        Alignment.Center
    ) {
        Row() {
            Text("Burası Giriş Sayfası")
        }
        Column {
            val deger = remember { mutableStateOf("") }
            OutlinedTextField(deger.value, onValueChange = {
                deger.value
                print(it)
            })


        }
    }

}