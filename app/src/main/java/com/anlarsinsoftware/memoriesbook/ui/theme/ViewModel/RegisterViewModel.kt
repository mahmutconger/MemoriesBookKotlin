package com.anlarsinsoftware.memoriesbook.ui.theme.ViewModel

import android.net.Uri
import com.anlarsinsoftware.memoriesbook.R
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// Kayıt işleminin durumunu UI'a bildirmek için
sealed interface RegistrationUiState {
    object Idle : RegistrationUiState      // Başlangıç
    object Loading : RegistrationUiState   // Yükleniyor
    object Success : RegistrationUiState   // Başarılı
    data class Error(val message: String) : RegistrationUiState // Hatalı
}

class RegisterViewModel : ViewModel() {
    private val auth = Firebase.auth

    private val _uiState = MutableStateFlow<RegistrationUiState>(RegistrationUiState.Idle)
    val uiState: StateFlow<RegistrationUiState> = _uiState.asStateFlow()

    fun registerUser(email: String, password: String,userName: String) {
        viewModelScope.launch {
            _uiState.value = RegistrationUiState.Loading // 1. Yükleme durumunu başlat
            try {
                // 2. Firebase işlemini Coroutine ile bekle
                val result = auth.createUserWithEmailAndPassword(email, password).await()

                val user = result.user
                if (user != null) {
                    // 3. Profil güncelleme isteği oluşturuyoruz.
                    val profileUpdates = userProfileChangeRequest {
                        displayName = userName
                    }

                    // 4. İsteği Firebase'e gönderip işlemin bitmesini bekliyoruz.
                    user.updateProfile(profileUpdates).await()
                }
                // 3. İşlem başarılıysa durumu güncelle
                _uiState.value = RegistrationUiState.Success

                // İsteğe bağlı: Kullanıcı bilgilerini (kullanıcı adı vb.) Firestore'a burada kaydedebilirsin.

            } catch (e: Exception) {
                // 4. Hata olursa durumu ve hata mesajını güncelle
                _uiState.value = RegistrationUiState.Error(e.localizedMessage ?: "Bilinmeyen bir hata oluştu.")
            }
        }
    }
}