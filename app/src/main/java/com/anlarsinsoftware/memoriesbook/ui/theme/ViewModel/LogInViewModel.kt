package com.anlarsinsoftware.memoriesbook.ui.theme.ViewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


sealed interface LoginUiState {
    object Idle : LoginUiState
    object Loading : LoginUiState
    object Success : LoginUiState
    data class Error(val message: String) : LoginUiState
}

class LogInViewModel : ViewModel() {
    private val auth = Firebase.auth
    private val firestore = Firebase.firestore
    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun loginUser(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            try {
                val result= auth.signInWithEmailAndPassword(email, password).await()
                result.user?.let { updateUserLastSignInAndToken(it.uid) }
                _uiState.value = LoginUiState.Success


            } catch (e: Exception) {
                _uiState.value =
                    LoginUiState.Error(e.localizedMessage ?: "Bilinmeyen bir hata oluştu.")
            }
        }
    }

    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            try {
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                val result = auth.signInWithCredential(credential).await()
                // GİRİŞ BAŞARILI, ŞİMDİ ZAMANI GÜNCELLE
                result.user?.let { updateUserLastSignInAndToken(it.uid) }

                _uiState.value = LoginUiState.Success
            } catch (e: Exception) {
                _uiState.value = LoginUiState.Error(
                    e.localizedMessage ?: "Google ile giriş sırasında hata oluştu."
                )
            }
        }
    }

    private suspend fun updateUserLastSignInAndToken(uid: String) {
        try {
            // Cihazın en güncel FCM token'ını al
            val token = Firebase.messaging.token.await()

            // Hem son giriş tarihini hem de token'ı tek bir işlemde güncelle
            firestore.collection("Users").document(uid)
                .update(mapOf(
                    "lastSignIn" to Timestamp.now(),
                    "fcmToken" to token
                )).await()
            Log.d("Login", "User last sign-in and FCM token updated.")
        } catch (e: Exception) {
            Log.w("Login", "Could not update user token.", e)
        }
    }


    private suspend fun updateUserLastSignIn(uid: String) {
        try {
            firestore.collection("Users").document(uid)
                .update("lastSignIn", com.google.firebase.Timestamp.now())
                .await()
            Log.d("Login", "User last sign-in time updated.")
        } catch (e: Exception) {
            Log.w("Login", "Could not update last sign-in time.", e)
        }
    }
}