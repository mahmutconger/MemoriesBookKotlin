package com.anlarsinsoftware.memoriesbook.ui.theme.ViewModel

import android.graphics.Color
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.Log
import android.widget.Toast
import androidx.datastore.dataStore
import androidx.lifecycle.ViewModel
import com.anlarsinsoftware.memoriesbook.ui.theme.Model.Comments
import com.anlarsinsoftware.memoriesbook.ui.theme.Tools.showToast
import com.google.firebase.Timestamp
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.text.SimpleDateFormat
import java.util.Locale

class CommentsViewModel: ViewModel(){
    val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    private val _comments = MutableStateFlow<List<Comments>>(emptyList())
    val comments: StateFlow<List<Comments>> = _comments.asStateFlow()


     fun fetchCommentsForPost(postId: String) {
        firestore.collection("posts")
            .document(postId)
            .collection("comments")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener(EventListener<QuerySnapshot>
            { value: QuerySnapshot?, error: FirebaseFirestoreException? ->
                if (error != null) {
                    return@EventListener
                }
                if (value != null) {
                   val commentList = value.documents.map { snapshot ->
                       val data = snapshot.data
                       val timestamp = data?.get("timestamp") as? Timestamp
                       val date = timestamp?.toDate()?.let {
                           SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(it)
                       } ?: ""

                       Comments(
                           comment = data?.get("comment") as? String ?: "",
                           date = date,
                           user = data?.get("email") as? String ?: "",
                           documentId = snapshot.id,
                           postId = postId,
                           isLiked = data?.get("isLiked") as? Boolean ?: false,
                       )
                   }
                    _comments.value=commentList
                }
            })
    }

    fun onCommentLikeClicked(comment: Comments) {
        _comments.update { currentComments ->
            currentComments.map { c ->
                if (c.documentId == comment.documentId) {
                    c.copy(isLiked = !c.isLiked)
                } else {
                    c
                }
            }
        }
        firestore.collection("posts")
            .document(comment.postId)
            .collection("comments")
            .document(comment.documentId)
            .update("isLiked", !comment.isLiked)
            .addOnSuccessListener {
                Log.d("Firestore", "Post like status updated successfully.")
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error updating post like status.", e)
            }
    }

}