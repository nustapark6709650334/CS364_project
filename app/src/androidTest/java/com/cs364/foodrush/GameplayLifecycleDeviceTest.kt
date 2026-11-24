package com.cs364.foodrush

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.pressBack
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.lifecycle.Lifecycle
import com.cs364.foodrush.game.GameEngine
import com.cs364.foodrush.game.GameUiState
import com.cs364.foodrush.game.TargetType
import com.cs364.foodrush.ui.screens.GameplayQaActivity
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GameplayLifecycleDeviceTest {
    @Test fun backgroundAndRecreationKeepSamePausedGame() {
        ActivityScenario.launch(GameplayQaActivity::class.java).use { scenario ->
            lateinit var engine: GameEngine
            lateinit var snapshot: GameUiState
            scenario.onActivity { activity ->
                engine = activity.session.engine
                repeat(5) {
                    val item = engine.uiState.value.items.first()
                    engine.submitItemToTarget(item.id, item.type.category.expectedTarget)
                }
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
            pressBack() // Dismiss the pause dialog: cancel exit and resume.
            scenario.onActivity { assertFalse(engine.uiState.value.isPaused) }
            pressBack() // Open pause dialog again.
            scenario.onActivity { assertTrue(engine.uiState.value.isPaused); assertEquals(0, it.menuCalls) }
        }
    }

    @Test fun koConsumesBackAndDefersNavigationWhileBackgrounded() {
        ActivityScenario.launch(GameplayQaActivity::class.java).use { scenario ->
            scenario.onActivity {
                val engine = it.session.engine
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
            val deadline = android.os.SystemClock.uptimeMillis() + 10000
            var navigationCount = 0
            while (navigationCount == 0 && android.os.SystemClock.uptimeMillis() < deadline) {
                Thread.sleep(100)
                scenario.onActivity { navigationCount = it.gameOverCalls }
            }
            scenario.onActivity { assertEquals(1, it.gameOverCalls) }
        }
    }
}
