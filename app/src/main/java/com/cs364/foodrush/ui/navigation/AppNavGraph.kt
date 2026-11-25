package com.cs364.foodrush.ui.navigation

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.cs364.foodrush.R
import com.cs364.foodrush.data.ScoreRepository
import com.cs364.foodrush.data.settings.SettingsPreferences
import com.cs364.foodrush.data.settings.UserSettings
import com.cs364.foodrush.game.HapticAudioManager
import com.cs364.foodrush.ui.components.PixelArcadeBox
import com.cs364.foodrush.ui.screens.GameOverScreen
import com.cs364.foodrush.ui.screens.GameplayScreen
import com.cs364.foodrush.ui.screens.MainMenuScreen
import com.cs364.foodrush.ui.screens.ScoreHistoryScreen
import com.cs364.foodrush.ui.settings.SettingsScreen
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.io.IOException

private val Context.highScoreDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "food_rush_high_score"
)

/**
 * Lightweight DataStore wrapper for device high score persistence until Role 4
 * connects the full ScoreRepository leaderboard database.
 */
class HighScorePreferences(context: Context) {
    private val appContext = context.applicationContext

    private object Keys {
        val BEST_SCORE = intPreferencesKey("best_score")
    }

    val bestScoreFlow: Flow<Int> = appContext.highScoreDataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { prefs -> prefs[Keys.BEST_SCORE] ?: 0 }

    suspend fun recordScore(score: Int) {
        if (score <= 0) return
        appContext.highScoreDataStore.edit { prefs ->
            val current = prefs[Keys.BEST_SCORE] ?: 0
            if (score > current) {
                prefs[Keys.BEST_SCORE] = score
            }
        }
    }
}

/**
 * Canonical navigation routes for the 5-screen flow in AGENTS.md (Rubric 4.1).
 */
object AppRoute {
    const val MAIN_MENU = "main_menu"
    const val GAMEPLAY = "gameplay"
    const val ARG_SCORE = "score"
    const val ARG_FAIL_REASON = "failReasonResId"
    const val ARG_FAIL_FOOD = "failFoodNameResId"
    const val GAME_OVER_PATTERN =
        "game_over/{$ARG_SCORE}/{$ARG_FAIL_REASON}/{$ARG_FAIL_FOOD}"
    const val SCORE_HISTORY = "score_history"
    const val SETTINGS = "settings"

    fun gameOver(score: Int, failReasonResId: Int, failFoodNameResId: Int): String {
        return buildGameOverRoute(score, failReasonResId, failFoodNameResId)
    }
}

internal fun buildGameOverRoute(
    score: Int,
    @StringRes failReasonResId: Int,
    @StringRes failFoodNameResId: Int
): String {
    val safeScore = score.coerceAtLeast(0)
    return "game_over/$safeScore/$failReasonResId/$failFoodNameResId"
}

internal fun sanitizeFailReasonResId(@StringRes candidate: Int): Int {
    return when (candidate) {
        R.string.fail_fed_clean_to_skinny,
        R.string.fail_fed_high_cal_to_chubby,
        R.string.fail_fed_hazard_to_person,
        R.string.fail_trashed_good_food,
        R.string.fail_missed_item -> candidate
        else -> R.string.fail_missed_item
    }
}

internal fun sanitizeFailFoodNameResId(@StringRes candidate: Int): Int {
    return when (candidate) {
        R.string.food_burger,
        R.string.food_salad,
        R.string.food_scraps -> candidate
        else -> R.string.food_burger
    }
}

/**
 * Role 3: 5-Screen Navigation Graph connecting Role 2 Gameplay, Role 5 Settings,
 * Role 3 Main Menu + HapticAudioManager, and Role 4 bridge destinations (Rubric 1.3 & 4.1).
 */
@Composable
fun AppNavGraph(
    settingsPreferences: SettingsPreferences,
    hapticAudioManager: HapticAudioManager,
    onExitApp: () -> Unit,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val highScorePreferences = remember(context) { HighScorePreferences(context) }
    val scoreRepository = remember(context) { ScoreRepository(context) }

    val userSettings by settingsPreferences.settingsFlow.collectAsStateWithLifecycle(
        initialValue = UserSettings()
    )
    val prefsBestScore by highScorePreferences.bestScoreFlow.collectAsStateWithLifecycle(
        initialValue = 0
    )
    val savedScores by scoreRepository.scores.collectAsStateWithLifecycle(
        initialValue = emptyList()
    )
    val bestScore = maxOf(prefsBestScore, savedScores.maxOfOrNull { it.score } ?: 0)

    LaunchedEffect(userSettings) {
        hapticAudioManager.updateSettings(userSettings)
    }

    NavHost(
        navController = navController,
        startDestination = AppRoute.MAIN_MENU,
        modifier = modifier.fillMaxSize()
    ) {
        composable(AppRoute.MAIN_MENU) {
            MainMenuScreen(
                highScore = bestScore,
                isSoundEnabled = userSettings.isSoundEnabled,
                isHapticEnabled = userSettings.isHapticEnabled,
                onPlayClick = {
                    hapticAudioManager.playUiClick()
                    navController.navigate(AppRoute.GAMEPLAY) {
                        launchSingleTop = true
                    }
                },
                onScoreHistoryClick = {
                    hapticAudioManager.playUiClick()
                    navController.navigate(AppRoute.SCORE_HISTORY) {
                        launchSingleTop = true
                    }
                },
                onSettingsClick = {
                    hapticAudioManager.playUiClick()
                    navController.navigate(AppRoute.SETTINGS) {
                        launchSingleTop = true
                    }
                },
                onConfirmExitApp = {
                    hapticAudioManager.playUiClick()
                    onExitApp()
                }
            )
        }

        composable(AppRoute.GAMEPLAY) {
            DisposableEffect(Unit) {
                hapticAudioManager.startGameplaySession()
                onDispose {
                    hapticAudioManager.stopGameplaySession()
                }
            }

            GameplayScreen(
                onNavigateBackToMenu = {
                    hapticAudioManager.playUiClick()
                    hapticAudioManager.stopGameplaySession()
                    navController.popBackStack(AppRoute.MAIN_MENU, inclusive = false)
                },
                onNavigateToGameOver = { score, failReasonResId, failFoodNameResId ->
                    scope.launch { highScorePreferences.recordScore(score) }
                    hapticAudioManager.stopGameplaySession()
                    navController.navigate(
                        AppRoute.gameOver(score, failReasonResId, failFoodNameResId)
                    ) {
                        popUpTo(AppRoute.GAMEPLAY) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onCorrectMatchFeedback = { hapticAudioManager.playCorrectMatch() },
                onSpeedLevelUpFeedback = { level -> hapticAudioManager.playSpeedLevelUp(level) },
                onGameOverFeedback = { hapticAudioManager.playGameOver() },
                onPauseAudio = { hapticAudioManager.pauseAudio() },
                onResumeAudio = { hapticAudioManager.resumeAudio() },
                onRetryGame = { hapticAudioManager.startGameplaySession() },
                showRetryOnGameOver = false
            )
        }

        composable(
            route = AppRoute.GAME_OVER_PATTERN,
            arguments = listOf(
                navArgument(AppRoute.ARG_SCORE) { type = NavType.IntType },
                navArgument(AppRoute.ARG_FAIL_REASON) { type = NavType.IntType },
                navArgument(AppRoute.ARG_FAIL_FOOD) { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val score = backStackEntry.arguments?.getInt(AppRoute.ARG_SCORE) ?: 0
            val failReasonResId = sanitizeFailReasonResId(
                backStackEntry.arguments?.getInt(AppRoute.ARG_FAIL_REASON)
                    ?: R.string.fail_missed_item
            )
            val failFoodResId = sanitizeFailFoodNameResId(
                backStackEntry.arguments?.getInt(AppRoute.ARG_FAIL_FOOD)
                    ?: R.string.food_burger
            )
            val displayBest = maxOf(bestScore, score)
            val failFoodName = stringResource(failFoodResId)
            val failReasonText = stringResource(failReasonResId, failFoodName)

            // Back returns cleanly to MainMenu without popping into a dead Gameplay board (Rubric 4.1)
            BackHandler {
                hapticAudioManager.playUiClick()
                navController.popBackStack(AppRoute.MAIN_MENU, inclusive = false)
            }

            GameOverScreen(
                score = score,
                bestScore = displayBest,
                failReason = failReasonText,
                repository = scoreRepository,
                onPlayAgain = {
                    hapticAudioManager.playUiClick()
                    navController.navigate(AppRoute.GAMEPLAY) {
                        popUpTo(AppRoute.MAIN_MENU) { inclusive = false }
                        launchSingleTop = true
                    }
                },
                onViewHistory = {
                    hapticAudioManager.playUiClick()
                    navController.navigate(AppRoute.SCORE_HISTORY) {
                        popUpTo(AppRoute.MAIN_MENU) { inclusive = false }
                        launchSingleTop = true
                    }
                },
                onNavigateHome = {
                    hapticAudioManager.playUiClick()
                    navController.popBackStack(AppRoute.MAIN_MENU, inclusive = false)
                }
            )
        }

        composable(AppRoute.SCORE_HISTORY) {
            BackHandler {
                hapticAudioManager.playUiClick()
                navController.popBackStack(AppRoute.MAIN_MENU, inclusive = false)
            }

            ScoreHistoryScreen(
                repository = scoreRepository,
                onNavigateBack = {
                    hapticAudioManager.playUiClick()
                    navController.popBackStack(AppRoute.MAIN_MENU, inclusive = false)
                }
            )
        }

        composable(AppRoute.SETTINGS) {
            SettingsScreen(
                settingsPreferences = settingsPreferences,
                onNavigateBack = {
                    hapticAudioManager.playUiClick()
                    navController.popBackStack(AppRoute.MAIN_MENU, inclusive = false)
                }
            )
        }
    }
}
