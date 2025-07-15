package com.anlarsinsoftware.memoriesbook.ui.theme.ViewModel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anlarsinsoftware.memoriesbook.ui.theme.Model.Posts
import com.anlarsinsoftware.memoriesbook.ui.theme.Tools.showToast
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// Arayüzde gösterilecek kullanıcı modeli
data class SearchResultUser(
    val uid: String = "",
    val username: String = "",
    val email: String = ""
    // İsteğe bağlı olarak requestStatus: "sent", "friends", "none" gibi bir alan eklenebilir
)

data class Connections(
    val uid: String = "",
    val username: String = "",
    val email: String = ""
)

@OptIn(FlowPreview::class)
class ConnectionsViewModel : ViewModel() {
    private val firestore = Firebase.firestore
    private val auth = Firebase.auth

    // Arama kutusuna yazılan metni tutan state
    val searchQuery = MutableStateFlow("")

    private val _searchResults = MutableStateFlow<List<SearchResultUser>>(emptyList())
    val searchResults: StateFlow<List<SearchResultUser>> = _searchResults.asStateFlow()

    private val _connections=MutableStateFlow<List<Connections>>(emptyList())
    val connections: StateFlow<List<Connections>> = _connections.asStateFlow()

    init {
        // Arama sorgusu her değiştiğinde (ama kullanıcı yazmayı bıraktıktan 300ms sonra)
        // veritabanında arama yap. Bu, her tuş basışında sorgu yapmayı engeller.
        searchQuery
            .debounce(300L)
            .onEach { query ->
                if (query.isNotBlank()) {
                    searchUsers(query)
                } else {
                    _searchResults.value = emptyList()
                }
            }
            .launchIn(viewModelScope)

        fetchFollowers()
        fetchFollowing()
    }

    private fun searchUsers(query: String) {

        firestore.collection("Users")
            .whereGreaterThanOrEqualTo("username", query)
            .whereLessThanOrEqualTo("username", query + '\uf8ff')
            .limit(10)
            .get()
            .addOnSuccessListener { documents ->
                val userList = documents.toObjects(SearchResultUser::class.java)
                _searchResults.value = userList.filter { it.uid != auth.currentUser?.uid }
            }
            .addOnFailureListener {
                // Hata yönetimi
            }
    }
    fun fetchFollowers() {
        val currentUser = auth.currentUser ?: return

        viewModelScope.launch {
            try {
                // 1. Adım: Mevcut kullanıcının dökümanından 'followers' dizisini (UID listesini) al.
                val userDoc = firestore.collection("Users").document(currentUser.uid).get().await()
                val followerUids = userDoc.get("followers") as? List<String> ?: emptyList()

                if (followerUids.isEmpty()) {
                    _connections.value = emptyList() // Takipçi yoksa listeyi boşalt
                    return@launch
                }

                // 2. Adım: Bu UID listesini kullanarak 'Users' koleksiyonundan takipçilerin profillerini çek.
                val followersQuery = firestore.collection("Users")
                    .whereIn(FieldPath.documentId(), followerUids)
                    .get().await()

                // `Followers` data class'ına dönüştür
                _connections.value = followersQuery.toObjects(Connections::class.java)

            } catch (e: Exception) {
                // Hata yönetimi
                Log.e("FollowerFetch", "Error fetching followers", e)
            }
        }
    }

    fun fetchFollowing() {
        val currentUser = auth.currentUser ?: return

        viewModelScope.launch {
            try {
                // 1. Adım: Mevcut kullanıcının dökümanından 'followers' dizisini (UID listesini) al.
                val userDoc = firestore.collection("Users").document(currentUser.uid).get().await()
                val followerUids = userDoc.get("following") as? List<String> ?: emptyList()

                if (followerUids.isEmpty()) {
                    _connections.value = emptyList() // Takipçi yoksa listeyi boşalt
                    return@launch
                }

                // 2. Adım: Bu UID listesini kullanarak 'Users' koleksiyonundan takipçilerin profillerini çek.
                val followersQuery = firestore.collection("Users")
                    .whereIn(FieldPath.documentId(), followerUids)
                    .get().await()

                // `Followers` data class'ına dönüştür
                _connections.value = followersQuery.toObjects(Connections::class.java)

            } catch (e: Exception) {
                // Hata yönetimi
                Log.e("FollowerFetch", "Error fetching followers", e)
            }
        }
    }


    fun sendFriendRequest(receiverId: String,context : Context) {
        val senderId = auth.currentUser?.uid ?: return

        val request = hashMapOf(
            "senderId" to senderId,
            "receiverId" to receiverId,
            "status" to "pending"
        )

        firestore.collection("friend_requests").add(request)
            .addOnSuccessListener {
                showToast(context,"İstek başarıyla gönderildi")
            }
            .addOnFailureListener {
                // Hata yönetimi
            }
    }
}