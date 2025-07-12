package com.anlarsinsoftware.memoriesbook.ui.theme.ViewModel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

// Yükleme durumunu UI'a bildirmek için
sealed interface UploadUiState {
    object Idle : UploadUiState // Başlangıç durumu
    object Loading : UploadUiState
    object Success : UploadUiState
    data class Error(val message: String) : UploadUiState
}

    class CreatePostViewModel : ViewModel() {
    private val storage = Firebase.storage
    private val firestore = Firebase.firestore
    private val auth = Firebase.auth

    private val _uiState = MutableStateFlow<UploadUiState>(UploadUiState.Idle)
    val uiState: StateFlow<UploadUiState> = _uiState.asStateFlow()

    fun createPost(
        imageUri: Uri,
        comment: String,
        visibility: String, // "public" veya "private"
        visibleTo: List<String> // Özelse, kimlerin göreceği
    ) {
        viewModelScope.launch {
            _uiState.value = UploadUiState.Loading // Yükleme başladı

            val user = auth.currentUser
            if (user == null) {
                _uiState.value = UploadUiState.Error("Kullanıcı giriş yapmamış.")
                return@launch
            }

            try {
                // 1. Resmi Firebase Storage'a yükle
                val imageName = "images/${UUID.randomUUID()}.jpg"
                val downloadUrl = storage.reference.child(imageName)
                    .putFile(imageUri).await() // .await() ile callback beklemeden sonucu al
                    .storage.downloadUrl.await() // Yüklenen resmin URL'sini al
                    .toString()

                // 2. Post verisini hazırla
                val postData = hashMapOf(
                    "authorId" to user.uid,
                    "useremail" to user.email,
                    "downloadurl" to downloadUrl,
                    "comment" to comment,
                    "date" to Timestamp.now(),
                    "visibility" to visibility,
                    "visibleTo" to if (visibility == "private") visibleTo else emptyList()
                )

                // 3. Post verisini Firestore'a kaydet
                firestore.collection("posts").add(postData).await()

                _uiState.value = UploadUiState.Success // İşlem başarılı

            } catch (e: Exception) {
                _uiState.value = UploadUiState.Error(e.localizedMessage ?: "Bilinmeyen bir hata oluştu.")
            }
        }
    }
}