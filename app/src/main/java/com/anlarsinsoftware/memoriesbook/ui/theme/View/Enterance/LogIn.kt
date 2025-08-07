package com.anlarsinsoftware.memoriesbook.ui.theme.View.Enterance

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.anlarsinsoftware.memoriesbook.R
import com.anlarsinsoftware.memoriesbook.ui.theme.Util.AccentGreen
import com.anlarsinsoftware.memoriesbook.ui.theme.Util.AuthBottomLink
import com.anlarsinsoftware.memoriesbook.ui.theme.Util.AuthButton
import com.anlarsinsoftware.memoriesbook.ui.theme.Util.AuthScreenLayout
import com.anlarsinsoftware.memoriesbook.ui.theme.Util.AuthTextField
import com.anlarsinsoftware.memoriesbook.ui.theme.Util.DarkPurple
import com.anlarsinsoftware.memoriesbook.ui.theme.Util.OrSeparator
import com.anlarsinsoftware.memoriesbook.ui.theme.Util.PasswordTextField
import com.anlarsinsoftware.memoriesbook.ui.theme.Util.SocialLogins
import com.anlarsinsoftware.memoriesbook.ui.theme.Util.TextWhite
import com.anlarsinsoftware.memoriesbook.ui.theme.Util.showToast
import com.anlarsinsoftware.memoriesbook.ui.theme.ViewModel.LogInViewModel
import com.anlarsinsoftware.memoriesbook.ui.theme.ViewModel.LoginUiState
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException


@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit,
    logInViewModel: LogInViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by logInViewModel.uiState.collectAsState()

    LaunchedEffect(key1 = uiState) {
        when (val state = uiState) {
            is LoginUiState.Success -> onLoginSuccess()
            is LoginUiState.Error -> showToast(context, state.message, isLengthLong = true)
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

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var rememberMe by remember { mutableStateOf(false) }

    AuthScreenLayout(title = "Giriş Yap") {
        AuthTextField(
            value = email,
            onValueChange = { email = it },
            label = "Email",
            isSuccess = email.isNotEmpty()
        )
        Spacer(modifier = Modifier.height(16.dp))

        PasswordTextField(
            value = password,
            onValueChange = { password = it },
            label = "Şifre",
            isSuccess = password.isNotEmpty()
        )
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = rememberMe,
                    onCheckedChange = { rememberMe = it },
                    colors = CheckboxDefaults.colors(
                        checkedColor = AccentGreen,
                        checkmarkColor = DarkPurple
                    )
                )
                Text("Beni hatırla", color = TextWhite)
            }
            Text("Forgot Password?", color = TextWhite, modifier = Modifier.clickable {
                /* Şifre sıfırlama akışı */
            })
        }
        Spacer(modifier = Modifier.height(32.dp))

        AuthButton(
            text = "Giriş Yap",
            onClick = {
                if (email.isBlank() || password.isBlank()) {
                    showToast(context, "Tüm alanları doldurunuz.")
                } else {
                    logInViewModel.loginUser(email, password)
                }
            },
            enabled = uiState !is LoginUiState.Loading
        )

        if (uiState is LoginUiState.Loading) {
            Spacer(Modifier.height(16.dp))
            CircularProgressIndicator(color = AccentGreen)
        }

        Spacer(modifier = Modifier.height(24.dp))
        OrSeparator()
        Spacer(modifier = Modifier.height(24.dp))

        SocialLogins(
            onGoogleClick = {
                val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(context.getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build()
                val googleSignInClient = GoogleSignIn.getClient(context, gso)
                googleSignInClient.signOut().addOnCompleteListener {
                    launcher.launch(googleSignInClient.signInIntent)
                }
            },
            onFacebookClick = { /* Facebook giriş mantığı */ },
            onTwitterClick = { /* Twitter giriş mantığı */ }
        )

        Spacer(modifier = Modifier.weight(1f))

        AuthBottomLink(
            text = "Hesabın yok mu?",
            linkText = "Hesap Oluştur",
            onClick = onNavigateToRegister
        )
    }
}