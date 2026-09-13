package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class UserPreferencesRepository(private val context: Context) {
    
    private val THEME_KEY = stringPreferencesKey("theme_preference")

    val themePreference: Flow<ThemePreference> = context.dataStore.data
        .map { preferences ->
            val themeName = preferences[THEME_KEY] ?: ThemePreference.SYSTEM.name
            try {
                ThemePreference.valueOf(themeName)
            } catch (e: Exception) {
                ThemePreference.SYSTEM
            }
        }

    suspend fun setThemePreference(theme: ThemePreference) {
        context.dataStore.edit { preferences ->
            preferences[THEME_KEY] = theme.name
        }
    }
}
