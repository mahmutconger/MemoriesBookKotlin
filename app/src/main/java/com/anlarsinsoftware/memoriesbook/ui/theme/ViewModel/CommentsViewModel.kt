package com.anlarsinsoftware.memoriesbook.ui.theme.ViewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anlarsinsoftware.memoriesbook.ui.theme.Model.Comments
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Locale

class CommentsViewModel : ViewModel() {
    val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    private val _comments = MutableStateFlow<List<Comments>>(emptyList())
    val comments: StateFlow<List<Comments>> = _comments.asStateFlow()

    private val _commentLikers = MutableStateFlow<List<FriendProfile>>(emptyList())
    val commentLikers: StateFlow<List<FriendProfile>> = _commentLikers.asStateFlow()




    fun fetchCommentsForPost(postId: String) {
        firestore.collection("posts")
            .document(postId)
            .collection("comments")
            // Not: Hem 'date' hem 'timestamp' olabileceği için sıralamayı
            // şimdilik en güvenli olan 'date' üzerinden yapalım.
            // En iyisi tüm veriyi 'date'e çevirmektir.
            .orderBy("date", Query.Direction.ASCENDING)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    return@addSnapshotListener
                }

                if (value != null) {
                    val commentList = value.documents.map { snapshot ->
                        val data = snapshot.data

                        // --- GÜNCELLENMİŞ VE GÜVENLİ VERİ OKUMA MANTIĞI ---

                        // 1. Tarihi Oku: Önce 'date' alanını ara, yoksa 'timestamp' alanını ara.
                        val dateValue = (data?.get("date") ?: data?.get("timestamp")) as? Timestamp
                            ?: Timestamp.now()

                        // 2. Kullanıcı Adını Oku: Önce 'username' ara, yoksa 'email' ara, o da yoksa 'user' ara.
                        val usernameValue = data?.get("username") as? String
                            ?: data?.get("email") as? String
                            ?: data?.get("user") as? String
                            ?: "Bilinmeyen"

                        // 3. Kullanıcı ID'sini Oku: Hem 'userId' hem de 'userid' formatını kontrol et.
                        val userIdValue = data?.get("userId") as? String
                            ?: data?.get("userid") as? String
                            ?: ""

                        // 4. Diğer alanları oku.
                        val commentValue = data?.get("comment") as? String ?: ""
                        val photoUrlValue = data?.get("userPhotoUrl") as? String ?: ""
                        val likedByValue = data?.get("likedBy") as? List<String> ?: emptyList()

                        // Veriyi data class'a ata
                        Comments(
                            comment = commentValue,
                            date = dateValue,
                            username = usernameValue,
                            userId = userIdValue,
                            userPhotoUrl = photoUrlValue,
                            documentId = snapshot.id,
                            postId = postId,
                            likedBy =likedByValue
                        )
                    }
                    _comments.value = commentList
                }
            }
    }

    fun fetchLikersForComment(comment: Comments) {
        // Eğer beğenen kimse yoksa, listeyi boşalt ve işlemi bitir.
        if (comment.likedBy.isEmpty()) {
            _commentLikers.value = emptyList()
            return
        }

        viewModelScope.launch {
            try {
                val usersQuery = firestore.collection("Users")
                    .whereIn(FieldPath.documentId(), comment.likedBy)
                    .get().await()

                _commentLikers.value = usersQuery.toObjects(FriendProfile::class.java)
            } catch (e: Exception) {
                Log.e("FetchLikers", "Error fetching comment likers", e)
                _commentLikers.value = emptyList()
            }
        }
    }

    fun onCommentLikeClicked(comment: Comments) {
        val currentUser = FirebaseAuth.getInstance().currentUser ?: return
        val currentUid = currentUser.uid

        val isAlreadyLiked = comment.likedBy.contains(currentUid)
        _comments.update { currentComments ->
            currentComments.map {c ->
                if (c.documentId == comment.documentId) {
                    val newLikedBy = if (isAlreadyLiked) {
                        c.likedBy - currentUid
                    } else {
                        c.likedBy + currentUid
                    }
                    c.copy(likedBy = newLikedBy)
                } else {
                    c
                }
            }
        }
       val likedRef = firestore.collection("posts")
            .document(comment.postId)
            .collection("comments")
            .document(comment.documentId)

        if (isAlreadyLiked) {
            // Beğeniyi geri al
            likedRef.update("likedBy", FieldValue.arrayRemove(currentUid))
        } else {
            // Beğen
            likedRef.update("likedBy", FieldValue.arrayUnion(currentUid))
        }

    }

    fun addComment(postId: String, commentText: String) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null || commentText.isBlank()) {
            return
        }


        val commentData = hashMapOf(
            "comment" to commentText,
            "date" to Timestamp.now(),
            "userId" to currentUser.uid,
            "username" to (currentUser.displayName ?: "İsimsiz Kullanıcı"),
            "userPhotoUrl" to (currentUser.photoUrl?.toString() ?: ""),
            "likedBy" to emptyList<String>()
        )

        val postRef = firestore.collection("posts").document(postId)
        val commentRef = postRef.collection("comments")

        firestore.runBatch { batch ->
            batch.set(commentRef.document(), commentData)
            batch.update(postRef, "commentCount", FieldValue.increment(1))
        }
            .addOnSuccessListener { Log.d("Firestore", "Comment added and count incremented!") }
            .addOnFailureListener { e -> Log.e("Firestore", "Error in batch write", e) }
    }


}