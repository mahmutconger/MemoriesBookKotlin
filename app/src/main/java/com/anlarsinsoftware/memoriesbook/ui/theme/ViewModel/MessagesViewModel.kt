import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class FriendProfile(
    val uid: String = "",
    val username: String = "",
    val photoUrl: String = ""
)

class MessagesViewModel : ViewModel() {
    private val firestore = Firebase.firestore
    private val auth = Firebase.auth

    private val _friends = MutableStateFlow<List<FriendProfile>>(emptyList())
    val friends: StateFlow<List<FriendProfile>> = _friends.asStateFlow()

    init {
        fetchFriendsWithIntersection()
    }

    private fun fetchFriendsWithIntersection() {
        viewModelScope.launch {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                Log.e("FriendFetch", "HATA: Mevcut kullanıcı null, işlem durduruldu.")
                return@launch
            }
            Log.d("FriendFetch", "1. Adım: Mevcut kullanıcı bulundu. UID: ${currentUser.uid}")

            try {
                val userDoc = firestore.collection("Users").document(currentUser.uid).get().await()

                if (!userDoc.exists()) {
                    Log.e("FriendFetch", "HATA: Kullanıcının dökümanı 'Users' koleksiyonunda bulunamadı!")
                    return@launch
                }
                Log.d("FriendFetch", "2. Adım: Kullanıcı dökümanı başarıyla çekildi.")

                val followers = userDoc.get("followers") as? List<String> ?: emptyList()
                val following = userDoc.get("following") as? List<String> ?: emptyList()
                Log.d("FriendFetch", "3. Adım: Takipçi sayısı: ${followers.size}, Takip edilen sayısı: ${following.size}")

                val friendUids = followers.intersect(following.toSet()).toList()
                if (friendUids.isEmpty()) {
                    Log.w("FriendFetch", "UYARI: Takipçi ve takip edilen listelerinin kesişimi boş. Hiç ortak arkadaş bulunamadı.")
                    _friends.value = emptyList()
                    return@launch
                }
                Log.d("FriendFetch", "4. Adım: Kesişim bulundu. Arkadaş UID'leri: $friendUids")

                val friendsQuery = firestore.collection("Users")
                    .whereIn(FieldPath.documentId(), friendUids)
                    .get().await()

                Log.d("FriendFetch", "5. Adım: Arkadaş profilleri sorgulandı. Bulunan döküman sayısı: ${friendsQuery.size()}")

                val friendObjects = friendsQuery.toObjects(FriendProfile::class.java)
                _friends.value = friendObjects
                Log.d("FriendFetch", "6. Adım: State güncellendi. Arayüze ${friendObjects.size} arkadaş gönderildi.")

            } catch (e: Exception) {
                Log.e("FriendFetch", "!!! HATA: İşlem sırasında bir exception oluştu: ${e.message}", e)
            }
        }
    }
}