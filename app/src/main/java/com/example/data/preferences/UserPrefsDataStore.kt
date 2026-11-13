package com.example.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "nutripulse_prefs")

class UserPrefsDataStore(private val context: Context) {
    private val keyLang = stringPreferencesKey("preferred_language")
    private val keyTheme = stringPreferencesKey("theme_mode")

    val languageFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[keyLang] ?: "ar"
    }

    val themeModeFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[keyTheme] ?: "system"
    }

    suspend fun setLanguage(lang: String) {
        context.dataStore.edit { prefs ->
            prefs[keyLang] = if (lang == "en") "en" else "ar"
        }
    }

    suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { prefs ->
            prefs[keyTheme] = when (mode) {
                "light", "dark", "system" -> mode
                else -> "system"
            }
        }
    }
}
