package com.cs364.foodrush.game

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.cs364.foodrush.R

/**
 * The 3 sorting targets in Fit & Fat: Food Rush.
 * - [SKINNY] (Left): Needs high-calorie food (Burger).
 * - [CHUBBY] (Right): Needs clean food (Vegetable Salad).
 * - [TRASH] (Bottom Center): Disposes of hazards/garbage (Food Scraps).
 */
enum class TargetType {
    SKINNY,
    CHUBBY,
    TRASH
}

enum class FoodCategory(val expectedTarget: TargetType) {
    HIGH_CALORIE(TargetType.SKINNY),
    CLEAN_FOOD(TargetType.CHUBBY),
    HAZARD(TargetType.TRASH)
}

/**
 * The 3 food & hazard types matching the 16-bit pixel art sprites 1-to-1:
 * - [BURGER] (เบอร์เกอร์) -> `R.drawable.food_burger` -> [TargetType.SKINNY]
 * - [SALAD] (สลัดผัก) -> `R.drawable.food_salad` -> [TargetType.CHUBBY]
 * - [FOOD_SCRAPS] (เศษอาหาร) -> `R.drawable.food_hazard` -> [TargetType.TRASH]
 */
enum class FoodType(
    @param:StringRes val nameResId: Int,
    @param:DrawableRes val spriteResId: Int,
    val category: FoodCategory
) {
    BURGER(R.string.food_burger, R.drawable.food_burger, FoodCategory.HIGH_CALORIE),
    SALAD(R.string.food_salad, R.drawable.food_salad, FoodCategory.CLEAN_FOOD),
    FOOD_SCRAPS(R.string.food_scraps, R.drawable.food_hazard, FoodCategory.HAZARD);

    companion object {
        val highCalorieItems = listOf(BURGER)
        val cleanFoodItems = listOf(SALAD)
        val hazardItems = listOf(FOOD_SCRAPS)
    }
}

/**
 * Represents a live food plate moving down the vertical conveyor belt.
 * @param id Unique identifier for drag-and-drop tracking.
 * @param type The [FoodType] on the plate.
 * @param yProgress Normalized vertical position on the belt: `0.0f` (top spawn) to `1.0f` (bottom miss threshold).
 */
data class ActiveFoodItem(
    val id: Long,
    val type: FoodType,
    val yProgress: Float = 0.05f
)
