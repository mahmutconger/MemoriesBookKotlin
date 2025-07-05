package com.anlarsinsoftware.memoriesbook.ui.theme.View

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.anlarsinsoftware.memoriesbook.ui.theme.Tools.myBrush
import com.anlarsinsoftware.memoriesbook.ui.theme.Tools.myButton
import com.anlarsinsoftware.memoriesbook.ui.theme.Tools.mySpacer
import com.anlarsinsoftware.memoriesbook.ui.theme.Tools.myText

@Composable
fun WellComeScreen(modifier: Modifier = Modifier, navController: NavController) {
    Column(
        modifier
            .fillMaxSize()
            .background(brush = myBrush() )
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(end = 15.dp, top = 40.dp),
            horizontalAlignment = Alignment.End
        ) {
            myText("Merhaba!", 21, FontWeight.Bold, Color.White)
            myText("Tekrardan Hoş Geldiniz", 16, FontWeight.Light, Color.White)
        }
        Column(
            Modifier
                .fillMaxSize()
                .weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            val deger = remember { mutableStateOf("Daha Önce Geldin Mi?") }
            myText(deger.value, 16, FontWeight.ExtraBold, Color.White)
            mySpacer(10)
            myButton("Giriş Yap", true) {
                navController.navigate("login_screen")
                deger.value = "evet"
                Log.d("btnTag", "Giriş Yap butonuna basıldı.")
            }
            mySpacer(10)
            myText("Hayır mı?", 16, FontWeight.Bold, Color.White)
            mySpacer(10)
            myButton("Kayıt Ol", false) {
                navController.navigate("register_screen")
                Log.d("btnTag", "Kayıt Ol butonuna basıldı.")
            }
        }
    }
}