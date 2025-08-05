package com.anlarsinsoftware.memoriesbook.ui.theme.ViewModel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anlarsinsoftware.memoriesbook.ui.theme.Model.Posts
import com.anlarsinsoftware.memoriesbook.ui.theme.Model.Users
import com.anlarsinsoftware.memoriesbook.ui.theme.Repository.UserRepository
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.storage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

data class ProfileState(
    val isLoading: Boolean = true,
    val profileUser: Users? = null,
    val isCurrentUserProfile: Boolean = false,
    val publicPosts: List<Posts> = emptyList(),
    val privatePosts: List<Posts> = emptyList(),
    val followers: List<Users> = emptyList(),
    val following: List<Users> = emptyList(),
    val friends: List<Users> = emptyList(),
    val isLoadingMorePublicPosts: Boolean = false,
    val publicPostsEnded: Boolean = false,
    val isLoadingMorePrivatePosts: Boolean = false,
    val privatePostsEnded: Boolean = false,
    val updateProfileState: UpdateProfileUiState = UpdateProfileUiState.Idle
)

sealed interface UpdateProfileUiState {
    object Idle : UpdateProfileUiState
    object Loading : UpdateProfileUiState
    object Success : UpdateProfileUiState
    data class Error(val message: String) : UpdateProfileUiState
}


private  var auth = Firebase.auth

class ProfileViewModel(private val uid: String) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileState())
    val uiState: StateFlow<ProfileState> = _uiState.asStateFlow()

    private val userRepository = UserRepository()

    private var lastVisiblePublicPost: DocumentSnapshot? = null
    private var lastVisiblePrivatePost: DocumentSnapshot? = null

    private val firestore = Firebase.firestore
    private val storage = Firebase.storage

    init {
        loadInitialProfileData()
    }

    private fun loadInitialProfileData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val currentUserId = auth.currentUser?.uid

            val uidToFetch = if (uid == "me") currentUserId else uid

            if (uidToFetch.isNullOrBlank()) {
                _uiState.update { it.copy(isLoading = false) }
                return@launch
            }


            val user = userRepository.getUserProfile(uidToFetch)
            val isMyProfile = (currentUserId != null && currentUserId == uidToFetch)
            val publicPage = userRepository.getPublicPosts(uidToFetch, null)
            val privatePage = userRepository.getPrivatePostsForViewer(uidToFetch, currentUserId, null)
            val followersList = userRepository.getFollowers(uidToFetch)
            val followingList = userRepository.getFollowing(uidToFetch)
            val friendsList = userRepository.getFriends(uidToFetch)

            lastVisiblePublicPost = publicPage.lastVisible
            lastVisiblePrivatePost = privatePage.lastVisible

            _uiState.update {
                it.copy(
                    isLoading = false,
                    profileUser = user,
                    isCurrentUserProfile = isMyProfile,
                    followers = followersList,
                    following = followingList,
                    friends = friendsList,
                    publicPosts = publicPage.posts,
                    privatePosts = privatePage.posts,
                    publicPostsEnded = publicPage.lastVisible == null,
                    privatePostsEnded = privatePage.lastVisible == null
                )
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


    fun loadMorePublicPosts() {
        // Zaten yükleniyorsa veya liste bittiyse tekrar yükleme yapma
        if (_uiState.value.isLoadingMorePublicPosts || _uiState.value.publicPostsEnded) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingMorePublicPosts = true) }
            val uidToFetch = if (uid == "me") Firebase.auth.currentUser?.uid else uid

            if (uidToFetch.isNullOrBlank()) {
                _uiState.update { it.copy(isLoadingMorePublicPosts = false) }
                return@launch
            }

            val nextPage = userRepository.getPublicPosts(uidToFetch, lastVisiblePublicPost)
            lastVisiblePublicPost = nextPage.lastVisible

            _uiState.update { currentState ->
                currentState.copy(
                    isLoadingMorePublicPosts = false,
                    publicPosts = currentState.publicPosts + nextPage.posts,
                    publicPostsEnded = nextPage.posts.isEmpty() || nextPage.lastVisible == null
                )
            }
        }
    }

    fun loadMorePrivatePosts() {
        // Zaten yükleniyorsa veya liste bittiyse tekrar yükleme yapma
        if (_uiState.value.isLoadingMorePrivatePosts || _uiState.value.privatePostsEnded) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingMorePrivatePosts = true) }
            val uidToFetch = if (uid == "me") Firebase.auth.currentUser?.uid else uid

            if (uidToFetch.isNullOrBlank()) {
                _uiState.update { it.copy(isLoadingMorePrivatePosts = false) }
                return@launch
            }

            val ownerId = _uiState.value.profileUser?.uid ?: return@launch
            val viewerId = auth.currentUser?.uid ?: return@launch

            val nextPage = userRepository.getPrivatePostsForViewer(ownerId, viewerId, lastVisiblePrivatePost)
            lastVisiblePrivatePost = nextPage.lastVisible

            _uiState.update { currentState ->
                currentState.copy(
                    isLoadingMorePrivatePosts = false,
                    privatePosts = currentState.privatePosts + nextPage.posts, // Yeni gönderileri listeye ekle
                    privatePostsEnded = nextPage.posts.isEmpty() || nextPage.lastVisible == null
                )
            }
        }
    }


}

class ProfileViewModelFactory(private val uid: String) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProfileViewModel(uid) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}