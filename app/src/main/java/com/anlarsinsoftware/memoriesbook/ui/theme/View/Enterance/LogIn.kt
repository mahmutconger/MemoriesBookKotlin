package com.anlarsinsoftware.memoriesbook.ui.theme.View.Enterance

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.anlarsinsoftware.memoriesbook.R
import com.anlarsinsoftware.memoriesbook.ui.theme.Tools.myBrush
import com.anlarsinsoftware.memoriesbook.ui.theme.Tools.myImageButton
import com.anlarsinsoftware.memoriesbook.ui.theme.Tools.mySpacer
import com.anlarsinsoftware.memoriesbook.ui.theme.Tools.myTextField
import com.anlarsinsoftware.memoriesbook.ui.theme.Tools.showToast
import com.anlarsinsoftware.memoriesbook.ui.theme.ViewModel.LogInViewModel
import com.anlarsinsoftware.memoriesbook.ui.theme.ViewModel.LoginUiState
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException


@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    logInViewModel: LogInViewModel = viewModel()
) {

    val context = LocalContext.current

    // ViewModel'den gelen genel UI durumunu dinliyoruz
    val uiState by logInViewModel.uiState.collectAsState()

    LaunchedEffect(key1 = uiState) {
        when (val state = uiState) {
            is LoginUiState.Success -> {
                // Artık burada navigasyon yapmıyoruz, sadece haber veriyoruz.
                onLoginSuccess()
            }

            is LoginUiState.Error -> {
                showToast(context, state.message, isLengthLong = true)
            }

            else -> {}
        }
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                // Launcher'ın tek görevi token'ı alıp ViewModel'e göndermek.
                account.idToken?.let { idToken ->
                    logInViewModel.signInWithGoogle(idToken)
                } ?: run {
                    showToast(context, "Google token alınamadı.", true)
                }
            } catch (e: ApiException) {
                showToast(context, "Google ile giriş başarısız oldu: ${e.statusCode}", true)
            }
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
            var userEmail by remember { mutableStateOf("") }
            var password by remember { mutableStateOf("") }
            myTextField(
                value = userEmail,
                "Kullanıcı Adı",
                false
            ) { newValue ->
                userEmail = newValue
            }

            mySpacer(16)
            myTextField(
                password,
                "Şifre", false
            ) {
                password = it
            }

            mySpacer(24)
            Button(onClick = {
                if (userEmail.isBlank() || password.isBlank()) {
                    showToast(context, "Tüm alanları doldurunuz.")
                } else {
                    logInViewModel.loginUser(userEmail, password)
                }
            }, enabled = uiState !is LoginUiState.Loading)
            {
                Text("Giriş yap")
            }

            // Yükleme animasyonunu göster
            if (uiState is LoginUiState.Loading) {
                Spacer(Modifier.height(16.dp))
                CircularProgressIndicator()
            }

            mySpacer(100)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clickable(enabled = uiState !is LoginUiState.Loading) {
                        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                            .requestIdToken(context.getString(R.string.default_web_client_id))
                            .requestEmail()
                            .build()
                        val googleSignInClient = GoogleSignIn.getClient(context, gso)
                        googleSignInClient.signOut().addOnCompleteListener {
                            launcher.launch(googleSignInClient.signInIntent)
                        }
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
                if (uiState is LoginUiState.Loading) {
                    Spacer(Modifier.height(16.dp))
                    CircularProgressIndicator()
                }

            }
        }
    }
}



