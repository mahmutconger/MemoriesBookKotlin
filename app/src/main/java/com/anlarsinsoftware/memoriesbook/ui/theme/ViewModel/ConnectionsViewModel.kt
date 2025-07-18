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
import com.anlarsinsoftware.memoriesbook.ui.theme.Model.Posts
import com.anlarsinsoftware.memoriesbook.ui.theme.Model.SearchResultUser
import com.anlarsinsoftware.memoriesbook.ui.theme.Tools.showToast
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FieldValue
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




@OptIn(FlowPreview::class)
class ConnectionsViewModel : ViewModel() {
    private val firestore = Firebase.firestore
    private val auth = Firebase.auth

    // Arama kutusuna yazılan metni tutan state
    val searchQuery = MutableStateFlow("")

    private val _searchResults = MutableStateFlow<List<SearchResultUser>>(emptyList())
    val searchResults: StateFlow<List<SearchResultUser>> = _searchResults.asStateFlow()

    private val _followers = MutableStateFlow<List<Followers>>(emptyList())
    val followers: StateFlow<List<Followers>> = _followers.asStateFlow()

    private val _following = MutableStateFlow<List<Following>>(emptyList())
    val following: StateFlow<List<Following>> = _following.asStateFlow()

    private val _friends = MutableStateFlow<List<com.anlarsinsoftware.memoriesbook.ui.theme.Model.FriendProfile>>(emptyList())
    val friends: StateFlow<List<FriendProfile>> = _friends.asStateFlow()

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

        fetchFollowers()
        fetchFollowing()
        fetchFriends()
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

    fun fetchFollowers() {
        val currentUser = auth.currentUser ?: return

        viewModelScope.launch {
            try {
                // 1. Adım: Mevcut kullanıcının dökümanından 'followers' dizisini (UID listesini) al.
                val userDoc = firestore.collection("Users").document(currentUser.uid).get().await()
                val followerUids = userDoc.get("followers") as? List<String> ?: emptyList()

                if (followerUids.isEmpty()) {
                    _followers.value = emptyList()
                    return@launch
                }

                // 2. Adım: Bu UID listesini kullanarak 'Users' koleksiyonundan takipçilerin profillerini çek.
                val followersQuery = firestore.collection("Users")
                    .whereIn(FieldPath.documentId(), followerUids)
                    .get().await()

                // `Followers` data class'ına dönüştür
                _followers.value = followersQuery.toObjects(Followers::class.java)

            } catch (e: Exception) {
                // Hata yönetimi
                Log.e("ConnectionsLOG", "Error fetching followers", e)
            }
        }
    }

    fun fetchFollowing() {
        val currentUser = auth.currentUser ?: return

        viewModelScope.launch {
            try {
                // 1. Adım: Mevcut kullanıcının dökümanından 'followers' dizisini (UID listesini) al.
                val userDoc = firestore.collection("Users").document(currentUser.uid).get().await()
                val followingUids = userDoc.get("following") as? List<String> ?: emptyList()

                if (followingUids.isEmpty()) {
                    _following.value = emptyList() // Takipçi yoksa listeyi boşalt
                    return@launch
                }

                // 2. Adım: Bu UID listesini kullanarak 'Users' koleksiyonundan takipçilerin profillerini çek.
                val followersQuery = firestore.collection("Users")
                    .whereIn(FieldPath.documentId(), followingUids)
                    .get().await()

                // `Followers` data class'ına dönüştür
                _following.value = followersQuery.toObjects(Following::class.java)

            } catch (e: Exception) {
                // Hata yönetimi
                Log.e("ConnectionsLOG", "Error fetching followers", e)
            }
        }
    }

    private fun fetchFriends() {
        viewModelScope.launch {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                Log.e("FriendFetch", "HATA: Mevcut kullanıcı null, işlem durduruldu.")
                _friends.value = emptyList()
                return@launch
            }
            Log.d("FriendFetch", "1. Adım: Mevcut kullanıcı bulundu. UID: ${currentUser.uid}")

            try {
                // 2. Adım: Mevcut kullanıcının dökümanını oku.
                val userDoc = firestore.collection("Users").document(currentUser.uid).get().await()

                if (!userDoc.exists()) {
                    Log.e("FriendFetch", "HATA: Kullanıcının dökümanı 'Users' koleksiyonunda bulunamadı!")
                    return@launch
                }

                // 3. Adım: Doğrudan 'friends' dizisini al.
                val friendUids = userDoc.get("friends") as? List<String> ?: emptyList()

                if (friendUids.isEmpty()) {
                    Log.i("FriendFetch", "Kullanıcının hiç arkadaşı yok.")
                    _friends.value = emptyList()
                    return@launch
                }
                Log.d("FriendFetch", "4. Adım: Arkadaş listesi bulundu. Arkadaş UID'leri: $friendUids")

                // 5. Adım: Bu UID'lerle kullanıcı profillerini tek bir sorguda çek.
                val friendsQuery = firestore.collection("Users")
                    .whereIn(FieldPath.documentId(), friendUids)
                    .get().await()

                val friendObjects = friendsQuery.toObjects(FriendProfile::class.java)
                _friends.value = friendObjects
                Log.d("FriendFetch", "6. Adım: State güncellendi. Arayüze ${friendObjects.size} arkadaş gönderildi.")

            } catch (e: Exception) {
                Log.e("FriendFetch", "!!! HATA: İşlem sırasında bir exception oluştu: ${e.message}", e)
                _friends.value = emptyList()
            }
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
            fetchFollowers()
        }
    }

    fun declineFriendRequest(requestId: String) {
        firestore.collection("friend_requests").document(requestId).delete()
            .addOnSuccessListener {
                fetchFriendRequests()
            }
    }
}
