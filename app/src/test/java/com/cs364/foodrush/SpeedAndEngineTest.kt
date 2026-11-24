package com.cs364.foodrush

import com.cs364.foodrush.game.GameEngine
import com.cs364.foodrush.game.SpeedController
import com.cs364.foodrush.game.TargetType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Role 1 Unit tests verifying the CS364 Speed Cap formula, Delta Time (dt) GameEngine loop,
 * Sudden Death validation, and Pause/Resume dt spike protection (Rubric 1.4, 2.1–2.4, 5.3).
 */
class SpeedAndEngineTest {

    @Test
    fun speedController_followsStepFormulaAndCapsAtVMax() {
        val controller = SpeedController(v0 = 0.20f, k = 0.05f, vMax = 0.40f)

        assertEquals(0.20f, controller.calculateSpeed(0), 1e-4f)
        assertEquals(0.20f, controller.calculateSpeed(4), 1e-4f)
        assertEquals(1, controller.getSpeedLevel(4))

        assertEquals(0.25f, controller.calculateSpeed(5), 1e-4f)
        assertTrue(controller.didLevelUp(4, 5))
        assertEquals(2, controller.getSpeedLevel(5))

        assertEquals(0.40f, controller.calculateSpeed(20), 1e-4f)
        assertEquals(0.40f, controller.calculateSpeed(55), 1e-4f)
        assertTrue(controller.isAtMaxSpeed(20))
        assertEquals(0.25f, controller.getHazardProbability(25), 1e-4f)
    }

    @Test
    fun gameEngine_correctMatchIncrementsScoreAndWrongMatchTriggersSuddenDeath() {
        var correctCount = 0
        var gameOverTriggered = false
        val engine = GameEngine(
            onCorrectMatch = { correctCount++ },
            onGameOver = { _, _, _ -> gameOverTriggered = true }
        )

        val firstItem = engine.uiState.value.items.first()
        val expectedTarget = firstItem.type.category.expectedTarget

        engine.submitItemToTarget(itemId = firstItem.id, target = expectedTarget)
        assertEquals(1, engine.uiState.value.score)
        assertEquals(1, correctCount)
        assertFalse(engine.uiState.value.isGameOver)

        val secondItem = engine.uiState.value.items.first()
        val wrongTarget = TargetType.entries.first {
            it != secondItem.type.category.expectedTarget
        }
        engine.submitItemToTarget(itemId = secondItem.id, target = wrongTarget)
        assertTrue(engine.uiState.value.isGameOver)
        assertTrue(gameOverTriggered)
    }

    @Test
    fun gameEngine_pausePreservesStateAndClampsPostResumeDtSpike() {
        val engine = GameEngine()
        val initialY = engine.uiState.value.items.first().yProgress

        engine.pauseGame()
        engine.update(1.5f)
        assertEquals(initialY, engine.uiState.value.items.first().yProgress, 1e-5f)

        engine.resumeGame()
        engine.update(2.0f)
        val expectedMaxDelta = engine.speedController.calculateSpeed(0) * GameEngine.MAX_FRAME_DT_SECONDS
        assertEquals(initialY + expectedMaxDelta, engine.uiState.value.items.first().yProgress, 1e-4f)
        assertFalse(engine.uiState.value.isGameOver)
    }
}
