package com.cs364.foodrush

import com.cs364.foodrush.data.settings.AppLanguage
import com.cs364.foodrush.data.settings.AppThemeMode
import com.cs364.foodrush.data.settings.UserSettings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Role 5 QA Unit Tests verifying Theme Mode, Language Tag mapping, and Default UserSettings.
 */
class SettingsAndLocalizationTest {

    @Test
    fun userSettings_hasExpectedDefaultValues() {
        val defaults = UserSettings()
        assertEquals(AppThemeMode.SYSTEM, defaults.themeMode)
        assertEquals(AppLanguage.SYSTEM, defaults.language)
        assertTrue(defaults.isSoundEnabled)
        assertTrue(defaults.isHapticEnabled)
    }

    @Test
    fun appThemeMode_parsesStorageKeysAndFallsBackToSystem() {
        assertEquals(AppThemeMode.LIGHT, AppThemeMode.fromStorageKey("light"))
        assertEquals(AppThemeMode.DARK, AppThemeMode.fromStorageKey("dark"))
        assertEquals(AppThemeMode.SYSTEM, AppThemeMode.fromStorageKey("system"))
        assertEquals(AppThemeMode.SYSTEM, AppThemeMode.fromStorageKey("unknown_value"))
        assertEquals(AppThemeMode.SYSTEM, AppThemeMode.fromStorageKey(null))
    }

    @Test
    fun appLanguage_parsesStorageKeysAndLocaleTags() {
        assertEquals(AppLanguage.TH, AppLanguage.fromStorageKey("th"))
        assertEquals(AppLanguage.EN, AppLanguage.fromStorageKey("en"))
        assertEquals(AppLanguage.SYSTEM, AppLanguage.fromStorageKey("system"))
        assertEquals(AppLanguage.SYSTEM, AppLanguage.fromStorageKey(null))

        assertEquals(AppLanguage.TH, AppLanguage.fromLanguageTag("th-TH"))
        assertEquals(AppLanguage.EN, AppLanguage.fromLanguageTag("en-US"))
        assertEquals(AppLanguage.SYSTEM, AppLanguage.fromLanguageTag(""))
    }
}