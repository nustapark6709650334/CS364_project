package com.cs364.foodrush

import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.KeyEvent
import android.view.accessibility.AccessibilityNodeInfo
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.cs364.foodrush.game.GameEngine
import com.cs364.foodrush.game.GameUiState
import com.cs364.foodrush.game.TargetType
import com.cs364.foodrush.ui.screens.GameplayQaActivity
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class GameplayLifecycleDeviceTest {
    @Test fun backgroundAndRecreationKeepSamePausedGame() {
        ActivityScenario.launch(GameplayQaActivity::class.java).use { scenario ->
            val instrumentation = InstrumentationRegistry.getInstrumentation()
            resetFreshRound(scenario)
            lateinit var engine: GameEngine
            lateinit var snapshot: GameUiState
            lateinit var resumeText: String
            scenario.onActivity { activity ->
                resumeText = activity.getString(R.string.pause_dialog_resume)
                engine = activity.session.engine
                engine.resetGame()
                repeat(5) {
                    val item = engine.uiState.value.items.first()
                    engine.submitItemToTarget(item.id, item.type.category.expectedTarget)
                }
                assertEquals(5, engine.uiState.value.score)
            }
            scenario.moveToState(Lifecycle.State.CREATED)
            scenario.onActivity {
                snapshot = engine.uiState.value
                assertTrue(snapshot.isPaused)
                assertTrue(it.pauseAudioCalls > 0)
            }
            scenario.moveToState(Lifecycle.State.RESUMED)
            scenario.recreate()
            scenario.onActivity {
                assertSame(engine, it.session.engine)
                assertEquals(snapshot, it.session.engine.uiState.value)
            }
            assertTrue(
                "Expected PauseExitDialog after Activity recreation",
                waitUntil(3000L) {
                    val root = instrumentation.uiAutomation.rootInActiveWindow
                    root != null && findNodeMatching(root, resumeText, resumeText) != null
                }
            )
            instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK)
            assertTrue(
                "Expected Back to dismiss pause dialog and resume game",
                waitUntil(3000L) {
                    val root = instrumentation.uiAutomation.rootInActiveWindow
                    !engine.uiState.value.isPaused &&
                        root != null &&
                        findNodeMatching(root, resumeText, resumeText) == null
                }
            )
            Thread.sleep(100)
            runOnMainNow {
                assertFalse(engine.uiState.value.isPaused)
                assertFalse(engine.uiState.value.isGameOver)
            }
            instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK)
            assertTrue(
                "Expected Back during gameplay to pause game again",
                waitUntil(3000L) { engine.uiState.value.isPaused }
            )
            scenario.onActivity { assertTrue(engine.uiState.value.isPaused); assertEquals(0, it.menuCalls) }
        }
    }

    @Test fun koConsumesBackAndDefersNavigationWhileBackgrounded() {
        ActivityScenario.launch(GameplayQaActivity::class.java).use { scenario ->
            resetFreshRound(scenario)
            scenario.onActivity {
                val engine = it.session.engine
                engine.resetGame()
                val item = engine.uiState.value.items.first()
                val wrong = TargetType.entries.first { target -> target != item.type.category.expectedTarget }
                engine.submitItemToTarget(item.id, wrong)
                it.onBackPressedDispatcher.onBackPressed()
                assertFalse(it.isFinishing)
            }
            scenario.moveToState(Lifecycle.State.CREATED)
            Thread.sleep(1600)
            scenario.onActivity { assertEquals(0, it.gameOverCalls) }
            scenario.moveToState(Lifecycle.State.RESUMED)
            val deadline = SystemClock.uptimeMillis() + 10000
            var navigationCount = 0
            while (navigationCount == 0 && SystemClock.uptimeMillis() < deadline) {
                Thread.sleep(100)
                scenario.onActivity { navigationCount = it.gameOverCalls }
            }
            scenario.onActivity { assertEquals(1, it.gameOverCalls) }
        }
    }

    @Test fun koRetryButtonResetsSessionAndAllowsSecondRound() {
        ActivityScenario.launch(GameplayQaActivity::class.java).use { scenario ->
            val instrumentation = InstrumentationRegistry.getInstrumentation()
            resetFreshRound(scenario)
            lateinit var qaActivity: GameplayQaActivity
            lateinit var engine: GameEngine
            lateinit var retryCd: String
            lateinit var retryText: String

            scenario.onActivity { activity ->
                qaActivity = activity
                engine = activity.session.engine
                retryCd = activity.getString(R.string.gameplay_ko_retry_cd)
                retryText = activity.getString(R.string.gameplay_ko_retry)
                engine.resetGame()
                repeat(2) {
                    val item = engine.uiState.value.items.first()
                    engine.submitItemToTarget(item.id, item.type.category.expectedTarget)
                }
                assertEquals(2, engine.uiState.value.score)
                val failItem = engine.uiState.value.items.first()
                val wrong = TargetType.entries.first { target -> target != failItem.type.category.expectedTarget }
                engine.submitItemToTarget(failItem.id, wrong)
            }

            val deadline = SystemClock.uptimeMillis() + 10000
            var retryNode: AccessibilityNodeInfo? = null
            while (retryNode == null && SystemClock.uptimeMillis() < deadline) {
                val root = instrumentation.uiAutomation.rootInActiveWindow
                retryNode = root?.let { findClickableNode(it, retryCd, retryText) }
                if (retryNode == null) Thread.sleep(100)
            }
            assertNotNull("Expected KO Retry button to appear after Game Over", retryNode)

            scenario.onActivity { activity ->
                assertEquals(1, activity.gameOverCalls)
                assertTrue(activity.session.engine.uiState.value.isGameOver)
            }

            assertTrue(retryNode!!.performAction(AccessibilityNodeInfo.ACTION_CLICK))

            assertTrue(
                "Expected Retry click to reset game session and hide banner",
                waitUntil(3000L) {
                    val root = instrumentation.uiAutomation.rootInActiveWindow
                    !engine.uiState.value.isGameOver &&
                        root != null &&
                        findClickableNode(root, retryCd, retryText) == null
                }
            )

            runOnMainNow {
                val state = engine.uiState.value
                assertEquals(1, qaActivity.retryCalls)
                assertTrue(qaActivity.resumeAudioCalls >= 1)
                assertEquals(0, state.score)
                assertFalse(state.isGameOver)
                assertFalse(state.isPaused)
                assertTrue(state.items.isNotEmpty())

                val item = state.items.first()
                engine.submitItemToTarget(item.id, item.type.category.expectedTarget)
                assertEquals(1, engine.uiState.value.score)
                assertFalse(engine.uiState.value.isGameOver)

                val secondFailItem = engine.uiState.value.items.first()
                val secondWrong = TargetType.entries.first { target -> target != secondFailItem.type.category.expectedTarget }
                engine.submitItemToTarget(secondFailItem.id, secondWrong)
            }

            val secondDeadline = SystemClock.uptimeMillis() + 10000
            var gameOverCount = 0
            while (gameOverCount < 2 && SystemClock.uptimeMillis() < secondDeadline) {
                Thread.sleep(100)
                runOnMainNow { gameOverCount = qaActivity.gameOverCalls }
            }
            assertEquals(2, gameOverCount)
        }
    }

    private fun runOnMainNow(block: () -> Unit) {
        var failure: Throwable? = null
        val latch = CountDownLatch(1)
        Handler(Looper.getMainLooper()).post {
            try {
                block()
            } catch (t: Throwable) {
                failure = t
            } finally {
                latch.countDown()
            }
        }
        assertTrue("Main-thread action timed out", latch.await(5, TimeUnit.SECONDS))
        failure?.let { throw it }
    }

    private fun waitUntil(timeoutMs: Long, condition: () -> Boolean): Boolean {
        val deadline = SystemClock.uptimeMillis() + timeoutMs
        while (SystemClock.uptimeMillis() < deadline) {
            if (condition()) return true
            Thread.sleep(50)
        }
        return condition()
    }

    private fun findNodeMatching(
        node: AccessibilityNodeInfo,
        targetCd: String,
        targetText: String
    ): AccessibilityNodeInfo? {
        if (node.contentDescription?.toString() == targetCd || node.text?.toString() == targetText) {
            return node
        }
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            val found = findNodeMatching(child, targetCd, targetText)
            if (found != null) return found
        }
        return null
    }

    private fun findClickableNode(
        node: AccessibilityNodeInfo,
        targetCd: String,
        targetText: String
    ): AccessibilityNodeInfo? {
        if (node.contentDescription?.toString() == targetCd || node.text?.toString() == targetText) {
            var current: AccessibilityNodeInfo? = node
            while (current != null && !current.isClickable) {
                current = current.parent
            }
            if (current != null && current.isClickable) return current
        }
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            val found = findClickableNode(child, targetCd, targetText)
            if (found != null) return found
        }
        return null
    }

    private fun resetFreshRound(scenario: ActivityScenario<GameplayQaActivity>) {
        scenario.onActivity { activity ->
            activity.session.engine.resetGame()
            activity.session.engine.pauseGame()
            activity.gameOverCalls = 0
            activity.menuCalls = 0
            activity.pauseAudioCalls = 0
            activity.resumeAudioCalls = 0
            activity.retryCalls = 0
        }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
    }
}
