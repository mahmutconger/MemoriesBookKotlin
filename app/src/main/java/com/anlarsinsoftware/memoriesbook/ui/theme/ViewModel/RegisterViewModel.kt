package com.anlarsinsoftware.memoriesbook.ui.theme.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.firestore
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
    private val firestore = Firebase.firestore

    private val _uiState = MutableStateFlow<RegistrationUiState>(RegistrationUiState.Idle)
    val uiState: StateFlow<RegistrationUiState> = _uiState.asStateFlow()


    fun registerUser(email: String, password: String, userName: String) {
        viewModelScope.launch {
            _uiState.value = RegistrationUiState.Loading
            try {
                // 1. Kullanıcı Auth'ta oluşturuluyor
                val result = auth.createUserWithEmailAndPassword(email, password).await()
                val user = result.user

                if (user != null) {
                    // 2. Kullanıcının görünen adı Auth'ta güncelleniyor
                    val profileUpdates = userProfileChangeRequest {
                        displayName = userName
                    }
                    user.updateProfile(profileUpdates).await()

                    val userMap = hashMapOf(
                        "uid" to user.uid,
                        "username" to userName,
                        "email" to email,
                        "photoUrl" to "", // Başlangıçta profil fotoğrafı boş
                        "following" to emptyList<String>(), // Başlangıçta takip listeleri boş
                        "followers" to emptyList<String>(),
                        "friends" to emptyList<String>()
                    )

                    // 4. 'Users' koleksiyonuna, kullanıcının UID'sini döküman ID'si olarak kullanarak veriyi kaydediyoruz.
                    firestore.collection("Users").document(user.uid).set(userMap).await()
                    // --- YENİ KISIM SONU ---
                }

                // 5. Tüm işlemler başarılıysa durumu güncelle
                _uiState.value = RegistrationUiState.Success

            } catch (e: Exception) {
                _uiState.value = RegistrationUiState.Error(e.localizedMessage ?: "Bilinmeyen bir hata oluştu.")
            }
        }
    }

    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            _uiState.value = RegistrationUiState.Loading
            try {
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                val result = auth.signInWithCredential(credential).await()

                val user = result.user
                // KRİTİK KONTROL: Bu kullanıcı yeni mi oluşturuldu?
                val isNewUser = result.additionalUserInfo?.isNewUser ?: false

                if (user != null && isNewUser) {
                    // Eğer kullanıcı yeniyse, Firestore'da onun için bir döküman oluştur.
                    val userMap = hashMapOf(
                        "uid" to user.uid,
                        "username" to user.displayName,
                        "email" to user.email,
                        "photoUrl" to user.photoUrl?.toString(),
                        "following" to emptyList<String>(),
                        "followers" to emptyList<String>(),
                        "friends" to emptyList<String>()
                    )
                    firestore.collection("Users").document(user.uid).set(userMap).await()
                }

                _uiState.value = RegistrationUiState.Success

            } catch (e: Exception) {
                _uiState.value = RegistrationUiState.Error(e.localizedMessage ?: "Google ile kayıt sırasında bir hata oluştu.")
            }
        }
    }
}