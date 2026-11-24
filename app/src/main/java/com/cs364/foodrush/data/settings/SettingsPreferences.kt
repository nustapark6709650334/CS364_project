package com.cs364.foodrush.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "food_rush_settings"
)

enum class AppThemeMode(val storageKey: String) {
    SYSTEM("system"),
    LIGHT("light"),
    DARK("dark");

    companion object {
        fun fromStorageKey(key: String?): AppThemeMode {
            return entries.firstOrNull { it.storageKey == key } ?: SYSTEM
        }
    }
}

enum class AppLanguage(val storageKey: String, val languageTag: String) {
    SYSTEM("system", ""),
    TH("th", "th"),
    EN("en", "en");

    companion object {
        fun fromStorageKey(key: String?): AppLanguage {
            return entries.firstOrNull { it.storageKey == key } ?: SYSTEM
        }

        fun fromLanguageTag(tag: String?): AppLanguage {
            if (tag.isNullOrBlank()) return SYSTEM
            return when {
                tag.startsWith("th", ignoreCase = true) -> TH
                tag.startsWith("en", ignoreCase = true) -> EN
                else -> SYSTEM
            }
        }
    }
}

data class UserSettings(
    val themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    val language: AppLanguage = AppLanguage.SYSTEM,
    val isSoundEnabled: Boolean = true,
    val isHapticEnabled: Boolean = true
)

/**
 * Role 5 Persistent Settings Repository powered by Jetpack DataStore.
 * Role 2 can observe [settingsFlow] for Sound (SFX) & Haptic flags during gameplay.
 */
class SettingsPreferences(context: Context) {

    private val appContext = context.applicationContext

    private object Keys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val LANGUAGE = stringPreferencesKey("app_language")
        val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        val HAPTIC_ENABLED = booleanPreferencesKey("haptic_enabled")
    }

    val settingsFlow: Flow<UserSettings> = appContext.settingsDataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { prefs ->
            UserSettings(
                themeMode = AppThemeMode.fromStorageKey(prefs[Keys.THEME_MODE]),
                language = AppLanguage.fromStorageKey(prefs[Keys.LANGUAGE]),
                isSoundEnabled = prefs[Keys.SOUND_ENABLED] ?: true,
                isHapticEnabled = prefs[Keys.HAPTIC_ENABLED] ?: true
            )
        }

    suspend fun setThemeMode(mode: AppThemeMode) {
        appContext.settingsDataStore.edit { prefs ->
            prefs[Keys.THEME_MODE] = mode.storageKey
        }
    }

    suspend fun setLanguage(language: AppLanguage) {
        appContext.settingsDataStore.edit { prefs ->
            prefs[Keys.LANGUAGE] = language.storageKey
        }
    }

    suspend fun setSoundEnabled(enabled: Boolean) {
        appContext.settingsDataStore.edit { prefs ->
            prefs[Keys.SOUND_ENABLED] = enabled
        }
    }

    suspend fun setHapticEnabled(enabled: Boolean) {
        appContext.settingsDataStore.edit { prefs ->
            prefs[Keys.HAPTIC_ENABLED] = enabled
        }
    }

    suspend fun resetToDefaults() {
        appContext.settingsDataStore.edit { prefs ->
            prefs[Keys.THEME_MODE] = AppThemeMode.SYSTEM.storageKey
            prefs[Keys.LANGUAGE] = AppLanguage.SYSTEM.storageKey
            prefs[Keys.SOUND_ENABLED] = true
            prefs[Keys.HAPTIC_ENABLED] = true
        }
    }
}