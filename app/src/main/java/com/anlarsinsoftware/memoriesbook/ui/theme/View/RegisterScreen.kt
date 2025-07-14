package com.anlarsinsoftware.memoriesbook.ui.theme.View

import android.content.Context
import android.util.Log
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
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.anlarsinsoftware.memoriesbook.R
import com.anlarsinsoftware.memoriesbook.ui.theme.Tools.myBrush
import com.anlarsinsoftware.memoriesbook.ui.theme.Tools.myButton
import com.anlarsinsoftware.memoriesbook.ui.theme.Tools.myImageButton
import com.anlarsinsoftware.memoriesbook.ui.theme.Tools.mySpacer
import com.anlarsinsoftware.memoriesbook.ui.theme.Tools.myTextField
import com.anlarsinsoftware.memoriesbook.ui.theme.Tools.showToast
import com.anlarsinsoftware.memoriesbook.ui.theme.ViewModel.RegisterViewModel
import com.anlarsinsoftware.memoriesbook.ui.theme.ViewModel.RegistrationUiState
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

@Composable
fun RegisterScreen(
    navController: NavController,
    registerViewModel: RegisterViewModel = viewModel()
) {
    val context = LocalContext.current

    // UI state'lerini Composable içinde tutuyoruz
    var userNameReg by remember { mutableStateOf("") }
    var userEmailReg by remember { mutableStateOf("") }
    var passwordReg by remember { mutableStateOf("") }
    var passwordRegVerify by remember { mutableStateOf("") }

    // ViewModel'den gelen genel UI durumunu dinliyoruz
    val uiState by registerViewModel.uiState.collectAsState()

    // State'e göre yan etkileri (Toast, Navigasyon) yönetmek için
    LaunchedEffect(key1 = uiState) {
        when (val state = uiState) {
            is RegistrationUiState.Success -> {
                showToast(context, "Kayıt başarılı!")
                navController.navigate("home_screen") {
                    popUpTo("welcome_screen") { inclusive = true }
                }
            }

            is RegistrationUiState.Error -> {
                showToast(context, state.message, isLengthLong = true)
            }

            else -> {}
        }
    }
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
            myTextField(
                userNameReg,
                "Kullanıcı Adı",
                false
            ) {
                userNameReg = it
            }
            mySpacer(24)
            myTextField(
                userEmailReg,
                "Email",
                false
            ) {
                userEmailReg = it
            }
            mySpacer(24)
            myTextField(
                passwordReg,
                "Şifre",
                false
            ) {
                passwordReg = it
            }
            mySpacer(24)
            myTextField(
                passwordRegVerify,
                "Şifre Onay",
                false
            ) {
                passwordRegVerify = it
            }
            mySpacer(32)

            Button(onClick = {
                if (passwordReg != passwordRegVerify) {
                    showToast(context, "Şifreler uyuşmuyor.")
                } else if (userNameReg.isBlank() || userEmailReg.isBlank() || passwordReg.isBlank()) {
                    showToast(context, "Tüm alanları doldurunuz.")
                } else {
                    registerViewModel.registerUser(userEmailReg, passwordReg,userNameReg)
                }
            }, enabled = uiState !is RegistrationUiState.Loading )
               {
                Text("Kayıt Ol")
            }

            // Yükleme animasyonunu göster
            if (uiState is RegistrationUiState.Loading) {
                Spacer(Modifier.height(16.dp))
                CircularProgressIndicator()
            }
            mySpacer(100)
            Card(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clickable {
                        navController.navigate("home_screen")
                    },
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    val context = LocalContext.current
                    myImageButton(id = R.drawable.google_ico, imageSize = 50) {
                        showToast(context, "Google", false)
                    }
                    Spacer(Modifier.width(20.dp))
                    Text(
                        "Google İle Oturum Aç",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

            }

        }
    }
}

@Preview(showBackground = true)
@Composable
fun prev_Register() {
    RegisterScreen(rememberNavController())
}