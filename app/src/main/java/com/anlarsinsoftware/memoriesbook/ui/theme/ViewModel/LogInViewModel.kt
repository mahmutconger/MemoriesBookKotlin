package com.anlarsinsoftware.memoriesbook.ui.theme.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


sealed interface LoginUiState {
    object Idle : LoginUiState      // Başlangıç
    object Loading : LoginUiState   // Yükleniyor
    object Success : LoginUiState   // Başarılı
    data class Error(val message: String) : LoginUiState // Hatalı
}

class LogInViewModel: ViewModel() {
    private val auth = Firebase.auth

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun loginUser(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            try {
                // 2. Firebase işlemini Coroutine ile bekle
                auth.signInWithEmailAndPassword(email, password).await()

                // 3. İşlem başarılıysa durumu güncelle
                _uiState.value = LoginUiState.Success



            } catch (e: Exception) {
                // 4. Hata olursa durumu ve hata mesajını güncelle
                _uiState.value = LoginUiState.Error(e.localizedMessage ?: "Bilinmeyen bir hata oluştu.")
            }
        }
    }

    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading // Ortak yükleme durumunu başlat
            try {
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                auth.signInWithCredential(credential).await() // Coroutine ile bekle
                _uiState.value = LoginUiState.Success // Başarılı
            } catch (e: Exception) {
                _uiState.value = LoginUiState.Error(e.localizedMessage ?: "Google ile giriş sırasında hata oluştu.")
            }
        }
    }
}