package com.anlarsinsoftware.memoriesbook.ui.theme.ViewModel


import androidx.lifecycle.ViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

class SettingsViewModel : ViewModel() {
    private val auth = Firebase.auth

    fun signOut() {
        // Firebase Authentication oturumunu sonlandırır.
        auth.signOut()

        // Gerekirse Realtime Database'deki online durumunu da güncelleyebilirsin
        // Örn: homeViewModel.setUserOffline() gibi bir mantık burada da çağrılabilir.
    }
}