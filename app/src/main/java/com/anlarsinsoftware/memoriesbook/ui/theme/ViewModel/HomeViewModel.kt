package com.anlarsinsoftware.memoriesbook.ui.theme.ViewModel

import android.app.DownloadManager
import android.util.Log
import androidx.lifecycle.ViewModel
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
import java.text.SimpleDateFormat
import java.util.Locale

class HomeViewModel : ViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    // Verileri dışarıya sadece okunabilir StateFlow olarak sunuyoruz.
    private val _posts = MutableStateFlow<List<Posts>>(emptyList())
    val posts: StateFlow<List<Posts>> = _posts.asStateFlow()

    private val _comments = MutableStateFlow<List<Comments>>(emptyList())
    val comments: StateFlow<List<Comments>> = _comments.asStateFlow()

    init {
        getData()
    }

    // FeedActivity'deki getData() metodunun ViewModel versiyonu
    private fun getData() {
        firestore.collection("posts").orderBy("date", Query.Direction.DESCENDING)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    Log.e("FirestoreError", "Error fetching posts: ${error.message}")
                    return@addSnapshotListener
                }
                if (value != null) {
                    val postList = value.documents.map { snapshot ->
                        val data = snapshot.data

                        val timestamp = data?.get("date") as? Timestamp
                        val date = timestamp?.toDate()?.let {
                            SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(it)
                        } ?: ""

                        Posts(
                            email = data?.get("useremail") as? String ?: "",
                            comment = data?.get("comment") as? String ?: "",
                            downloadUrl = data?.get("downloadurl") as? String ?: "",
                            documentId = snapshot.id,
                            isLiked = data?.get("isLiked") as? Boolean ?: false,
                            date = date
                        )
                    }
                    _posts.value = postList
                    Log.d(
                        "FirestoreSuccess",
                        "Successfully fetched and updated ${postList.size} posts."
                    )
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
}
