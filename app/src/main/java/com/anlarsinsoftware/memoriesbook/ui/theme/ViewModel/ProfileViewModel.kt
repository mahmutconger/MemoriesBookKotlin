package com.anlarsinsoftware.memoriesbook.ui.theme.ViewModel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

// Güncelleme işleminin durumunu UI'a bildirmek için
sealed interface UpdateProfileUiState {
    object Idle : UpdateProfileUiState
    object Loading : UpdateProfileUiState
    object Success : UpdateProfileUiState
    data class Error(val message: String) : UpdateProfileUiState
}

class ProfileViewModel : ViewModel() {
    private val auth = Firebase.auth
    private val storage = Firebase.storage
    private val firestore = Firebase.firestore

    private val _uiState = MutableStateFlow<UpdateProfileUiState>(UpdateProfileUiState.Idle)
    val uiState: StateFlow<UpdateProfileUiState> = _uiState.asStateFlow()

    fun updateUserProfile(newDisplayName: String, newImageUri: Uri?) {
        viewModelScope.launch {
            _uiState.value = UpdateProfileUiState.Loading
            val user = auth.currentUser ?: run {
                _uiState.value = UpdateProfileUiState.Error("Kullanıcı bulunamadı.")
                return@launch
            }

            try {
                // 1. Adım: Eğer yeni bir resim seçildiyse, onu Firebase Storage'a yükle
                val photoUrl = if (newImageUri != null) {
                    val imageName = "profile_pictures/${user.uid}/${UUID.randomUUID()}.jpg"
                    storage.reference.child(imageName)
                        .putFile(newImageUri).await()
                        .storage.downloadUrl.await()
                        .toString()
                } else {
                    user.photoUrl?.toString() // Resim değişmediyse mevcut URL'yi kullan
                }

                // 2. Adım: Firebase Authentication profilini güncelle
                val profileUpdates = userProfileChangeRequest {
                    displayName = newDisplayName
                    photoUri = Uri.parse(photoUrl)
                }
                user.updateProfile(profileUpdates).await()

                // 3. Adım: Firestore'daki 'Users' dökümanını güncelle
                val userDocRef = firestore.collection("Users").document(user.uid)
                val updates = mapOf(
                    "username" to newDisplayName,
                    "photoUrl" to photoUrl
                )
                userDocRef.update(updates).await()

                _uiState.value = UpdateProfileUiState.Success

            } catch (e: Exception) {
                _uiState.value = UpdateProfileUiState.Error(e.localizedMessage ?: "Profil güncellenirken bir hata oluştu.")
            }
        }
    }

    // UI state'ini başlangıç durumuna döndürmek için
    fun resetUiState() {
        _uiState.value = UpdateProfileUiState.Idle
    }
}