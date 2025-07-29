package com.anlarsinsoftware.memoriesbook.ui.theme.Util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun rememberFormattedTimestamp(timestamp: Timestamp?): String {
    return remember(timestamp) {
        if (timestamp == null) return@remember "Tarih bilgisi yok"

        val messageDate = timestamp.toDate()
        val messageCal = Calendar.getInstance().apply { time = messageDate }
        val currentCal = Calendar.getInstance()

        if (messageCal.get(Calendar.YEAR) < currentCal.get(Calendar.YEAR)) {
            SimpleDateFormat("dd MMMM yyyy", Locale("tr")).format(messageDate)
        }
        else {
            SimpleDateFormat("dd MMMM, HH:mm", Locale("tr")).format(messageDate)
        }
    }
}