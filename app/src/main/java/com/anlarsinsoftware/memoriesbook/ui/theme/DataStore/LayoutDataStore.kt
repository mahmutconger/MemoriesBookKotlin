package com.anlarsinsoftware.memoriesbook.ui.theme.DataStore


import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "layout_settings")

class LayoutDataStore(private val context: Context) {
    // Görünüm modunu saklamak için bir anahtar ("fit" veya "crop")
    private val contentScaleKey = stringPreferencesKey("content_scale_mode")

    // Kaydedilmiş görünüm modunu Flow olarak oku
    val getContentScale: Flow<String> = context.dataStore.data
        .map { preferences ->
            // Değer yoksa varsayılan olarak "Crop" kullan
            preferences[contentScaleKey] ?: "crop"
        }

    // Yeni görünüm modunu DataStore'a kaydet
    suspend fun setContentScale(scaleMode: String) {
        context.dataStore.edit { settings ->
            settings[contentScaleKey] = scaleMode
        }
    }
}