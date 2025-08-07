package com.anlarsinsoftware.memoriesbook.ui.theme.View.Enterance

import android.app.Activity
import android.util.Log
import android.util.Patterns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.anlarsinsoftware.memoriesbook.ui.theme.Util.showToast
import com.anlarsinsoftware.memoriesbook.ui.theme.ViewModel.RegisterViewModel
import com.anlarsinsoftware.memoriesbook.ui.theme.ViewModel.RegistrationUiState
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.anlarsinsoftware.memoriesbook.ui.theme.Util.AccentGreen
import com.anlarsinsoftware.memoriesbook.ui.theme.Util.AuthBottomLink
import com.anlarsinsoftware.memoriesbook.ui.theme.Util.AuthButton
import com.anlarsinsoftware.memoriesbook.ui.theme.Util.AuthScreenLayout
import com.anlarsinsoftware.memoriesbook.ui.theme.Util.AuthTextField
import com.anlarsinsoftware.memoriesbook.ui.theme.Util.DarkPurple
import com.anlarsinsoftware.memoriesbook.ui.theme.Util.PasswordTextField
import com.anlarsinsoftware.memoriesbook.ui.theme.Util.TextGray
import com.anlarsinsoftware.memoriesbook.ui.theme.Util.TextWhite


@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit,
    registerViewModel: RegisterViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by registerViewModel.uiState.collectAsState()

    LaunchedEffect(key1 = uiState) {
        when (val state = uiState) {
            is RegistrationUiState.Success -> onRegisterSuccess()
            is RegistrationUiState.Error -> showToast(context, state.message, isLengthLong = true)
            else -> {}
        }
    }

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(com.google.android.gms.common.api.ApiException::class.java)!!
                account.idToken?.let { idToken ->
                    registerViewModel.signInWithGoogle(idToken)
                } ?: run {
                    showToast(context, "Google token alınamadı.", true)
                }
            } catch (e: com.google.android.gms.common.api.ApiException) {
                Log.w("GoogleSignIn", "Google ile kayıt başarısız", e)
                showToast(context, "Google ile kayıt başarısız oldu.", true)
            }
        }
    }

    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var termsAccepted by remember { mutableStateOf(false) }

    AuthScreenLayout(title = "Kayıt Ol") {
        AuthTextField(
            value = username,
            onValueChange = { username = it },
            label = "Kullanıcı Adı",
            leadingIcon = { Icon(Icons.Default.AlternateEmail, contentDescription = null, tint = TextGray) },
            isSuccess = username.isNotBlank()
        )
        Spacer(modifier = Modifier.height(16.dp))
        AuthTextField(
            value = email,
            onValueChange = { email = it },
            label = "Email",
            isSuccess = Patterns.EMAIL_ADDRESS.matcher(email).matches() // Email format kontrolü
        )
        Spacer(modifier = Modifier.height(16.dp))
        PasswordTextField(
            value = password,
            onValueChange = { password = it },
            label = "Şifre",
            isSuccess = password.length >= 6
        )
        Spacer(modifier = Modifier.height(16.dp))
        PasswordTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = "Şifreyi Doğrula",
            isSuccess = confirmPassword.isNotBlank() && confirmPassword == password
        )
        Spacer(modifier = Modifier.height(24.dp))

        // Şartlar ve koşullar satırı
        Row(verticalAlignment = Alignment.Top) {
            Checkbox(
                checked = termsAccepted,
                onCheckedChange = { termsAccepted = it },
                colors = CheckboxDefaults.colors(checkedColor = AccentGreen, checkmarkColor = DarkPurple)
            )
            Spacer(modifier = Modifier.width(8.dp))
            TermsAndPrivacyText()
        }
        Spacer(modifier = Modifier.height(32.dp))

        // Kayıt Ol butonu
        AuthButton(
            text = "Kayıt Ol",
            onClick = {
                when {
                    !termsAccepted -> showToast(context, "Lütfen hizmet şartlarını kabul edin.")
                    username.isBlank() || email.isBlank() || password.isBlank() -> showToast(context, "Tüm alanları doldurunuz.")
                    password != confirmPassword -> showToast(context, "Şifreler uyuşmuyor.")
                    else -> registerViewModel.registerUser(email, password, username)
                }
            },
            enabled = uiState !is RegistrationUiState.Loading
        )

        if (uiState is RegistrationUiState.Loading) {
            Spacer(Modifier.height(16.dp))
            CircularProgressIndicator(color = AccentGreen)
        }

        Spacer(modifier = Modifier.weight(1f))

        AuthBottomLink(
            text = "Hesabın var mıydı?",
            linkText = "Giriş yap",
            onClick = onNavigateToLogin
        )
    }
}

@Composable
fun TermsAndPrivacyText() {
    // Tıklanabilir linkleri açmak için UriHandler'ı kullanacağız
    val uriHandler = LocalUriHandler.current

    val annotatedString = buildAnnotatedString {
        withStyle(style = SpanStyle(color = TextWhite)) {
            append("By signing up you accept the ")
        }
        // "Term of service" linkini ekliyoruz
        pushStringAnnotation(tag = "TOS", annotation = "https://your-terms-of-service-url.com")
        withStyle(style = SpanStyle(color = AccentGreen)) {
            append("Term of service")
        }
        pop()

        withStyle(style = SpanStyle(color = TextWhite)) {
            append(" and ")
        }

        pushStringAnnotation(tag = "PRIVACY", annotation = "https://your-privacy-policy-url.com")
        withStyle(style = SpanStyle(color = AccentGreen)) {
            append("Privacy Policy")
        }
        pop()
    }

    ClickableText(
        text = annotatedString,
        onClick = { offset ->
            annotatedString.getStringAnnotations(tag = "TOS", start = offset, end = offset)
                .firstOrNull()?.let { annotation ->
                    uriHandler.openUri(annotation.item)
                }

            annotatedString.getStringAnnotations(tag = "PRIVACY", start = offset, end = offset)
                .firstOrNull()?.let { annotation ->
                    uriHandler.openUri(annotation.item)
                }
        }
    )
}