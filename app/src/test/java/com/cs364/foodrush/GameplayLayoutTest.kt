package com.cs364.foodrush

import com.cs364.foodrush.R
import com.cs364.foodrush.ui.components.spriteContentBounds
import com.cs364.foodrush.ui.screens.gameplayArenaLayout
import com.cs364.foodrush.ui.screens.koRetryBannerPlacement
import com.cs364.foodrush.ui.screens.resolveRole2FailReasonResId
import com.cs364.foodrush.ui.screens.resolveRole2FoodNameResId
import org.junit.Assert.*
import org.junit.Test

class GameplayLayoutTest {
    @Test fun spriteBoundsIgnoreTransparentPaddingAndFaintSpeckles() {
        val pixels = IntArray(100)
        pixels[0] = 0x01000000
        for (y in 3..6) for (x in 4..5) pixels[y * 10 + x] = -1
        val bounds = spriteContentBounds(pixels, 10, 10)
        assertEquals(3, bounds.left); assertEquals(2, bounds.top)
        assertEquals(4, bounds.width); assertEquals(6, bounds.height)
        assertEquals(10, spriteContentBounds(IntArray(100), 10, 10).width)
    }

    @Test fun sideLanesAndCollapsedBodiesFitSmallLargeAndLandscapeScreens() {
        for ((width, height) in listOf(320f to 480f, 360f to 640f, 412f to 915f, 800f to 1200f, 640f to 360f)) {
            val l = gameplayArenaLayout(width, height)
            assertTrue(l.sideWidth * 2 + l.beltWidth + 32 <= width + 0.01f)
            assertTrue(l.skinnyHeight + l.feetInset + 100 <= height + 0.01f)
            assertTrue(l.chubbyHeight < l.skinnyHeight)
            assertTrue(l.skinnyHeight + l.sideWidth + 16 < width)
            assertTrue(l.chubbyHeight * 1.16f + l.sideWidth + 16 < width)
            assertTrue(l.feetInset > l.trashSize)
        }
    }

    @Test fun koRetryBannerPlacementStaysAboveCharactersOnPortraitScreens() {
        for ((width, height) in listOf(360f to 640f, 412f to 915f, 800f to 1200f)) {
            val layout = gameplayArenaLayout(width, height)
            val hudTopInset = if (height < 680f || width < 360f) 68f else 78f
            val placement = koRetryBannerPlacement(width, height, hudTopInset, layout)
            val characterTopY = height - layout.feetInset - layout.skinnyHeight
            assertTrue(placement.topInset >= hudTopInset)
            assertTrue(placement.maxWidth <= width)
            assertTrue(placement.topInset + 132f <= characterTopY)
        }
        val landscapeLayout = gameplayArenaLayout(640f, 360f)
        val landscapePlacement = koRetryBannerPlacement(640f, 360f, 68f, landscapeLayout)
        assertTrue(landscapePlacement.maxWidth > 0f && landscapePlacement.maxWidth <= 640f)
        assertTrue(landscapePlacement.maxHeight > 0f)
    }

    @Test fun role2FailReasonAndFoodNameMappingsCoverAllEngineStrings() {
        assertEquals(R.string.gameplay_target_skinny_food, resolveRole2FoodNameResId(R.string.food_burger))
        assertEquals(R.string.gameplay_target_chubby_food, resolveRole2FoodNameResId(R.string.food_salad))
        assertEquals(R.string.gameplay_target_trash_food, resolveRole2FoodNameResId(R.string.food_scraps))

        assertEquals(R.string.gameplay_ko_fail_clean_to_skinny, resolveRole2FailReasonResId(R.string.fail_fed_clean_to_skinny))
        assertEquals(R.string.gameplay_ko_fail_high_cal_to_chubby, resolveRole2FailReasonResId(R.string.fail_fed_high_cal_to_chubby))
        assertEquals(R.string.gameplay_ko_fail_hazard_to_person, resolveRole2FailReasonResId(R.string.fail_fed_hazard_to_person))
        assertEquals(R.string.gameplay_ko_fail_trashed_good_food, resolveRole2FailReasonResId(R.string.fail_trashed_good_food))
        assertEquals(R.string.gameplay_ko_fail_missed_item, resolveRole2FailReasonResId(R.string.fail_missed_item))

        assertEquals(123456, resolveRole2FoodNameResId(123456))
        assertEquals(654321, resolveRole2FailReasonResId(654321))
    }
}
