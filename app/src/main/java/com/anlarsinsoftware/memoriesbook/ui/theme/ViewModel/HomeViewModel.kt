package com.anlarsinsoftware.memoriesbook.ui.theme.ViewModel

import android.app.DownloadManager
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anlarsinsoftware.memoriesbook.ui.theme.Model.Comments
import com.anlarsinsoftware.memoriesbook.ui.theme.Model.Posts
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Locale

class HomeViewModel : ViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    // Verileri dışarıya sadece okunabilir StateFlow olarak sunuyoruz.
    private val _posts = MutableStateFlow<List<Posts>>(emptyList())
    val posts: StateFlow<List<Posts>> = _posts.asStateFlow()

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()


    init {
        fetchFeedPosts()
    }

    fun fetchFeedPosts() {
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading
            val currentUser = auth.currentUser ?: run {
                _uiState.value = HomeUiState.Error("Kullanıcı giriş yapmamış.")
                return@launch
            }

            try {
                // Sorgu 1: Herkese açık tüm postları çek
                val publicPostsQuery = firestore.collection("posts")
                    .whereEqualTo("visibility", "public")
                    .get().await()

                // Sorgu 2: Özel olup benim görme iznim olan postları çek
                val privatePostsForMeQuery = firestore.collection("posts")
                    .whereEqualTo("visibility", "private")
                    .whereArrayContains("visibleTo", currentUser.uid)
                    .get().await()

                // Sorgu 3: Kendi özel postlarımı da ekleyelim
                val myPrivatePostsQuery = firestore.collection("posts")
                    .whereEqualTo("visibility", "private")
                    .whereEqualTo("authorId", currentUser.uid)
                    .get().await()

                val publicPosts = publicPostsQuery.toObjects(Posts::class.java)
                val privatePostsForMe = privatePostsForMeQuery.toObjects(Posts::class.java)
                val myPrivatePosts = myPrivatePostsQuery.toObjects(Posts::class.java)

                val feedList = (publicPosts + privatePostsForMe + myPrivatePosts)
                    .distinctBy { it.documentId }
                    .sortedByDescending { it.date } // Tarihe göre sırala

                _posts.value = feedList
                _uiState.value = HomeUiState.Success

            } catch (e: Exception) {
                Log.e("FirestoreFeed", "Error fetching feed posts", e)
                _uiState.value = HomeUiState.Error(e.localizedMessage ?: "Bilinmeyen bir hata oluştu.")
            }
        }
    }

    fun updatePostComment(postId: String, newComment: String) {
        firestore.collection("posts").document(postId).update("comment", newComment)
    }

    fun deletePost(postId: String) {
        firestore.collection("posts").document(postId).delete()
    }

    fun toggleLikeStatus(postId: String, isLiked: Boolean) {
    }

    fun onPostLikeClicked(post: Posts) {
        _posts.update { currentPosts ->
            currentPosts.map { p ->
                if (p.documentId == post.documentId) {
                    p.copy(isLiked = !p.isLiked)
                } else {
                    p
                }
            }
        }
        firestore.collection("posts").document(post.documentId)
            .update("isLiked", !post.isLiked)
            .addOnSuccessListener {
                Log.d("Firestore", "Post like status updated successfully.")
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error updating post like status.", e)
            }
    }


    sealed interface HomeUiState {
        object Loading : HomeUiState
        object Success : HomeUiState
        data class Error(val message: String) : HomeUiState
    }
}
