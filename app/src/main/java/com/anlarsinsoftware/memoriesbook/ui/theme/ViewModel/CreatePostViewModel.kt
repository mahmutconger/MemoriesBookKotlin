package com.anlarsinsoftware.memoriesbook.ui.theme.ViewModel

import android.app.Application
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import java.util.UUID

// Yükleme durumunu UI'a bildirmek için
sealed interface UploadUiState {
    object Idle : UploadUiState // Başlangıç durumu
    object Loading : UploadUiState
    object Success : UploadUiState
    data class Error(val message: String) : UploadUiState
}

class CreatePostViewModel (application: Application) : AndroidViewModel(application) {
    private val storage = Firebase.storage
    private val firestore = Firebase.firestore
    private val auth = Firebase.auth

    private val _uiState = MutableStateFlow<UploadUiState>(UploadUiState.Idle)
    val uiState: StateFlow<UploadUiState> = _uiState.asStateFlow()

    private val _selectedImageUris = MutableStateFlow<List<Uri>>(emptyList())
    val selectedImageUris: StateFlow<List<Uri>> = _selectedImageUris.asStateFlow()

    private val _selectedVideoUri = MutableStateFlow<Uri?>(null)
    val selectedVideoUri: StateFlow<Uri?> = _selectedVideoUri.asStateFlow()

    // YENİ STATE: Seçilen veya oluşturulan kapak fotoğrafını tutar
    private val _thumbnailBitmap = MutableStateFlow<Bitmap?>(null)
    val thumbnailBitmap: StateFlow<Bitmap?> = _thumbnailBitmap.asStateFlow()

    private val _comment = MutableStateFlow("")
    val comment: StateFlow<String> = _comment.asStateFlow()


    fun onImagesSelected(uris: List<Uri>) {
        _selectedVideoUri.value = null
        _thumbnailBitmap.value = null // Resim seçilince video kapağını temizle
        _selectedImageUris.value = uris
    }

    fun onVideoSelected(uri: Uri?) {
        _selectedImageUris.value = emptyList()
        _selectedVideoUri.value = uri

        // Video seçildiği an, varsayılan kapağı oluştur.
        if (uri != null) {
            generateDefaultThumbnail(uri)
        } else {
            _thumbnailBitmap.value = null
        }
    }

    fun onCommentChange(newComment: String) {
        _comment.value = newComment
    }

    fun onThumbnailCreated(bitmap: Bitmap?) {
        _thumbnailBitmap.value = bitmap
    }

    private fun generateDefaultThumbnail(videoUri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            val retriever = MediaMetadataRetriever()
            try {
                // Artık 'getApplication()' sorunsuz çalışacak
                retriever.setDataSource(getApplication<Application>().applicationContext, videoUri)
                val frame = retriever.getFrameAtTime(1_000_000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                _thumbnailBitmap.value = frame
            } catch (e: Exception) {
                // Hata yönetimi
            } finally {
                retriever.release()
            }
        }
    }

    // 2. createPost FONKSİYONUNU SADELEŞTİRİYORUZ
    fun createPost(visibility: String, visibleTo: List<String>) {
        viewModelScope.launch {
            _uiState.value = UploadUiState.Loading

            // Veriyi artık parametre olarak değil, doğrudan ViewModel'in state'lerinden alıyoruz
            val urisToUpload = if (_selectedImageUris.value.isNotEmpty()) _selectedImageUris.value else _selectedVideoUri.value?.let { listOf(it) }
            val thumbnailToUpload = _thumbnailBitmap.value
            val commentToUpload = _comment.value
            val mediaType = if (_selectedImageUris.value.isNotEmpty()) "image" else "video"

            val user = auth.currentUser
            if (user == null) {
                _uiState.value = UploadUiState.Error("Kullanıcı giriş yapmamış.")
                return@launch
            }
            if (urisToUpload.isNullOrEmpty()) {
                _uiState.value = UploadUiState.Error("Lütfen bir medya dosyası seçin.")
                return@launch
            }

            try {
                // Paralel yükleme mantığı
                val downloadUrls = urisToUpload.map { uri ->
                    async(Dispatchers.IO) {
                        val fileExtension = if (mediaType == "video") "mp4" else "jpg"
                        val fileName = "media/${UUID.randomUUID()}.$fileExtension"
                        storage.reference.child(fileName).putFile(uri).await()
                            .storage.downloadUrl.await().toString()
                    }
                }.awaitAll()

                var thumbnailUrl = ""
                if (thumbnailToUpload != null) {
                    val baos = ByteArrayOutputStream()
                    thumbnailToUpload.compress(Bitmap.CompressFormat.JPEG, 80, baos)
                    val data = baos.toByteArray()
                    val thumbnailName = "thumbnails/${UUID.randomUUID()}.jpg"
                    thumbnailUrl = storage.reference.child(thumbnailName)
                        .putBytes(data).await()
                        .storage.downloadUrl.await().toString()
                }

                // Post verisini hazırlama
                val postData = hashMapOf(
                    "authorId" to user.uid,
                    "useremail" to user.email,
                    "authorUsername" to (user.displayName ?: ""),
                    "authorPhotoUrl" to (user.photoUrl?.toString() ?: ""),
                    "comment" to commentToUpload,
                    "date" to Timestamp.now(),
                    "mediaType" to mediaType,
                    "mediaUrls" to downloadUrls,
                    "thumbnailUrl" to thumbnailUrl,
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