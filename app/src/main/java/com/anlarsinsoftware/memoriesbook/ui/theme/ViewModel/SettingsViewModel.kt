package com.anlarsinsoftware.memoriesbook.ui.theme.ViewModel


import androidx.lifecycle.ViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class SettingsViewModel : ViewModel() {
    private val auth = Firebase.auth

    fun signOut() {
        // Firebase Authentication oturumunu sonlandırır.
        auth.signOut()

        // Gerekirse Realtime Database'deki online durumunu da güncelleyebilirsin
        // Örn: homeViewModel.setUserOffline() gibi bir mantık burada da çağrılabilir.
    }
}