package com.cs364.foodrush

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cs364.foodrush.data.settings.SettingsPreferences
import com.cs364.foodrush.data.settings.UserSettings
import com.cs364.foodrush.ui.settings.SettingsScreen
import com.cs364.foodrush.ui.theme.FoodRushTheme

/**
 * MainActivity connected with Role 5 FoodRushTheme, SettingsPreferences (DataStore),
 * and AppCompatActivity per-app language support.
 * Role 3 will replace the placeholder home switcher with AppNavGraph.
 */
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val settingsPreferences = remember { SettingsPreferences(applicationContext) }
            val userSettings by settingsPreferences.settingsFlow.collectAsStateWithLifecycle(
                initialValue = UserSettings()
            )
            var showSettings by rememberSaveable { mutableStateOf(false) }

            FoodRushTheme(themeMode = userSettings.themeMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Role 3 will plug AppNavGraph(settingsPreferences = settingsPreferences) here
                    if (showSettings) {
                        SettingsScreen(
                            settingsPreferences = settingsPreferences,
                            onNavigateBack = { showSettings = false }
                        )
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = stringResource(R.string.app_name),
                                style = MaterialTheme.typography.headlineMedium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = stringResource(R.string.subtitle_tagline),
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Button(onClick = { showSettings = true }) {
                                Text(text = stringResource(R.string.menu_settings))
                            }
                        }
                    }
                }
            }
        }
    }
}