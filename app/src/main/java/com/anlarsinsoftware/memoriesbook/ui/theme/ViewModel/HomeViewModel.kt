package com.anlarsinsoftware.memoriesbook.ui.theme.ViewModel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anlarsinsoftware.memoriesbook.ui.theme.Model.FriendProfile
import com.anlarsinsoftware.memoriesbook.ui.theme.Model.Posts
import com.anlarsinsoftware.memoriesbook.ui.theme.Model.UserStatus
import com.anlarsinsoftware.memoriesbook.ui.theme.Util.showToast
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class HomeViewModel : ViewModel() {
    private val auth = Firebase.auth
    private val firestore = Firebase.firestore
    private val database = Firebase.database.reference


    private val _posts = MutableStateFlow<List<Posts>>(emptyList())
    val posts: StateFlow<List<Posts>> = _posts.asStateFlow()

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _postLikers = MutableStateFlow<List<FriendProfile>>(emptyList())
    val postLikers: StateFlow<List<FriendProfile>> = _postLikers.asStateFlow()



    init {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            // 1. Üç farklı veri akışını (Flow) oluşturuyoruz.
            val publicPostsFlow = getPublicPostsFlow()
            val privateForMeFlow = getPrivateForMePostsFlow(currentUser.uid)
            val myPrivatePostsFlow = getMyPrivatePostsFlow(currentUser.uid)

            viewModelScope.launch {
                // 2. Bu üç akışı 'combine' ile birleştiriyoruz. Herhangi biri güncellendiğinde, bu blok çalışır.
                combine(
                    publicPostsFlow,
                    privateForMeFlow,
                    myPrivatePostsFlow
                ) { public, privateForMe, myPrivate ->
                    // 3. Sonuçları birleştir, tekrarları sil ve sırala.
                    (public + privateForMe + myPrivate)
                        .distinctBy { it.documentId }
                        .sortedByDescending { it.date }
                }.collect { combinedAndSortedList ->
                    // 4. Son listeyi UI'ın dinlediği StateFlow'a ata.
                    _posts.value = combinedAndSortedList
                    _uiState.value = HomeUiState.Success
                    Log.d(
                        "ViewModelSuccess",
                        "Feed updated with ${combinedAndSortedList.size} posts."
                    )
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
                if (e != null) {
                    close(e); return@addSnapshotListener
                }
                val postsWithIds = snapshot?.documents?.map { doc ->
                    val post = doc.toObject(Posts::class.java)
                    post?.copy(documentId = doc.id)

                }?.filterNotNull() ?: emptyList() // null olanları temizle

                trySend(postsWithIds)
            }
        awaitClose { listener.remove() } // Coroutine iptal edildiğinde dinleyiciyi kaldır
    }

    // Bana özel postları dinleyen Flow
    // HomeViewModel.kt içinde

    private fun getPrivateForMePostsFlow(userId: String): Flow<List<Posts>> = callbackFlow {
        val listener = firestore.collection("posts")
            .whereEqualTo("visibility", "private")
            .whereArrayContains("visibleTo", userId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) { close(e); return@addSnapshotListener }


                val postsWithIds = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Posts::class.java)?.copy(documentId = doc.id)
                } ?: emptyList()

                trySend(postsWithIds)
            }
        awaitClose { listener.remove() }
    }

    // HomeViewModel.kt içinde

    private fun getMyPrivatePostsFlow(userId: String): Flow<List<Posts>> = callbackFlow {
        val listener = firestore.collection("posts")
            .whereEqualTo("visibility", "private")
            .whereEqualTo("authorId", userId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) { close(e); return@addSnapshotListener }


                val postsWithIds = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Posts::class.java)?.copy(documentId = doc.id)
                } ?: emptyList()

                trySend(postsWithIds)
            }
        awaitClose { listener.remove() }
    }

    fun deletePost(postId: String, context: Context) {
        firestore.collection("posts").document(postId).delete()
            .addOnSuccessListener {
                showToast(context, "Gönderi başarıyla silindi")
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Post silinirken hata oluştu.", e)
            }
    }

    fun updatePostComment(postId: String, newComment: String, onResult: (Boolean) -> Unit) {
        firestore.collection("posts").document(postId)
            .update("comment", newComment)
            .addOnSuccessListener {
                Log.d("Firestore", "Post comment updated successfully.")
                onResult(true)
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error updating comment.", e)
                onResult(false)
            }
    }

    fun updatePostVisibility(
        postId: String,
        newVisibility: String,
        newVisibleToList: List<String>,
        onResult: (Boolean) -> Unit
    ) {
        val updates = mapOf(
            "visibility" to newVisibility,
            "visibleTo" to if (newVisibility == "private") newVisibleToList else emptyList()
        )
        firestore.collection("posts").document(postId).update(updates)
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { onResult(false) }
    }


    fun addToFavorites(postId: String) {
        val currentUser = auth.currentUser ?: return
        // Bu fonksiyon, kullanıcının 'favorites' adlı bir dizisine post ID'sini ekleyebilir.
        // Veya postun içinde 'favoritedBy' adlı bir diziye kullanıcı ID'si eklenebilir.
        // Şimdilik sadece bir log atalım.
        Log.d("ViewModel", "Post $postId favorilere eklendi by ${currentUser.uid}")
    }


    fun onPostLikeClicked(post: Posts) {
        val currentUser = auth.currentUser ?: return
        val currentUid = currentUser.uid

        val isAlreadyLiked = post.likedBy.contains(currentUid)

        // Önce UI'ı hızlıca güncelle (Optimistic Update)
        _posts.update { currentPosts ->
            currentPosts.map { p ->
                if (p.documentId == post.documentId) {
                    val newLikedBy = if (isAlreadyLiked) {
                        p.likedBy - currentUid
                    } else {
                        p.likedBy + currentUid
                    }
                    p.copy(likedBy = newLikedBy)
                } else {
                    p
                }
            }
        }

        // Sonra değişikliği Firestore'a kaydet
        val postRef = firestore.collection("posts").document(post.documentId)
        if (isAlreadyLiked) {
            // Beğeniyi geri al
            postRef.update("likedBy", FieldValue.arrayRemove(currentUid))
        } else {
            // Beğen
            postRef.update("likedBy", FieldValue.arrayUnion(currentUid))
        }
    }

    // Beğenenlerin listesini çekmek için yeni fonksiyon
    fun fetchLikersForPost(post: Posts) {
        if (post.likedBy.isEmpty()) {
            _postLikers.value = emptyList()
            return
        }
        viewModelScope.launch {
            try {
                val usersQuery = firestore.collection("Users")
                    .whereIn(FieldPath.documentId(), post.likedBy)
                    .get().await()
                _postLikers.value = usersQuery.toObjects(FriendProfile::class.java)
            } catch (e: Exception) {
                // Hata yönetimi
            }
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
