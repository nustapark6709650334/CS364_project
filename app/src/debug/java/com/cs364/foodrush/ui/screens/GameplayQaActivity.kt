package com.cs364.foodrush.ui.screens

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.lifecycle.ViewModelProvider
import com.cs364.foodrush.game.*
import com.cs364.foodrush.ui.components.CharacterPose
import com.cs364.foodrush.ui.components.PauseExitDialog
import java.util.Locale

/** Standalone device QA entry point, excluded from release builds. */
class GameplayQaActivity : ComponentActivity() {
    var gameOverCalls = 0
    var menuCalls = 0
    var pauseAudioCalls = 0
    val session get() = ViewModelProvider(this)[GameplaySessionViewModel::class.java]

    override fun onCreate(savedInstanceState: Bundle?) {
        intent.getStringExtra("lang")?.let {
            @Suppress("DEPRECATION")
            resources.updateConfiguration(Configuration(resources.configuration).apply {
                setLocale(Locale.forLanguageTag(it))
            }, resources.displayMetrics)
        }
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = if (intent.getBooleanExtra("night", false)) darkColorScheme() else lightColorScheme()) {
                val pose = intent.getStringExtra("pose")
                if (pose == null) {
                    GameplayScreen(
                        onNavigateBackToMenu = { menuCalls++ },
                        onNavigateToGameOver = { _, _, _ -> gameOverCalls++ },
                        onPauseAudio = { pauseAudioCalls++ }
                    )
                } else {
                    val skinny = when (pose) {
                        "yum" -> CharacterPose.YUMMY
                        "skinny_ko" -> CharacterPose.DEAD
                        "chubby_ko", "sad" -> CharacterPose.SAD
                        else -> CharacterPose.IDLE
                    }
                    val chubby = when (pose) {
                        "yum" -> CharacterPose.YUMMY
                        "chubby_ko" -> CharacterPose.DEAD
                        "skinny_ko", "sad" -> CharacterPose.SAD
                        else -> CharacterPose.IDLE
                    }
                    GameplayBoardContent(
                        uiState = GameUiState(score = 18, speedLevel = 4, speedProgress = 0.65f,
                            items = listOf(ActiveFoodItem(1, FoodType.BURGER, 0.3f), ActiveFoodItem(2, FoodType.SALAD, 0.65f))),
                        beltScrollPhase = 0.3f, highlightedTarget = null,
                        skinnyPose = skinny, chubbyPose = chubby,
                        skinnyFedTick = 0, chubbyFedTick = 0, trashFedTick = 0,
                        shakeOffsetDp = 0f, onHighlightTargetChange = {},
                        onSubmitItem = { _, _ -> }, onPauseClick = {}
                    )
                    if (pose == "pause") PauseExitDialog(18, "1.7x", {}, {})
                }
            }
        }
    }
}
