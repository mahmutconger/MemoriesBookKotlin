package com.anlarsinsoftware.memoriesbook.ui.theme.Firebase

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.firebase.messaging.remoteMessage

class MessagingService: FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "token : $token")
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d("FCM", "message : ${message.from}")
        message.notification?.let {
            Log.d("FCM", "Bildirim Başlığı: ${it.title}")
            Log.d("FCM", "Bildirim İçeriği: ${it.body}")
            // Burada gelen bildirimi kullanarak özel bir bildirim gösterebilirsin.
        }
    }
}