package com.anlarsinsoftware.memoriesbook.ui.theme.DataStore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class ThemeDataStore(private val context: Context) {
    private val isDarkModeKey = booleanPreferencesKey("is_dark_mode")
    val getTheme: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[isDarkModeKey] ?: false
        }
    suspend fun setTheme(isDarkMode: Boolean) {
        context.dataStore.edit { settings ->
            settings[isDarkModeKey] = isDarkMode
        }
    }
}