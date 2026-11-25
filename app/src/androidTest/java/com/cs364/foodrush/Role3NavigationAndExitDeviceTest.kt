package com.cs364.foodrush

import android.os.SystemClock
import android.view.KeyEvent
import android.view.accessibility.AccessibilityNodeInfo
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Role 3 Android device/emulator tests verifying real 5-screen navigation,
 * sub-screen Back returns to MainMenu (Rubric 4.1), Main Menu exit confirmation
 * guard (Rubric 4.2), and Gameplay -> PauseExitDialog -> MainMenu transition.
 */
@RunWith(AndroidJUnit4::class)
class Role3NavigationAndExitDeviceTest {

    @Test
    fun mainMenuSubScreensReturnToMenuOnBackAndGuardAppExit() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            val instrumentation = InstrumentationRegistry.getInstrumentation()
            lateinit var playText: String
            lateinit var historyText: String
            lateinit var settingsText: String
            lateinit var settingsTitle: String
            lateinit var leaderboardTitle: String
            lateinit var exitTitle: String
            lateinit var exitStayText: String

            scenario.onActivity { activity ->
                playText = activity.getString(R.string.menu_play_game)
                historyText = activity.getString(R.string.menu_score_history)
                settingsText = activity.getString(R.string.menu_settings_how_to)
                settingsTitle = activity.getString(R.string.settings_title)
                leaderboardTitle = activity.getString(R.string.leaderboard_title)
                exitTitle = activity.getString(R.string.exit_app_dialog_title)
                exitStayText = activity.getString(R.string.exit_app_dialog_stay)
            }

            // 1. Main Menu is displayed on launch
            val playNode = waitForClickableNode(playText)
            assertNotNull("Expected Play Game button on MainMenu", playNode)

            // 2. Navigate MainMenu -> Settings -> Back -> MainMenu (Rubric 4.1)
            val settingsNode = waitForClickableNode(settingsText)
            assertNotNull(settingsNode)
            assertTrue(settingsNode!!.performAction(AccessibilityNodeInfo.ACTION_CLICK))
            assertTrue(
                "Expected SettingsScreen to open",
                waitUntil(3000L) { hasTextNode(settingsTitle) }
            )

            instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK)
            assertTrue(
                "Expected Back from Settings to return to MainMenu",
                waitUntil(3000L) { hasTextNode(playText) && !hasTextNode(settingsTitle) }
            )

            // 3. Navigate MainMenu -> Score History -> Back -> MainMenu (Rubric 4.1)
            val historyNode = waitForClickableNode(historyText)
            assertNotNull(historyNode)
            assertTrue(historyNode!!.performAction(AccessibilityNodeInfo.ACTION_CLICK))
            assertTrue(
                "Expected ScoreHistory bridge screen to open",
                waitUntil(3000L) { hasTextNode(leaderboardTitle) }
            )

            instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK)
            assertTrue(
                "Expected Back from ScoreHistory to return to MainMenu",
                waitUntil(3000L) { hasTextNode(playText) && !hasTextNode(leaderboardTitle) }
            )

            // 4. Back on MainMenu opens ExitAppConfirmDialog instead of closing immediately
            instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK)
            assertTrue(
                "Expected ExitAppConfirmDialog when pressing Back on MainMenu",
                waitUntil(3000L) { hasTextNode(exitTitle) }
            )
            scenario.onActivity { assertFalse(it.isFinishing) }

            val stayNode = waitForClickableNode(exitStayText)
            assertNotNull(stayNode)
            assertTrue(stayNode!!.performAction(AccessibilityNodeInfo.ACTION_CLICK))
            assertTrue(
                "Expected STAY button to dismiss dialog and keep MainMenu open",
                waitUntil(3000L) { !hasTextNode(exitTitle) && hasTextNode(playText) }
            )
        }
    }

    @Test
    fun gameplayBackShowsPauseDialogAndExitReturnsToMainMenu() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            val instrumentation = InstrumentationRegistry.getInstrumentation()
            lateinit var playText: String
            lateinit var pauseCd: String
            lateinit var pauseTitle: String
            lateinit var pauseExitText: String

            scenario.onActivity { activity ->
                playText = activity.getString(R.string.menu_play_game)
                pauseCd = activity.getString(R.string.gameplay_pause_cd)
                pauseTitle = activity.getString(R.string.pause_dialog_title)
                pauseExitText = activity.getString(R.string.pause_dialog_exit)
            }

            val playNode = waitForClickableNode(playText)
            assertNotNull(playNode)
            assertTrue(playNode!!.performAction(AccessibilityNodeInfo.ACTION_CLICK))

            assertTrue(
                "Expected GameplayScreen to open with Pause button",
                waitUntil(3000L) { findNodeByCdOrText(pauseCd, pauseCd) != null }
            )

            instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK)
            assertTrue(
                "Expected Back during Gameplay to open PauseExitDialog",
                waitUntil(3000L) { hasTextNode(pauseTitle) }
            )

            val exitToMenuNode = waitForClickableNode(pauseExitText)
            assertNotNull(exitToMenuNode)
            assertTrue(exitToMenuNode!!.performAction(AccessibilityNodeInfo.ACTION_CLICK))
            assertTrue(
                "Expected Exit to Menu to return cleanly to MainMenu",
                waitUntil(3000L) { hasTextNode(playText) && !hasTextNode(pauseTitle) }
            )
        }
    }

    private fun waitUntil(timeoutMs: Long = 3000L, condition: () -> Boolean): Boolean {
        val deadline = SystemClock.uptimeMillis() + timeoutMs
        while (SystemClock.uptimeMillis() < deadline) {
            if (condition()) return true
            Thread.sleep(60)
        }
        return condition()
    }

    private fun hasTextNode(targetText: String): Boolean {
        val root = InstrumentationRegistry.getInstrumentation().uiAutomation.rootInActiveWindow
            ?: return false
        return findNodeByCdOrText(targetText, targetText, root) != null
    }

    private fun waitForClickableNode(targetText: String, timeoutMs: Long = 3000L): AccessibilityNodeInfo? {
        val deadline = SystemClock.uptimeMillis() + timeoutMs
        while (SystemClock.uptimeMillis() < deadline) {
            val root = InstrumentationRegistry.getInstrumentation().uiAutomation.rootInActiveWindow
            if (root != null) {
                val found = findClickableNode(root, targetText, targetText)
                if (found != null) return found
            }
            Thread.sleep(60)
        }
        return null
    }

    private fun findNodeByCdOrText(
        targetCd: String,
        targetText: String,
        node: AccessibilityNodeInfo? = InstrumentationRegistry.getInstrumentation().uiAutomation.rootInActiveWindow
    ): AccessibilityNodeInfo? {
        node ?: return null
        if (node.contentDescription?.toString() == targetCd || node.text?.toString() == targetText) {
            return node
        }
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            val found = findNodeByCdOrText(targetCd, targetText, child)
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
}
