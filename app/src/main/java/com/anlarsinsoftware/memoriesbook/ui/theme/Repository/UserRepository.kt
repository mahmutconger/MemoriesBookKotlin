package com.anlarsinsoftware.memoriesbook.ui.theme.Repository
import android.util.Log
import com.anlarsinsoftware.memoriesbook.ui.theme.Model.Posts
import com.anlarsinsoftware.memoriesbook.ui.theme.Model.Users
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await

// Pagination için bir yardımcı data class
data class PostPage(
    val posts: List<Posts>,
    val lastVisible: DocumentSnapshot?
)

class UserRepository {
    private val firestore = Firebase.firestore
    private val auth = Firebase.auth

    suspend fun getUserProfile(uid: String): Users? {
        if (uid.isBlank()) return null
        return try {
            firestore.collection("Users").document(uid).get().await()
                .toObject(Users::class.java)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getFollowers(uid: String): List<Users> {
        if (uid.isBlank()) return emptyList()
        return try {
            val userDoc = firestore.collection("Users").document(uid).get().await()
            val followerUids = userDoc.get("followers") as? List<String> ?: emptyList()
            if (followerUids.isNotEmpty()) {
                firestore.collection("Users")
                    .whereIn(FieldPath.documentId(), followerUids).get().await()
                    .toObjects(Users::class.java)
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getFollowing(uid: String): List<Users> {
        if (uid.isBlank()) return emptyList()
        return try {
            val userDoc = firestore.collection("Users").document(uid).get().await()
            val followingUids = userDoc.get("following") as? List<String> ?: emptyList()
            if (followingUids.isNotEmpty()) {
                firestore.collection("Users")
                    .whereIn(FieldPath.documentId(), followingUids).get().await()
                    .toObjects(Users::class.java)
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getFriends(uid: String): List<Users> {
        if (uid.isBlank()) return emptyList()
        return try {
            val userDoc = firestore.collection("Users").document(uid).get().await()
            val friendUids = userDoc.get("friends") as? List<String> ?: emptyList()
            if (friendUids.isNotEmpty()) {
                firestore.collection("Users")
                    .whereIn(FieldPath.documentId(), friendUids).get().await()
                    .toObjects(Users::class.java)
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getPublicPosts(uid: String, lastVisible: DocumentSnapshot?): PostPage {
        if (uid.isBlank()) return PostPage(emptyList(), null)

        var query: Query = firestore.collection("posts")
            .whereEqualTo("authorId", uid)
            .whereEqualTo("visibility", "public")
            .orderBy("date", Query.Direction.DESCENDING)
            .limit(9L)

        if (lastVisible != null) {
            query = query.startAfter(lastVisible)
        }

        return try {
            val snapshot = query.get().await()
            val newPosts = snapshot.documents.mapNotNull { it.toObject(Posts::class.java)?.copy(documentId = it.id) }
            PostPage(newPosts, snapshot.documents.lastOrNull())
        } catch (e: Exception) {
            Log.e("UserRepository", "getPublicPosts başarısız!", e)
            PostPage(emptyList(), null)
        }
    }

    suspend fun getPrivatePostsForViewer(uid: String, viewerId: String?, lastVisible: DocumentSnapshot?): PostPage {
        if (uid.isBlank() || viewerId.isNullOrBlank()) return PostPage(emptyList(), null)

        var query: Query = firestore.collection("posts")
            .whereEqualTo("authorId", uid)
            .whereEqualTo("visibility", "private") // Sadece private olanlar

        if (uid != viewerId) {
            query = query.whereArrayContains("visibleTo", viewerId)
        }

        query = query.orderBy("date", Query.Direction.DESCENDING).limit(9L)

        if (lastVisible != null) {
            query = query.startAfter(lastVisible)
        }

        return try {
            val snapshot = query.get().await()
            val newPosts = snapshot.documents.mapNotNull { it.toObject(Posts::class.java)?.copy(documentId = it.id) }
            PostPage(newPosts, snapshot.documents.lastOrNull())
        } catch (e: Exception) {
            Log.e("UserRepository", "getPrivatePostsForViewer başarısız!", e)
            PostPage(emptyList(), null)
        }
    }
}