package com.cs364.foodrush.game

import androidx.annotation.StringRes
import com.cs364.foodrush.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Immutable snapshot of the active game state for Jetpack Compose UI rendering.
 */
data class GameUiState(
    val score: Int = 0,
    val speedLevel: Int = 1,
    val speedProgress: Float = 0.2f,
    val speedMultiplierText: String = "1.0x",
    val isAtMaxSpeed: Boolean = false,
    val items: List<ActiveFoodItem> = emptyList(),
    val isPaused: Boolean = false,
    val isGameOver: Boolean = false,
    val showSpeedUpBanner: Boolean = false,
    val showPlusOnePopup: Boolean = false,
    @param:StringRes val failReasonTemplateResId: Int = R.string.fail_missed_item,
    @param:StringRes val failFoodNameResId: Int = R.string.food_burger
)

/**
 * Role 1: Core Game Engine driven by frame Delta Time (`dt`) (Rubric 1.4, 2.1–2.4, 5.3).
 */
class GameEngine(
    val speedController: SpeedController = SpeedController(),
    private val onCorrectMatch: () -> Unit = {},
    private val onSpeedLevelUp: (Int) -> Unit = {},
    private val onGameOver: (score: Int, failTemplateResId: Int, foodNameResId: Int) -> Unit = { _, _, _ -> }
) {
    companion object {
        const val MAX_FRAME_DT_SECONDS = 0.05f
        private const val MAX_ITEMS_ON_BELT = 3
        private const val BANNER_DURATION_SEC = 1.25f
        private const val PLUS_ONE_DURATION_SEC = 0.55f
    }

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private var nextItemId = 1L
    private var spawnTimerSec = 0f
    private var speedBannerTimerSec = 0f
    private var plusOneTimerSec = 0f

    init {
        resetGame()
    }

    fun resetGame() {
        nextItemId = 1L
        spawnTimerSec = 0f
        speedBannerTimerSec = 0f
        plusOneTimerSec = 0f
        val firstItem = ActiveFoodItem(
            id = nextItemId++,
            type = speedController.nextFoodType(0),
            yProgress = 0.12f
        )
        _uiState.value = GameUiState(
            score = 0,
            speedLevel = speedController.getSpeedLevel(0),
            speedProgress = speedController.getSpeedProgressFraction(0),
            speedMultiplierText = speedController.getSpeedMultiplierText(0),
            isAtMaxSpeed = speedController.isAtMaxSpeed(0),
            items = listOf(firstItem),
            isPaused = false,
            isGameOver = false
        )
    }

    fun pauseGame() {
        _uiState.update { state ->
            if (state.isGameOver) state else state.copy(isPaused = true)
        }
    }

    fun resumeGame() {
        _uiState.update { state ->
            if (state.isGameOver) state else state.copy(isPaused = false)
        }
    }

    fun update(rawDtSeconds: Float) {
        val current = _uiState.value
        if (current.isPaused || current.isGameOver) return

        val dt = rawDtSeconds.coerceIn(0f, MAX_FRAME_DT_SECONDS)
        if (dt <= 0f) return

        val speed = speedController.calculateSpeed(current.score)

        if (speedBannerTimerSec > 0f) {
            speedBannerTimerSec = (speedBannerTimerSec - dt).coerceAtLeast(0f)
        }
        if (plusOneTimerSec > 0f) {
            plusOneTimerSec = (plusOneTimerSec - dt).coerceAtLeast(0f)
        }

        val updatedItems = current.items.map { item ->
            item.copy(yProgress = item.yProgress + speed * dt)
        }.toMutableList()

        val missedItem = updatedItems.firstOrNull { it.yProgress >= 1.0f }
        if (missedItem != null) {
            triggerSuddenDeath(
                failTemplateResId = R.string.fail_missed_item,
                foodNameResId = missedItem.type.nameResId
            )
            return
        }

        spawnTimerSec += dt
        val spawnInterval = speedController.getSpawnIntervalSeconds(current.score)
        val topMostY = updatedItems.minOfOrNull { it.yProgress } ?: 1f
        if (updatedItems.isEmpty() ||
            (spawnTimerSec >= spawnInterval && updatedItems.size < MAX_ITEMS_ON_BELT && topMostY > 0.26f)
        ) {
            spawnTimerSec = 0f
            updatedItems.add(
                ActiveFoodItem(
                    id = nextItemId++,
                    type = speedController.nextFoodType(current.score),
                    yProgress = 0.06f
                )
            )
        }

        _uiState.update { state ->
            state.copy(
                items = updatedItems,
                showSpeedUpBanner = speedBannerTimerSec > 0f,
                showPlusOnePopup = plusOneTimerSec > 0f
            )
        }
    }

    fun submitItemToTarget(itemId: Long? = null, target: TargetType) {
        val current = _uiState.value
        if (current.isPaused || current.isGameOver || current.items.isEmpty()) return

        val targetItem = if (itemId != null) {
            current.items.find { it.id == itemId }
        } else {
            current.items.maxByOrNull { it.yProgress }
        } ?: return

        val expectedTarget = targetItem.type.category.expectedTarget
        if (expectedTarget == target) {
            val prevScore = current.score
            val newScore = prevScore + 1
            val leveledUp = speedController.didLevelUp(prevScore, newScore)
            val newLevel = speedController.getSpeedLevel(newScore)

            plusOneTimerSec = PLUS_ONE_DURATION_SEC
            if (leveledUp) {
                speedBannerTimerSec = BANNER_DURATION_SEC
            }

            val remaining = current.items.filterNot { it.id == targetItem.id }.toMutableList()
            if (remaining.isEmpty()) {
                spawnTimerSec = 0f
                remaining.add(
                    ActiveFoodItem(
                        id = nextItemId++,
                        type = speedController.nextFoodType(newScore),
                        yProgress = 0.08f
                    )
                )
            }

            _uiState.update { state ->
                state.copy(
                    score = newScore,
                    speedLevel = newLevel,
                    speedProgress = speedController.getSpeedProgressFraction(newScore),
                    speedMultiplierText = speedController.getSpeedMultiplierText(newScore),
                    isAtMaxSpeed = speedController.isAtMaxSpeed(newScore),
                    items = remaining,
                    showSpeedUpBanner = speedBannerTimerSec > 0f,
                    showPlusOnePopup = true
                )
            }

            onCorrectMatch()
            if (leveledUp) {
                onSpeedLevelUp(newLevel)
            }
        } else {
            val failReasonResId = when {
                targetItem.type.category == FoodCategory.HAZARD && target != TargetType.TRASH ->
                    R.string.fail_fed_hazard_to_person
                target == TargetType.TRASH && targetItem.type.category != FoodCategory.HAZARD ->
                    R.string.fail_trashed_good_food
                target == TargetType.SKINNY && targetItem.type.category == FoodCategory.CLEAN_FOOD ->
                    R.string.fail_fed_clean_to_skinny
                else ->
                    R.string.fail_fed_high_cal_to_chubby
            }
            triggerSuddenDeath(
                failTemplateResId = failReasonResId,
                foodNameResId = targetItem.type.nameResId
            )
        }
    }

    private fun triggerSuddenDeath(
        @StringRes failTemplateResId: Int,
        @StringRes foodNameResId: Int
    ) {
        val finalScore = _uiState.value.score
        _uiState.update { state ->
            state.copy(
                isGameOver = true,
                isPaused = true,
                failReasonTemplateResId = failTemplateResId,
                failFoodNameResId = foodNameResId
            )
        }
        onGameOver(finalScore, failTemplateResId, foodNameResId)
    }
}
