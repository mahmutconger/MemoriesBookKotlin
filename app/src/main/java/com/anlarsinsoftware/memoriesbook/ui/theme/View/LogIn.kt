package com.anlarsinsoftware.memoriesbook.ui.theme.View

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.anlarsinsoftware.memoriesbook.ui.theme.Tools.myBrush
import com.anlarsinsoftware.memoriesbook.ui.theme.Tools.myButton
import com.anlarsinsoftware.memoriesbook.ui.theme.Tools.mySpacer
import com.anlarsinsoftware.memoriesbook.ui.theme.Tools.myText
import com.anlarsinsoftware.memoriesbook.ui.theme.Tools.myTextField

@Composable
fun LoginScreen(navController: NavController) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = myBrush()),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            var userName by remember { mutableStateOf("") }
            var password by remember { mutableStateOf("") }
            myTextField(
                value = userName,
                "Kullanıcı Adı",
                false
            ) { newValue ->
                userName = newValue
            }

            mySpacer(16)
            myTextField(
                 password,
                 "Şifre",false
            ) {
                password = it
            }

            mySpacer(24)
            myButton(
                text = "Giriş Yap",
                 false
            ) {
                if (userName.isNotEmpty() && password.isNotEmpty()) {
                    Log.d("LoginAttempt", "Kullanıcı: $userName, Şifre: $password")
                    //navController.navigate("")
                } else {
                    Log.d("LoginAttempt", "Alanlar boş bırakılamaz.")
                }
            }
        }
    }
}



@Preview(showBackground = true)
@Composable
fun Preview_Login() {

    LoginScreen(navController = rememberNavController())
}