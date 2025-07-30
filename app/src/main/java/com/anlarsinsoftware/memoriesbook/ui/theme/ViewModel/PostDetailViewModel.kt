package com.anlarsinsoftware.memoriesbook.ui.theme.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anlarsinsoftware.memoriesbook.ui.theme.Model.Posts
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class PostDetailViewModel(private val postId: String) : ViewModel() {
    private val firestore = Firebase.firestore

    private val _post = MutableStateFlow<Posts?>(null)
    val post: StateFlow<Posts?> = _post.asStateFlow()

    init {
        // ViewModel ilk oluşturulduğunda post detaylarını çek
        fetchPostDetails()
    }

    private fun fetchPostDetails() {
        viewModelScope.launch {
            if (postId.isBlank()) return@launch
            try {
                val docSnapshot = firestore.collection("posts").document(postId).get().await()
                // Döküman ID'sini de nesneye eklemeyi unutmuyoruz
                val postObject = docSnapshot.toObject(Posts::class.java)?.copy(documentId = docSnapshot.id)
                _post.value = postObject
            } catch (e: Exception) {
                // Hata yönetimi
            }
        }
    }
}

// ViewModel'e postId'yi iletmek için bir Factory (Üretici)
class PostDetailViewModelFactory(private val postId: String) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PostDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PostDetailViewModel(postId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}