package com.anlarsinsoftware.memoriesbook.ui.theme.ViewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anlarsinsoftware.memoriesbook.ui.theme.Model.Posts
import com.anlarsinsoftware.memoriesbook.ui.theme.Model.UserStatus
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {
    private val auth = Firebase.auth
    private val firestore = Firebase.firestore
    private val database = Firebase.database.reference


    private val _posts = MutableStateFlow<List<Posts>>(emptyList())
    val posts: StateFlow<List<Posts>> = _posts.asStateFlow()

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            // 1. Üç farklı veri akışını (Flow) oluşturuyoruz.
            val publicPostsFlow = getPublicPostsFlow()
            val privateForMeFlow = getPrivateForMePostsFlow(currentUser.uid)
            val myPrivatePostsFlow = getMyPrivatePostsFlow(currentUser.uid)

            viewModelScope.launch {
                // 2. Bu üç akışı 'combine' ile birleştiriyoruz. Herhangi biri güncellendiğinde, bu blok çalışır.
                combine(publicPostsFlow, privateForMeFlow, myPrivatePostsFlow) { public, privateForMe, myPrivate ->
                    // 3. Sonuçları birleştir, tekrarları sil ve sırala.
                    (public + privateForMe + myPrivate)
                        .distinctBy { it.documentId }
                        .sortedByDescending { it.date }
                }.collect { combinedAndSortedList ->
                    // 4. Son listeyi UI'ın dinlediği StateFlow'a ata.
                    _posts.value = combinedAndSortedList
                    _uiState.value = HomeUiState.Success
                    Log.d("ViewModelSuccess", "Feed updated with ${combinedAndSortedList.size} posts.")
                }
            }
        } else {
            _uiState.value = HomeUiState.Error("Kullanıcı giriş yapmamış.")
        }
    }

    // Herkese açık postları dinleyen Flow
    private fun getPublicPostsFlow(): Flow<List<Posts>> = callbackFlow {
        val listener = firestore.collection("posts")
            .whereEqualTo("visibility", "public")
            .addSnapshotListener { snapshot, e ->
                if (e != null) { close(e); return@addSnapshotListener }
                val postsWithIds = snapshot?.documents?.map { doc ->
                    val post = doc.toObject(Posts::class.java)
                    post?.copy(documentId = doc.id)

                }?.filterNotNull() ?: emptyList() // null olanları temizle

                trySend(postsWithIds)
            }
        awaitClose { listener.remove() } // Coroutine iptal edildiğinde dinleyiciyi kaldır
    }

    // Bana özel postları dinleyen Flow
    private fun getPrivateForMePostsFlow(userId: String): Flow<List<Posts>> = callbackFlow {
        val listener = firestore.collection("posts")
            .whereEqualTo("visibility", "private")
            .whereArrayContains("visibleTo", userId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) { close(e); return@addSnapshotListener }
                val posts = snapshot?.toObjects(Posts::class.java) ?: emptyList()
                trySend(posts)
            }
        awaitClose { listener.remove() }
    }

    // Benim özel postlarımı dinleyen Flow
    private fun getMyPrivatePostsFlow(userId: String): Flow<List<Posts>> = callbackFlow {
        val listener = firestore.collection("posts")
            .whereEqualTo("visibility", "private")
            .whereEqualTo("authorId", userId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) { close(e); return@addSnapshotListener }
                val posts = snapshot?.toObjects(Posts::class.java) ?: emptyList()
                trySend(posts)
            }
        awaitClose { listener.remove() }
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

    fun setUserOnline() {
        val uid = auth.currentUser?.uid ?: return
        val userStatusRef = database.child("status").child(uid)

        // Kullanıcının online olduğunu ve son görülme zamanını yaz
        userStatusRef.child("isOnline").setValue(true)
        userStatusRef.child("last_seen").setValue(ServerValue.TIMESTAMP)

        // Bağlantı koptuğunda ne olacağını sunucuya bildir
        userStatusRef.child("isOnline").onDisconnect().setValue(false)
        userStatusRef.child("last_seen").onDisconnect().setValue(ServerValue.TIMESTAMP)
    }

    // Kullanıcı manuel olarak çıkış yaptığında bu fonksiyon çağrılabilir
    fun setUserOffline() {
        val uid = auth.currentUser?.uid ?: return
        database.child("status").child(uid).child("isOnline").setValue(false)
        database.child("status").child(uid).child("last_seen").setValue(ServerValue.TIMESTAMP)
    }

    // Bir arkadaşın durumunu canlı dinlemek için (ChatDetailViewModel'e konulabilir)
    fun observeUserStatus(friendId: String): Flow<UserStatus> = callbackFlow {
        val ref = database.child("status").child(friendId)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val isOnline = snapshot.child("isOnline").getValue(Boolean::class.java) ?: false
                val lastSeen = snapshot.child("last_seen").getValue(Long::class.java)
                trySend(UserStatus(isOnline, lastSeen))
            }
            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) } // Dinleyiciyi kaldır
    }


    sealed interface HomeUiState {
        object Loading : HomeUiState
        object Success : HomeUiState
        data class Error(val message: String) : HomeUiState
    }
}
