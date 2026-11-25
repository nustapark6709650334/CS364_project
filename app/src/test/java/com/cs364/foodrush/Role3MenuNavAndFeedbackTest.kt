package com.cs364.foodrush

import com.cs364.foodrush.game.HapticPatternSpec
import com.cs364.foodrush.game.PcmWaveSynthesizer
import com.cs364.foodrush.ui.navigation.AppRoute
import com.cs364.foodrush.ui.navigation.sanitizeFailFoodNameResId
import com.cs364.foodrush.ui.navigation.sanitizeFailReasonResId
import com.cs364.foodrush.ui.screens.mainMenuLayoutMetrics
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Role 3 Unit Tests verifying CS364 Haptic patterns, 16-bit PCM WAV synthesis,
 * Navigation route builders/sanitizers, and MainMenu responsive layout metrics (Rubric 1.3, 3.1, 4.1).
 */
class Role3MenuNavAndFeedbackTest {

    @Test
    fun hapticPatternSpec_matchesCs364FeedbackTimings() {
        assertEquals(20L, HapticPatternSpec.CORRECT_MATCH_DURATION_MS)
        assertArrayEquals(
            longArrayOf(0L, 40L, 60L, 40L),
            HapticPatternSpec.SPEED_UP_TIMINGS_MS
        )
        assertEquals(
            HapticPatternSpec.SPEED_UP_TIMINGS_MS.size,
            HapticPatternSpec.SPEED_UP_AMPLITUDES.size
        )
        assertEquals(400L, HapticPatternSpec.GAME_OVER_DURATION_MS)
    }

    @Test
    fun pcmWaveSynthesizer_producesValidRiffWaveHeadersAndAudioData() {
        val waves = listOf(
            PcmWaveSynthesizer.buildUiClickWav(),
            PcmWaveSynthesizer.buildCorrectMatchWav(),
            PcmWaveSynthesizer.buildSpeedUpWav(),
            PcmWaveSynthesizer.buildGameOverWav(),
            PcmWaveSynthesizer.buildGameplayBgmWav()
        )

        for (wav in waves) {
            assertTrue("WAV file must be larger than 44-byte header", wav.size > 44)
            assertEquals("RIFF", String(wav.copyOfRange(0, 4), Charsets.US_ASCII))
            assertEquals("WAVE", String(wav.copyOfRange(8, 12), Charsets.US_ASCII))
            assertEquals("fmt ", String(wav.copyOfRange(12, 16), Charsets.US_ASCII))
            assertEquals("data", String(wav.copyOfRange(36, 40), Charsets.US_ASCII))

            val dataChunkSize = (wav[40].toInt() and 0xFF) or
                ((wav[41].toInt() and 0xFF) shl 8) or
                ((wav[42].toInt() and 0xFF) shl 16) or
                ((wav[43].toInt() and 0xFF) shl 24)
            assertEquals(wav.size - 44, dataChunkSize)
            assertTrue("PCM payload must contain non-zero samples", wav.drop(44).any { it.toInt() != 0 })
        }
    }

    @Test
    fun appRoute_buildsValidGameOverRoutesAndSanitizesIds() {
        val route = AppRoute.gameOver(
            score = 24,
            failReasonResId = R.string.fail_fed_clean_to_skinny,
            failFoodNameResId = R.string.food_salad
        )
        assertEquals(
            "game_over/24/${R.string.fail_fed_clean_to_skinny}/${R.string.food_salad}",
            route
        )

        val clampedRoute = AppRoute.gameOver(
            score = -5,
            failReasonResId = R.string.fail_missed_item,
            failFoodNameResId = R.string.food_burger
        )
        assertTrue(clampedRoute.startsWith("game_over/0/"))

        assertEquals(
            R.string.fail_trashed_good_food,
            sanitizeFailReasonResId(R.string.fail_trashed_good_food)
        )
        assertEquals(R.string.fail_missed_item, sanitizeFailReasonResId(999_999))

        assertEquals(R.string.food_scraps, sanitizeFailFoodNameResId(R.string.food_scraps))
        assertEquals(R.string.food_burger, sanitizeFailFoodNameResId(-1))
    }

    @Test
    fun mainMenuLayoutMetrics_scalesCleanlyAcrossPhoneTabletAndLandscape() {
        val sizes = listOf(
            320f to 480f,
            360f to 640f,
            412f to 915f,
            800f to 1200f,
            640f to 360f
        )

        for ((width, height) in sizes) {
            val m = mainMenuLayoutMetrics(width, height)
            assertTrue(m.contentMaxWidthDp <= width)
            assertTrue(m.buttonMinHeightDp >= 48f)
            assertTrue(m.skinnyHeightDp <= m.mascotStageHeightDp)
            assertTrue(m.chubbyHeightDp < m.skinnyHeightDp)
            assertTrue(m.trashSizeDp > 0f && m.foodBadgeSizeDp > 0f)
        }
    }
}
