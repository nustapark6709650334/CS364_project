package com.cs364.foodrush

import com.cs364.foodrush.ui.components.spriteContentBounds
import com.cs364.foodrush.ui.screens.gameplayArenaLayout
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
}
