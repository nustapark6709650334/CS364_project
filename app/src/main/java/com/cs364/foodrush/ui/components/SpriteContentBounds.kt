package com.cs364.foodrush.ui.components

internal data class SpriteContentBounds(val left: Int, val top: Int, val width: Int, val height: Int)

/** Content bounds ignore alpha < 128; leave a one-pixel gutter for the outline. */
internal fun spriteContentBounds(pixels: IntArray, width: Int, height: Int): SpriteContentBounds {
    var left = width
    var top = height
    var right = -1
    var bottom = -1
    pixels.forEachIndexed { index, color ->
        if ((color ushr 24) >= 128) {
            val x = index % width
            val y = index / width
            left = minOf(left, x); right = maxOf(right, x)
            top = minOf(top, y); bottom = maxOf(bottom, y)
        }
    }
    if (right < left) return SpriteContentBounds(0, 0, width, height)
    left = (left - 1).coerceAtLeast(0); top = (top - 1).coerceAtLeast(0)
    right = (right + 1).coerceAtMost(width - 1); bottom = (bottom + 1).coerceAtMost(height - 1)
    return SpriteContentBounds(left, top, right - left + 1, bottom - top + 1)
}
