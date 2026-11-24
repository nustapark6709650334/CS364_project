package com.cs364.foodrush.game

import kotlin.math.floor
import kotlin.math.min
import kotlin.random.Random

/**
 * Role 1: Speed scaling & spawn controller implementing the CS364 mathematical formula (Rubric 1.4 & 2.1):
 *
 *   v(s) = min(v_max, v_0 + k * floor(s / 5))
 *
 * - Starts at [v0] and increases every [POINTS_PER_LEVEL] (5) points until reaching [vMax].
 * - Once [vMax] is reached, conveyor speed stays capped so touch/drag input remains fair,
 *   while challenge continues to scale via a tighter spawn interval and higher hazard ratio (10% -> 25%).
 */
class SpeedController(
    val v0: Float = 0.20f,
    val k: Float = 0.045f,
    val vMax: Float = 0.47f
) {
    companion object {
        const val POINTS_PER_LEVEL = 5
        private const val BASE_SPAWN_INTERVAL_SEC = 1.95f
        private const val MIN_SPAWN_INTERVAL_SEC = 0.92f
        private const val BASE_HAZARD_RATIO = 0.10f
        private const val MAX_HAZARD_RATIO = 0.25f
    }

    fun calculateSpeed(score: Int): Float {
        val safeScore = score.coerceAtLeast(0)
        val tier = floor(safeScore.toDouble() / POINTS_PER_LEVEL.toDouble()).toFloat()
        return min(vMax, v0 + k * tier)
    }

    fun getSpeedLevel(score: Int): Int {
        val safeScore = score.coerceAtLeast(0)
        return 1 + (safeScore / POINTS_PER_LEVEL)
    }

    fun isAtMaxSpeed(score: Int): Boolean {
        return calculateSpeed(score) >= vMax - 1e-4f
    }

    fun getSpeedProgressFraction(score: Int): Float {
        val current = calculateSpeed(score)
        if (vMax <= v0) return 1f
        val raw = (current - v0) / (vMax - v0)
        return (0.20f + 0.80f * raw).coerceIn(0.20f, 1.0f)
    }

    fun getSpeedMultiplierText(score: Int): String {
        val mult = calculateSpeed(score) / v0
        return String.format(java.util.Locale.US, "%.1fx", mult)
    }

    fun didLevelUp(previousScore: Int, newScore: Int): Boolean {
        if (newScore <= previousScore) return false
        return getSpeedLevel(newScore) > getSpeedLevel(previousScore)
    }

    fun getSpawnIntervalSeconds(score: Int): Float {
        val level = getSpeedLevel(score)
        val reduction = (level - 1) * 0.13f
        return (BASE_SPAWN_INTERVAL_SEC - reduction).coerceAtLeast(MIN_SPAWN_INTERVAL_SEC)
    }

    fun getHazardProbability(score: Int): Float {
        return if (!isAtMaxSpeed(score)) {
            val progressToCap = ((calculateSpeed(score) - v0) / (vMax - v0)).coerceIn(0f, 1f)
            BASE_HAZARD_RATIO + 0.08f * progressToCap
        } else {
            MAX_HAZARD_RATIO
        }
    }

    fun nextFoodType(score: Int, random: Random = Random.Default): FoodType {
        val roll = random.nextFloat()
        val hazardProb = getHazardProbability(score)
        return when {
            roll < hazardProb -> FoodType.hazardItems.random(random)
            roll < hazardProb + (1f - hazardProb) / 2f -> FoodType.highCalorieItems.random(random)
            else -> FoodType.cleanFoodItems.random(random)
        }
    }
}
