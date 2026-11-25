package com.cs364.foodrush.ui.screens

import androidx.lifecycle.ViewModel
import com.cs364.foodrush.game.GameEngine

/** Scoped to the gameplay navigation entry; survives Activity configuration changes. */
class GameplaySessionViewModel : ViewModel() {
    var onCorrectMatch: () -> Unit = {}
    var onSpeedLevelUp: (Int) -> Unit = {}
    val engine = GameEngine(
        onCorrectMatch = { onCorrectMatch() },
        onSpeedLevelUp = { onSpeedLevelUp(it) }
    )
    override fun onCleared() {
        engine.pauseGame()
        onCorrectMatch = {}
        onSpeedLevelUp = {}
    }
}

internal data class GameplayArenaLayout(
    val beltWidth: Float, val sideWidth: Float, val trashSize: Float,
    val skinnyHeight: Float, val chubbyHeight: Float, val feetInset: Float
)

/** All inputs/outputs use dp. Reserve real side lanes instead of overlapping the belt. */
internal fun gameplayArenaLayout(width: Float, height: Float): GameplayArenaLayout {
    val belt = (width * 0.28f).coerceIn(72f, 142f)
    val side = ((width - belt) / 2f - 16f).coerceAtLeast(1f)
    val trash = minOf(width * 0.25f, height * 0.18f, 110f)
    val feet = maxOf(trash + 20f, height * 0.22f)
    val skinny = minOf(side / 0.57f, (height - feet - 100f).coerceAtLeast(1f), 360f)
    return GameplayArenaLayout(belt, side, trash, skinny, skinny * 0.82f, feet)
}

internal data class KoBannerPlacement(
    val topInset: Float,
    val maxWidth: Float,
    val maxHeight: Float
)

/** All inputs/outputs use dp. Keeps the KO Retry banner in the clear band above both characters. */
internal fun koRetryBannerPlacement(
    arenaWidth: Float,
    arenaHeight: Float,
    hudTopInset: Float,
    layout: GameplayArenaLayout
): KoBannerPlacement {
    val compact = arenaHeight < 680f || arenaWidth < 360f
    val topInset = hudTopInset + if (compact) 8f else 14f
    val characterTopY = arenaHeight - layout.feetInset - layout.skinnyHeight
    val availableAboveCharacters = characterTopY - topInset - 8f
    val maxHeight = availableAboveCharacters.coerceAtLeast(136f).coerceAtMost(220f)
    val maxWidth = (arenaWidth - 28f).coerceAtMost(320f).coerceAtLeast(200f)
    return KoBannerPlacement(topInset = topInset, maxWidth = maxWidth, maxHeight = maxHeight)
}
