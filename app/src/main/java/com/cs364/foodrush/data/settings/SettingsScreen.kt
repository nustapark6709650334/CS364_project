package com.cs364.foodrush.ui.settings

import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cs364.foodrush.R
import com.cs364.foodrush.data.settings.AppLanguage
import com.cs364.foodrush.data.settings.AppThemeMode
import com.cs364.foodrush.data.settings.SettingsPreferences
import com.cs364.foodrush.data.settings.UserSettings
import kotlinx.coroutines.launch

/**
 * Role 5 Settings & How-to-Play Screen.
 * Ready to be connected directly into Role 3's AppNavGraph.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settingsPreferences: SettingsPreferences,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val settings by settingsPreferences.settingsFlow.collectAsStateWithLifecycle(
        initialValue = UserSettings()
    )
    val scope = rememberCoroutineScope()

    val currentAppLocales = AppCompatDelegate.getApplicationLocales()
    val effectiveLanguage = if (currentAppLocales.isEmpty) {
        settings.language
    } else {
        AppLanguage.fromLanguageTag(currentAppLocales.toLanguageTags())
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.settings_title),
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_navigate_back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Appearance (Day / Night Theme)
            SettingsSectionCard(title = stringResource(R.string.settings_section_appearance)) {
                Text(
                    text = stringResource(R.string.settings_theme_label),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))

                val themeOptions = listOf(
                    AppThemeMode.SYSTEM to stringResource(R.string.settings_theme_system),
                    AppThemeMode.LIGHT to stringResource(R.string.settings_theme_light),
                    AppThemeMode.DARK to stringResource(R.string.settings_theme_dark)
                )

                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    themeOptions.forEachIndexed { index, (mode, label) ->
                        SegmentedButton(
                            selected = settings.themeMode == mode,
                            onClick = {
                                scope.launch { settingsPreferences.setThemeMode(mode) }
                            },
                            shape = SegmentedButtonDefaults.itemShape(
                                index = index,
                                count = themeOptions.size
                            )
                        ) {
                            Text(text = label, maxLines = 1)
                        }
                    }
                }
            }

            // 2. Language (TH / EN Per-App Locale)
            SettingsSectionCard(title = stringResource(R.string.settings_section_language)) {
                Text(
                    text = stringResource(R.string.settings_language_label),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))

                val languageOptions = listOf(
                    AppLanguage.SYSTEM to stringResource(R.string.settings_language_system),
                    AppLanguage.TH to stringResource(R.string.settings_language_th),
                    AppLanguage.EN to stringResource(R.string.settings_language_en)
                )

                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    languageOptions.forEachIndexed { index, (lang, label) ->
                        SegmentedButton(
                            selected = effectiveLanguage == lang,
                            onClick = {
                                scope.launch {
                                    settingsPreferences.setLanguage(lang)
                                    val localeList = if (lang == AppLanguage.SYSTEM) {
                                        LocaleListCompat.getEmptyLocaleList()
                                    } else {
                                        LocaleListCompat.forLanguageTags(lang.languageTag)
                                    }
                                    AppCompatDelegate.setApplicationLocales(localeList)
                                }
                            },
                            shape = SegmentedButtonDefaults.itemShape(
                                index = index,
                                count = languageOptions.size
                            )
                        ) {
                            Text(text = label, maxLines = 1)
                        }
                    }
                }
            }

            // 3. Gameplay Feedback (Sound & Haptic)
            SettingsSectionCard(title = stringResource(R.string.settings_section_gameplay)) {
                ToggleSettingRow(
                    title = stringResource(R.string.settings_sound_title),
                    subtitle = stringResource(R.string.settings_sound_desc),
                    checked = settings.isSoundEnabled,
                    onCheckedChange = { enabled ->
                        scope.launch { settingsPreferences.setSoundEnabled(enabled) }
                    }
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                ToggleSettingRow(
                    title = stringResource(R.string.settings_haptic_title),
                    subtitle = stringResource(R.string.settings_haptic_desc),
                    checked = settings.isHapticEnabled,
                    onCheckedChange = { enabled ->
                        scope.launch { settingsPreferences.setHapticEnabled(enabled) }
                    }
                )
            }

            // 4. How to Play Guide
            SettingsSectionCard(title = stringResource(R.string.settings_section_how_to_play)) {
                Text(
                    text = stringResource(R.string.how_to_play_intro),
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(12.dp))

                HowToPlayTargetRow(
                    characterRes = R.drawable.char_skinny,
                    foodRes = R.drawable.food_burger,
                    title = stringResource(R.string.how_to_play_skinny_title),
                    description = stringResource(R.string.how_to_play_skinny_desc)
                )
                Spacer(modifier = Modifier.height(10.dp))

                HowToPlayTargetRow(
                    characterRes = R.drawable.char_chubby,
                    foodRes = R.drawable.food_salad,
                    title = stringResource(R.string.how_to_play_chubby_title),
                    description = stringResource(R.string.how_to_play_chubby_desc)
                )
                Spacer(modifier = Modifier.height(10.dp))

                HowToPlayTargetRow(
                    characterRes = R.drawable.char_trash,
                    foodRes = R.drawable.food_hazard,
                    title = stringResource(R.string.how_to_play_trash_title),
                    description = stringResource(R.string.how_to_play_trash_desc)
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                Text(
                    text = "• ${stringResource(R.string.how_to_play_rule_sudden_death)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "• ${stringResource(R.string.how_to_play_rule_speed)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Reset Defaults Button
            OutlinedButton(
                onClick = {
                    scope.launch {
                        settingsPreferences.resetToDefaults()
                        AppCompatDelegate.setApplicationLocales(
                            LocaleListCompat.getEmptyLocaleList()
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = stringResource(R.string.settings_reset_default))
            }

            Text(
                text = stringResource(R.string.settings_about_footer),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 16.dp)
            )
        }
    }
}

@Composable
private fun SettingsSectionCard(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(10.dp))
            content()
        }
    }
}

@Composable
private fun ToggleSettingRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
private fun HowToPlayTargetRow(
    @DrawableRes characterRes: Int,
    @DrawableRes foodRes: Int,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = characterRes),
            contentDescription = title,
            modifier = Modifier.size(44.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Image(
            painter = painterResource(id = foodRes),
            contentDescription = null,
            modifier = Modifier.size(36.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}