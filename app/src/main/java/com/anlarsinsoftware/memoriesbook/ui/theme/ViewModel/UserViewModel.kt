package com.anlarsinsoftware.memoriesbook.ui.theme.ViewModel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anlarsinsoftware.memoriesbook.ui.theme.Model.Followers
import com.anlarsinsoftware.memoriesbook.ui.theme.Model.Following
import com.anlarsinsoftware.memoriesbook.ui.theme.Model.FriendProfile
import com.anlarsinsoftware.memoriesbook.ui.theme.Model.User
import com.anlarsinsoftware.memoriesbook.ui.theme.Repository.UserRepository
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.storage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID


data class UserViewModelState(
    val isLoading: Boolean = true,
    val currentUser: User? = null,
    val myFriends: List<User> = emptyList(),
    val myFollowers: List<User> = emptyList(),
    val myFollowing: List<User> = emptyList(),
    val updateProfileState: UpdateProfileUiState = UpdateProfileUiState.Idle
)


class UserViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(UserViewModelState())
    val uiState: StateFlow<UserViewModelState> = _uiState.asStateFlow()




    private val auth = Firebase.auth
    private val firestore = Firebase.firestore
    private val storage = Firebase.storage

    private val userRepository = UserRepository()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _myFriends = MutableStateFlow<List<User>>(emptyList())
    val myFriends: StateFlow<List<User>> = _myFriends.asStateFlow()

    private val _myFollowers = MutableStateFlow<List<User>>(emptyList())
    val myFollowers: StateFlow<List<User>> = _myFollowers.asStateFlow()

    private val _myFollowing = MutableStateFlow<List<User>>(emptyList())
    val myFollowing: StateFlow<List<User>> = _myFollowing.asStateFlow()

    init {
     loadInitialData()
    }


    private fun loadInitialData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val currentUserId = auth.currentUser?.uid
            if (currentUserId == null) {
                _uiState.value = UserViewModelState(isLoading = false)
                return@launch
            }

            try {
                val user = userRepository.getUserProfile(currentUserId)
                val friends = userRepository.getFriends(currentUserId)
                val followers = userRepository.getFollowers(currentUserId)
                val following = userRepository.getFollowing(currentUserId)

                // State'i tek seferde güncelle.
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        currentUser = user,
                        myFriends = friends,
                        myFollowers = followers,
                        myFollowing = following
                    )
                }
            } catch (e: Exception) {
                // Herhangi bir hata olursa state'i temizle.
                _uiState.value = UserViewModelState(isLoading = false)
            }
        }
    }


    fun updateUserProfile(newDisplayName: String, newImageUri: Uri?) {
        viewModelScope.launch {
            _uiState.update { it.copy(updateProfileState = UpdateProfileUiState.Loading) }
            val user = auth.currentUser ?: run {
                _uiState.update { it.copy(updateProfileState = UpdateProfileUiState.Error("Kullanıcı bulunamadı")) }
                return@launch
            }

            try {
                // 1. Adım: Eğer yeni bir resim seçildiyse, onu Firebase Storage'a yükle
                val photoUrl = if (newImageUri != null) {
                    val imageName = "profile_pictures/${user.uid}/${UUID.randomUUID()}.jpg"
                    storage.reference.child(imageName)
                        .putFile(newImageUri).await()
                        .storage.downloadUrl.await()
                        .toString()
                } else {
                    user.photoUrl?.toString() // Resim değişmediyse mevcut URL'yi kullan
                }

                val profileUpdates = userProfileChangeRequest {
                    displayName = newDisplayName
                    photoUri = Uri.parse(photoUrl)
                }
                user.updateProfile(profileUpdates).await()

                val userDocRef = firestore.collection("Users").document(user.uid)
                val updates = mapOf(
                    "username" to newDisplayName,
                    "photoUrl" to photoUrl
                )
                userDocRef.update(updates).await()

                _uiState.update { it.copy(updateProfileState = UpdateProfileUiState.Success) }

            } catch (e: Exception) {
                _uiState.update { it.copy(updateProfileState = UpdateProfileUiState.Error(e.localizedMessage ?: "Profil güncellenirken bir hata oluştu.")) }

            }
        }
    }

    fun resetUpdateState() {
        _uiState.update { it.copy(updateProfileState = UpdateProfileUiState.Idle) }
    }

    fun signOut() {
        auth.signOut()
        _uiState.update { UserViewModelState(isLoading = false, currentUser = null) }
    }
}