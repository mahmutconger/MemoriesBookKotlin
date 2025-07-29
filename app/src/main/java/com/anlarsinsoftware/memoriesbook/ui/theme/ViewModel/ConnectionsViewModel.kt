package com.anlarsinsoftware.memoriesbook.ui.theme.ViewModel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anlarsinsoftware.memoriesbook.ui.theme.Model.Followers
import com.anlarsinsoftware.memoriesbook.ui.theme.Model.Following
import com.anlarsinsoftware.memoriesbook.ui.theme.Model.FriendProfile
import com.anlarsinsoftware.memoriesbook.ui.theme.Model.FriendRequest
import com.anlarsinsoftware.memoriesbook.ui.theme.Model.PendingRequest
import com.anlarsinsoftware.memoriesbook.ui.theme.Model.SearchResultUser
import com.anlarsinsoftware.memoriesbook.ui.theme.Util.showToast
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await




@OptIn(FlowPreview::class)
class ConnectionsViewModel : ViewModel() {
    private val firestore = Firebase.firestore
    private val auth = Firebase.auth

    // Arama kutusuna yazılan metni tutan state
    val searchQuery = MutableStateFlow("")

    private val _searchResults = MutableStateFlow<List<SearchResultUser>>(emptyList())
    val searchResults: StateFlow<List<SearchResultUser>> = _searchResults.asStateFlow()


    private val _pendingRequests = MutableStateFlow<List<PendingRequest>>(emptyList())
    val pendingRequests: StateFlow<List<PendingRequest>> = _pendingRequests.asStateFlow()


    init {
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
        fetchFriendRequests()
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



    fun fetchFriendRequests() {
        val currentUser = auth.currentUser ?: return

        viewModelScope.launch {
            try {
                // 1. ADIM: Sana gelen ve durumu "pending" olan istekleri çek
                val requestsQuery = firestore.collection("friend_requests")
                    .whereEqualTo("receiverId", currentUser.uid)
                    .whereEqualTo("status", "pending")
                    .get().await()

                val requests = requestsQuery.documents.mapNotNull { doc ->
                    doc.toObject(FriendRequest::class.java)?.copy(documentId = doc.id) // documentId'yi de alalım
                }

                if (requests.isEmpty()) {
                    _pendingRequests.value = emptyList()
                    return@launch
                }

                // 2. ADIM: İstek gönderenlerin ID'lerini bir liste yap
                val senderIds = requests.map { it.senderId }.distinct()

                // 3. ADIM: Bu ID'lerle kullanıcıların profil bilgilerini çek
                val sendersQuery = firestore.collection("Users")
                    .whereIn(FieldPath.documentId(), senderIds)
                    .get().await()

                val senderProfiles = sendersQuery.toObjects(SearchResultUser::class.java)

                // 4. ADIM: İki bilgiyi birleştirerek UI için son listeyi oluştur
                val pendingRequestList = requests.mapNotNull { request ->
                    val senderInfo = senderProfiles.find { it.uid == request.senderId }
                    if (senderInfo != null) {
                        PendingRequest(requestId = request.documentId, senderProfile = senderInfo)
                    } else {
                        null
                    }
                }

                _pendingRequests.value = pendingRequestList

            } catch (e: Exception) {
                Log.e("FriendRequestFetch", "Error fetching friend requests", e)
            }
        }
    }


    fun sendFriendRequest(receiverId: String, context: Context) {
        val senderId = auth.currentUser?.uid ?: return

        val request = hashMapOf(
            "senderId" to senderId,
            "receiverId" to receiverId,
            "status" to "pending"
        )

        firestore.collection("friend_requests").add(request)
            .addOnSuccessListener {
                showToast(context, "İstek başarıyla gönderildi")
            }
            .addOnFailureListener {
                showToast(context, "İstek gönderilemedi")
            }
    }

    fun acceptFriendRequest(request: PendingRequest) {
        val currentUser = auth.currentUser ?: return

        val batch = firestore.batch()

        val requestRef = firestore.collection("friend_requests").document(request.requestId)
        batch.update(requestRef, "status", "accepted")

        val myRef = firestore.collection("Users").document(currentUser.uid)
        batch.update(myRef, "friends", FieldValue.arrayUnion(request.senderProfile.uid))

        val senderRef = firestore.collection("Users").document(request.senderProfile.uid)
        batch.update(senderRef, "friends", FieldValue.arrayUnion(currentUser.uid))

        batch.commit().addOnSuccessListener {
            fetchFriendRequests()
           // fetchFollowers()
        }
    }

    fun declineFriendRequest(requestId: String) {
        firestore.collection("friend_requests").document(requestId).delete()
            .addOnSuccessListener {
                fetchFriendRequests()
            }
    }
}
