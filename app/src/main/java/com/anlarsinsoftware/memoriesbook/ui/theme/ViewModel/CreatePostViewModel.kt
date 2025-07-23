package com.anlarsinsoftware.memoriesbook.ui.theme.ViewModel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
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
        mediaUris: List<Uri>,
        comment: String,
        mediaType: String,
        visibility: String,
        visibleTo: List<String>
    ) {
        viewModelScope.launch {
            _uiState.value = UploadUiState.Loading

            val user = auth.currentUser
            if (user == null) {
                _uiState.value = UploadUiState.Error("Kullanıcı giriş yapmamış.")
                return@launch
            }

            try {
                val downloadUrls = mediaUris.map { uri ->
                    async {
                        val fileExtension = if (mediaType == "video") "mp4" else "jpg"
                        val fileName = "media/${UUID.randomUUID()}.$fileExtension"
                        storage.reference.child(fileName).putFile(uri).await()
                            .storage.downloadUrl.await().toString()
                    }
                }.awaitAll()

                val postData = hashMapOf(
                    "authorId" to user.uid,
                    "useremail" to user.email,
                    "username" to user.displayName,
                    "userPhoto" to user.photoUrl,
                    "comment" to comment,
                    "date" to Timestamp.now(),
                    "mediaType" to mediaType,
                    "mediaUrls" to downloadUrls,
                    "visibility" to visibility,
                    "visibleTo" to if (visibility == "private") visibleTo else emptyList(),
                    "likedBy" to emptyList<String>(),
                    "commentCount" to 0
                )

                firestore.collection("posts").add(postData).await()
                _uiState.value = UploadUiState.Success
            } catch (e: Exception) {
                _uiState.value = UploadUiState.Error(e.localizedMessage ?: "Bilinmeyen bir hata oluştu.")
            }
        }
    }
}