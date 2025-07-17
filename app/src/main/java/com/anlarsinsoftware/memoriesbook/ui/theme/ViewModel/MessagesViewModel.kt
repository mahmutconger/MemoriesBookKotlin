import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anlarsinsoftware.memoriesbook.ui.theme.Model.FriendProfile
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await




class MessagesViewModel : ViewModel() {
    private val firestore = Firebase.firestore
    private val auth = Firebase.auth

    private val _friends = MutableStateFlow<List<FriendProfile>>(emptyList())
    val friends: StateFlow<List<FriendProfile>> = _friends.asStateFlow()

    init {
        fetchFriends()
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
}