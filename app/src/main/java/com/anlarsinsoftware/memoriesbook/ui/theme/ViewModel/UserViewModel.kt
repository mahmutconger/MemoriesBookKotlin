package com.anlarsinsoftware.memoriesbook.ui.theme.ViewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anlarsinsoftware.memoriesbook.ui.theme.Model.Users
import com.anlarsinsoftware.memoriesbook.ui.theme.Repository.UserRepository
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class UserViewModelState(
    val isLoading: Boolean = true,
    val currentUser: Users? = null,
    val myFriends: List<Users> = emptyList(),
    val myFollowers: List<Users> = emptyList(),
    val myFollowing: List<Users> = emptyList(),
)



private  var auth = Firebase.auth

class UserViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(UserViewModelState())
    val uiState: StateFlow<UserViewModelState> = _uiState.asStateFlow()



    private val userRepository = UserRepository()

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
                Log.e("UserViewModel_ERROR", "Veri yüklenirken hata oluştu!", e)
                _uiState.value = UserViewModelState(isLoading = false)
            }
        }
    }



    fun signOut() {
        auth.signOut()
        _uiState.update { UserViewModelState(isLoading = false, currentUser = null) }
    }
}