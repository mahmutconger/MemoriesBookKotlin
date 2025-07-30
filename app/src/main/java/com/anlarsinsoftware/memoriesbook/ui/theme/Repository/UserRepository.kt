package com.anlarsinsoftware.memoriesbook.ui.theme.Repository

import com.anlarsinsoftware.memoriesbook.ui.theme.Model.Followers
import com.anlarsinsoftware.memoriesbook.ui.theme.Model.Following
import com.anlarsinsoftware.memoriesbook.ui.theme.Model.FriendProfile
import com.anlarsinsoftware.memoriesbook.ui.theme.Model.Posts
import com.anlarsinsoftware.memoriesbook.ui.theme.Model.User
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

    suspend fun getUserProfile(userId: String): User? {
        if (userId.isBlank()) return null
        return try {
            firestore.collection("Users").document(userId).get().await()
                .toObject(User::class.java)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getFollowers(userId: String): List<User> {
        if (userId.isBlank()) return emptyList()
        return try {
            val userDoc = firestore.collection("Users").document(userId).get().await()
            val followerUids = userDoc.get("followers") as? List<String> ?: emptyList()
            if (followerUids.isNotEmpty()) {
                firestore.collection("Users")
                    .whereIn(FieldPath.documentId(), followerUids).get().await()
                    .toObjects(User::class.java)
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getFollowing(userId: String): List<User> {
        if (userId.isBlank()) return emptyList()
        return try {
            val userDoc = firestore.collection("Users").document(userId).get().await()
            val followingUids = userDoc.get("following") as? List<String> ?: emptyList()
            if (followingUids.isNotEmpty()) {
                firestore.collection("Users")
                    .whereIn(FieldPath.documentId(), followingUids).get().await()
                    .toObjects(User::class.java)
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getFriends(userId: String): List<User> {
        if (userId.isBlank()) return emptyList()
        return try {
            val userDoc = firestore.collection("Users").document(userId).get().await()
            val friendUids = userDoc.get("friends") as? List<String> ?: emptyList()
            if (friendUids.isNotEmpty()) {
                firestore.collection("Users")
                    .whereIn(FieldPath.documentId(), friendUids).get().await()
                    .toObjects(User::class.java)
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getUserPosts(userId: String, lastVisible: DocumentSnapshot?): PostPage {
        if (userId.isBlank()) return PostPage(emptyList(), null)

        var query: Query = firestore.collection("posts")
            .whereEqualTo("authorId", userId)
            .orderBy("date", Query.Direction.DESCENDING)
            .limit(9L)

        // Eğer başkasının profiline bakılıyorsa, sadece 'public' gönderileri göster
        if (userId != auth.currentUser?.uid) {
            query = query.whereEqualTo("visibility", "public")
        }

        if (lastVisible != null) {
            query = query.startAfter(lastVisible)
        }

        return try {
            val snapshot = query.get().await()
            val newPosts = snapshot.documents.mapNotNull { it.toObject(Posts::class.java)?.copy(documentId = it.id) }
            PostPage(newPosts, snapshot.documents.lastOrNull())
        } catch (e: Exception) {
            PostPage(emptyList(), null)
        }
    }



    // ...Diğer tüm veri fonksiyonları (updateUserProfile vb.) buraya taşınır.
}