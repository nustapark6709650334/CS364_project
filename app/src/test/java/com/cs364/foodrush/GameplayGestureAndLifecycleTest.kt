package com.cs364.foodrush

import com.cs364.foodrush.game.GameEngine
import com.cs364.foodrush.game.TargetType
import com.cs364.foodrush.ui.components.resolveDragTarget
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Role 2 Unit tests verifying drag-target gesture resolution (dead-zone protection)
 * and Pause/Lifecycle state preservation (Rubric 4.2 & 4.4).
 */
class GameplayGestureAndLifecycleTest {

    @Test
    fun resolveDragTarget_classifiesLeftRightDownAndIgnoresMicroTaps() {
        val threshold = 42f

        // Sub-threshold micro-taps must not trigger an accidental Sudden Death throw
        assertNull(resolveDragTarget(dragX = 12f, dragY = 10f, thresholdPx = threshold))
        assertNull(resolveDragTarget(dragX = -20f, dragY = 5f, thresholdPx = threshold))
        assertNull(resolveDragTarget(dragX = -60f, dragY = 0f, thresholdPx = 0f))

        // Left -> Skinny, Right -> Chubby, Down -> Trash
        assertEquals(
            TargetType.SKINNY,
            resolveDragTarget(dragX = -64f, dragY = 8f, thresholdPx = threshold)
        )
        assertEquals(
            TargetType.CHUBBY,
            resolveDragTarget(dragX = 68f, dragY = -12f, thresholdPx = threshold)
        )
        assertEquals(
            TargetType.TRASH,
            resolveDragTarget(dragX = 10f, dragY = 62f, thresholdPx = threshold)
        )
    }

    @Test
    fun resolveDragTarget_diagonalDownSwipeSelectsTrashOverHorizontal() {
        val threshold = 42f
        assertEquals(
            TargetType.TRASH,
            resolveDragTarget(dragX = 30f, dragY = 55f, thresholdPx = threshold)
        )
    }

    @Test
    fun lifecyclePause_preservesScoreSpeedAndBeltItemsAcrossBackgroundUpdates() {
        val engine = GameEngine()

        // Score 5 points to reach Level 2 speed before pausing
        repeat(5) {
            val frontItem = engine.uiState.value.items.first()
            engine.submitItemToTarget(
                itemId = frontItem.id,
                target = frontItem.type.category.expectedTarget
            )
        }
        engine.update(0.03f)

        val pausedSnapshot = engine.uiState.value
        assertEquals(5, pausedSnapshot.score)
        assertEquals(2, pausedSnapshot.speedLevel)

        // Simulate ON_PAUSE / ON_STOP or BackHandler pause
        engine.pauseGame()
        engine.update(0.05f)
        engine.update(0.05f)
        engine.submitItemToTarget(target = TargetType.TRASH)

        val whilePaused = engine.uiState.value
        assertTrue(whilePaused.isPaused)
        assertFalse(whilePaused.isGameOver)
        assertEquals(pausedSnapshot.score, whilePaused.score)
        assertEquals(pausedSnapshot.speedLevel, whilePaused.speedLevel)
        assertEquals(pausedSnapshot.speedMultiplierText, whilePaused.speedMultiplierText)
        assertEquals(pausedSnapshot.items, whilePaused.items)

        // Simulate tapping Resume in PauseExitDialog
        engine.resumeGame()
        val afterResume = engine.uiState.value
        assertFalse(afterResume.isPaused)
        assertEquals(5, afterResume.score)
        assertEquals(2, afterResume.speedLevel)

        engine.update(0.02f)
        assertFalse(engine.uiState.value.isGameOver)
        assertTrue(
            engine.uiState.value.items.first().yProgress >
                pausedSnapshot.items.first().yProgress
        )
    }
}
