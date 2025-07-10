package com.anlarsinsoftware.memoriesbook.ui.theme.View

import android.app.Activity
import android.util.Log
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.anlarsinsoftware.memoriesbook.R
import com.anlarsinsoftware.memoriesbook.ui.theme.Tools.myBrush
import com.anlarsinsoftware.memoriesbook.ui.theme.Tools.myButton
import com.anlarsinsoftware.memoriesbook.ui.theme.Tools.myImageButton
import com.anlarsinsoftware.memoriesbook.ui.theme.Tools.mySpacer
import com.anlarsinsoftware.memoriesbook.ui.theme.Tools.myText
import com.anlarsinsoftware.memoriesbook.ui.theme.Tools.myTextField
import com.anlarsinsoftware.memoriesbook.ui.theme.Tools.showToast
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.Firebase
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth

@Composable
fun LoginScreen(navController: NavController) {

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) } // Yükleme durumunu göstermek için

    // Google'dan gelen giriş sonucunu yakalamak için Launcher
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                // Google ID Token'ını alıp Firebase ile giriş yapmaya gönderiyoruz
                firebaseAuthWithGoogle(account.idToken!!) { success ->
                    if (success) {
                        navController.navigate("home_screen") {
                            popUpTo("login_screen") { inclusive = true }
                        }
                    } else {
                        showToast(context, "Firebase ile kimlik doğrulama başarısız.", true)
                        isLoading = false
                    }
                }
            } catch (e: ApiException) {
                Log.w("GoogleSignIn", "Google ile giriş başarısız", e)
                showToast(context, "Google ile giriş başarısız oldu.", true)
                isLoading = false
            }
        } else {
            isLoading = false
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
                "Şifre", false
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
                    navController.navigate("home_screen")
                } else {
                    Log.d("LoginAttempt", "Alanlar boş bırakılamaz.")
                }
            }

            mySpacer(100)
            Card ( modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clickable(enabled = !isLoading) {
                    isLoading = true

                    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(context.getString(R.string.default_web_client_id))
                        .requestEmail()
                        .build()

                    val googleSignInClient = GoogleSignIn.getClient(context, gso)

                    launcher.launch(googleSignInClient.signInIntent)
                },
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ){
                Row (Modifier.fillMaxWidth()
                    .padding(5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                    ){
                    val context = LocalContext.current
                    myImageButton (id= R.drawable.google_ico, imageSize = 50){
                        showToast(context,"SA",false)
                    }
                    Spacer(Modifier.width(20.dp))
                    Text("Google İle Oturum Aç",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp)
                }

            }
            if (isLoading) {
                // CircularProgressIndicator()
            }

        }
    }
}
private fun firebaseAuthWithGoogle(idToken: String, onResult: (Boolean) -> Unit) {
    val credential = GoogleAuthProvider.getCredential(idToken, null)
    Firebase.auth.signInWithCredential(credential)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Başarılı
                onResult(true)
            } else {
                // Başarısız
                onResult(false)
            }
        }
}



@Preview(showBackground = true)
@Composable
fun Preview_Login() {

    LoginScreen(navController = rememberNavController())
}