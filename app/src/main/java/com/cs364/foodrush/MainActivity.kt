package com.cs364.foodrush

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cs364.foodrush.data.settings.SettingsPreferences
import com.cs364.foodrush.data.settings.UserSettings
import com.cs364.foodrush.game.HapticAudioManager
import com.cs364.foodrush.ui.navigation.AppNavGraph
import com.cs364.foodrush.ui.theme.FoodRushTheme

/**
 * MainActivity connected with Role 5 FoodRushTheme, SettingsPreferences (DataStore),
 * AppCompatActivity per-app language support, and Role 3 AppNavGraph + HapticAudioManager.
 */
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val settingsPreferences = remember { SettingsPreferences(applicationContext) }
            val hapticAudioManager = remember { HapticAudioManager(applicationContext) }
            val userSettings by settingsPreferences.settingsFlow.collectAsStateWithLifecycle(
                initialValue = UserSettings()
            )
            val lifecycleOwner = LocalLifecycleOwner.current

            DisposableEffect(lifecycleOwner, hapticAudioManager) {
                val observer = LifecycleEventObserver { _, event ->
                    if (event == Lifecycle.Event.ON_PAUSE || event == Lifecycle.Event.ON_STOP) {
                        hapticAudioManager.pauseAudio()
                    }
                }
                lifecycleOwner.lifecycle.addObserver(observer)
                onDispose {
                    lifecycleOwner.lifecycle.removeObserver(observer)
                    hapticAudioManager.release()
                }
            }

            FoodRushTheme(themeMode = userSettings.themeMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavGraph(
                        settingsPreferences = settingsPreferences,
                        hapticAudioManager = hapticAudioManager,
                        onExitApp = { finish() }
                    )
                }
            }
        }
    }
}