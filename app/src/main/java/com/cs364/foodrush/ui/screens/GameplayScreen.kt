package com.cs364.foodrush.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cs364.foodrush.R
import com.cs364.foodrush.game.ActiveFoodItem
import com.cs364.foodrush.game.FoodType
import com.cs364.foodrush.game.GameEngine
import com.cs364.foodrush.game.GameUiState
import com.cs364.foodrush.game.TargetType
import com.cs364.foodrush.ui.components.CharacterPose
import com.cs364.foodrush.ui.components.CharacterTargetZone
import com.cs364.foodrush.ui.components.ConveyorBeltLane
import com.cs364.foodrush.ui.components.PauseExitDialog
import com.cs364.foodrush.ui.components.PixelArcadeBox
import com.cs364.foodrush.ui.components.resolveDragTarget
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.math.ceil
import kotlin.math.roundToInt

/**
 * Role 2: Main 16-Bit Pixel Art Arcade Gameplay Screen (Issue #4).
 *
 * Matches `docs/ui/ui_pixel_gameplay.png` proportions: full-height 3D vertical conveyor belt,
 * full-size animated Skinny (left) & Chubby (right) standing on the 8-column checkered diner floor,
 * Trash Bin at bottom-center, stepped pixel HUD, and pre-Game-Over screen shake.
 */
@Composable
fun GameplayScreen(
    modifier: Modifier = Modifier,
    onNavigateBackToMenu: () -> Unit = {},
    onNavigateToGameOver: (score: Int, failReasonTemplateResId: Int, failFoodNameResId: Int) -> Unit = { _, _, _ -> },
    onCorrectMatchFeedback: () -> Unit = {},
    onSpeedLevelUpFeedback: (newLevel: Int) -> Unit = {},
    onGameOverFeedback: () -> Unit = {},
    onPauseAudio: () -> Unit = {},
    onResumeAudio: () -> Unit = {},
    gameEngine: GameEngine? = null
) {
    val latestNavigateBackToMenu by rememberUpdatedState(onNavigateBackToMenu)
    val latestNavigateToGameOver by rememberUpdatedState(onNavigateToGameOver)
    val latestCorrectFeedback by rememberUpdatedState(onCorrectMatchFeedback)
    val latestSpeedUpFeedback by rememberUpdatedState(onSpeedLevelUpFeedback)
    val latestGameOverFeedback by rememberUpdatedState(onGameOverFeedback)
    val latestPauseAudio by rememberUpdatedState(onPauseAudio)
    val latestResumeAudio by rememberUpdatedState(onResumeAudio)

    val engine = remember(gameEngine) {
        gameEngine ?: GameEngine(
            onCorrectMatch = { latestCorrectFeedback() },
            onSpeedLevelUp = { newLevel -> latestSpeedUpFeedback(newLevel) }
        )
    }

    val uiState by engine.uiState.collectAsStateWithLifecycle()
    var highlightedTarget by remember { mutableStateOf<TargetType?>(null) }
    var beltScrollPhase by remember { mutableFloatStateOf(0f) }
    var showPauseDialog by rememberSaveable { mutableStateOf(false) }

    var skinnyFedTick by remember { mutableIntStateOf(0) }
    var chubbyFedTick by remember { mutableIntStateOf(0) }
    var trashFedTick by remember { mutableIntStateOf(0) }

    var skinnyIsYummy by remember { mutableStateOf(false) }
    var chubbyIsYummy by remember { mutableStateOf(false) }
    var lastWrongTarget by remember { mutableStateOf<TargetType?>(null) }

    val shakeOffsetX = remember { Animatable(0f) }
    var gameOverNavigated by remember { mutableStateOf(false) }

    val triggerPause: () -> Unit = {
        if (!engine.uiState.value.isGameOver) {
            highlightedTarget = null
            engine.pauseGame()
            latestPauseAudio()
            showPauseDialog = true
        }
    }

    // Back Navigation Guard (Rubric 4.2): Back pauses & asks confirmation instead of dropping out
    BackHandler(enabled = !uiState.isGameOver) {
        triggerPause()
    }

    // Activity Lifecycle Safety (Rubric 4.4): ON_PAUSE / ON_STOP immediately pause & preserve state
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, engine) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE,
                Lifecycle.Event.ON_STOP -> triggerPause()
                Lifecycle.Event.ON_RESUME -> {
                    if (engine.uiState.value.isPaused && !engine.uiState.value.isGameOver) {
                        showPauseDialog = true
                    }
                }
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Hold happy belly-rub pose briefly after each correct feed
    LaunchedEffect(skinnyFedTick) {
        if (skinnyFedTick > 0) {
            skinnyIsYummy = true
            delay(850L)
            skinnyIsYummy = false
        }
    }
    LaunchedEffect(chubbyFedTick) {
        if (chubbyFedTick > 0) {
            chubbyIsYummy = true
            delay(850L)
            chubbyIsYummy = false
        }
    }

    // Delta Time (dt) loop: resets lastFrameNanos whenever resumed so dt never spikes.
    LaunchedEffect(engine, uiState.isPaused, uiState.isGameOver) {
        if (!uiState.isPaused && !uiState.isGameOver) {
            var lastFrameNanos = 0L
            while (isActive) {
                withFrameNanos { frameNanos ->
                    if (lastFrameNanos != 0L) {
                        val rawDt = (frameNanos - lastFrameNanos) / 1_000_000_000f
                        engine.update(rawDt)
                        val speed = engine.speedController.calculateSpeed(engine.uiState.value.score)
                        val clampedDt = rawDt.coerceIn(0f, GameEngine.MAX_FRAME_DT_SECONDS)
                        beltScrollPhase = (beltScrollPhase + speed * clampedDt) % 1f
                    }
                    lastFrameNanos = frameNanos
                }
            }
        }
    }

    // Sudden Death screen shake + KO / crying reaction beat before navigating to GameOverScreen.
    LaunchedEffect(uiState.isGameOver) {
        if (uiState.isGameOver && !gameOverNavigated) {
            gameOverNavigated = true
            highlightedTarget = null
            skinnyIsYummy = false
            chubbyIsYummy = false
            latestGameOverFeedback()
            val shakeSequence = listOf(14f, -14f, 10f, -10f, 6f, -6f, 0f)
            for (targetX in shakeSequence) {
                shakeOffsetX.animateTo(
                    targetValue = targetX,
                    animationSpec = tween(durationMillis = 52)
                )
            }
            delay(950L)
            latestNavigateToGameOver(
                uiState.score,
                uiState.failReasonTemplateResId,
                uiState.failFoodNameResId
            )
        } else if (!uiState.isGameOver) {
            lastWrongTarget = null
            skinnyIsYummy = false
            chubbyIsYummy = false
            gameOverNavigated = false
            shakeOffsetX.snapTo(0f)
        }
    }

    val (skinnyPose, chubbyPose) = if (uiState.isGameOver) {
        when (lastWrongTarget) {
            TargetType.SKINNY -> CharacterPose.DEAD to CharacterPose.SAD
            TargetType.CHUBBY -> CharacterPose.SAD to CharacterPose.DEAD
            else -> when (uiState.failFoodNameResId) {
                R.string.food_burger -> CharacterPose.DEAD to CharacterPose.SAD
                R.string.food_salad -> CharacterPose.SAD to CharacterPose.DEAD
                else -> CharacterPose.SAD to CharacterPose.SAD
            }
        }
    } else {
        val sPose = if (skinnyIsYummy) CharacterPose.YUMMY else CharacterPose.IDLE
        val cPose = if (chubbyIsYummy) CharacterPose.YUMMY else CharacterPose.IDLE
        sPose to cPose
    }

    GameplayBoardContent(
        uiState = uiState,
        beltScrollPhase = beltScrollPhase,
        highlightedTarget = highlightedTarget,
        skinnyPose = skinnyPose,
        chubbyPose = chubbyPose,
        skinnyFedTick = skinnyFedTick,
        chubbyFedTick = chubbyFedTick,
        trashFedTick = trashFedTick,
        shakeOffsetDp = shakeOffsetX.value,
        onHighlightTargetChange = { highlightedTarget = it },
        onSubmitItem = { itemId, target ->
            val prevScore = engine.uiState.value.score
            engine.submitItemToTarget(itemId = itemId, target = target)
            val afterState = engine.uiState.value
            if (afterState.score > prevScore) {
                when (target) {
                    TargetType.SKINNY -> skinnyFedTick++
                    TargetType.CHUBBY -> chubbyFedTick++
                    TargetType.TRASH -> trashFedTick++
                }
            } else if (afterState.isGameOver) {
                lastWrongTarget = target
            }
        },
        onPauseClick = triggerPause,
        modifier = modifier
    )

    if ((showPauseDialog || uiState.isPaused) && !uiState.isGameOver) {
        val speedLabel = if (uiState.isAtMaxSpeed) {
            stringResource(R.string.gameplay_speed_max_format, uiState.speedMultiplierText)
        } else {
            stringResource(
                R.string.gameplay_speed_level_format,
                uiState.speedLevel,
                uiState.speedMultiplierText
            )
        }
        PauseExitDialog(
            score = uiState.score,
            speedLabel = speedLabel,
            onResume = {
                showPauseDialog = false
                engine.resumeGame()
                latestResumeAudio()
            },
            onConfirmExit = {
                showPauseDialog = false
                latestNavigateBackToMenu()
            }
        )
    }
}

@Composable
private fun GameplayBoardContent(
    uiState: GameUiState,
    beltScrollPhase: Float,
    highlightedTarget: TargetType?,
    skinnyPose: CharacterPose,
    chubbyPose: CharacterPose,
    skinnyFedTick: Int,
    chubbyFedTick: Int,
    trashFedTick: Int,
    shakeOffsetDp: Float,
    onHighlightTargetChange: (TargetType?) -> Unit,
    onSubmitItem: (itemId: Long?, target: TargetType) -> Unit,
    onPauseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    val dragThresholdPx = with(LocalDensity.current) { 42.dp.toPx() }
    val latestThreshold by rememberUpdatedState(dragThresholdPx)
    val latestHighlightChange by rememberUpdatedState(onHighlightTargetChange)
    val latestSubmitItem by rememberUpdatedState(onSubmitItem)

    val hasApproachingHazard = remember(uiState.items) {
        uiState.items.any { it.type == FoodType.FOOD_SCRAPS && it.yProgress >= 0.45f }
    }

    var arenaSwipeOffset by remember { mutableStateOf(Offset.Zero) }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(colorScheme.surface)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { arenaSwipeOffset = Offset.Zero },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        arenaSwipeOffset += dragAmount
                        latestHighlightChange(
                            resolveDragTarget(arenaSwipeOffset.x, arenaSwipeOffset.y, latestThreshold)
                        )
                    },
                    onDragEnd = {
                        val resolved = resolveDragTarget(
                            arenaSwipeOffset.x,
                            arenaSwipeOffset.y,
                            latestThreshold
                        )
                        arenaSwipeOffset = Offset.Zero
                        latestHighlightChange(null)
                        if (resolved != null) {
                            latestSubmitItem(null, resolved)
                        }
                    },
                    onDragCancel = {
                        arenaSwipeOffset = Offset.Zero
                        latestHighlightChange(null)
                    }
                )
            }
    ) {
        val compactMode = maxHeight < 680.dp || maxWidth < 360.dp

        // Layer 1: Authentic 16-bit Diner Kitchen Wall, Cabinets & 8-Column Checkered Floor
        Canvas(modifier = Modifier.fillMaxSize()) {
            val wallHeight = size.height * 0.055f
            val cabinetBottom = size.height * 0.165f
            val counterThickness = 4.dp.toPx()

            // Upper diner wall strip
            drawRect(
                color = colorScheme.tertiaryContainer.copy(alpha = 0.72f),
                topLeft = Offset.Zero,
                size = Size(size.width, wallHeight)
            )

            // Kitchen upper cabinet panels behind HUD
            drawRect(
                color = colorScheme.secondaryContainer,
                topLeft = Offset(0f, wallHeight),
                size = Size(size.width, cabinetBottom - wallHeight)
            )
            val panelWidth = size.width / 5f
            for (i in 1..4) {
                val x = panelWidth * i
                drawRect(
                    color = colorScheme.onSurface.copy(alpha = 0.25f),
                    topLeft = Offset(x, wallHeight + 2.dp.toPx()),
                    size = Size(2.dp.toPx(), cabinetBottom - wallHeight - 4.dp.toPx())
                )
            }

            // Countertop ledge + floor shadow
            drawRect(
                color = colorScheme.onSurface,
                topLeft = Offset(0f, cabinetBottom),
                size = Size(size.width, counterThickness)
            )
            drawRect(
                color = colorScheme.onSurface.copy(alpha = 0.16f),
                topLeft = Offset(0f, cabinetBottom + counterThickness),
                size = Size(size.width, 6.dp.toPx())
            )

            // 8-column checkered diner floor
            val floorStart = cabinetBottom + counterThickness
            val tilePx = size.width / 8f
            val rows = ceil((size.height - floorStart) / tilePx).toInt()
            val altTileColor = colorScheme.surfaceVariant.copy(alpha = 0.65f)
            for (r in 0..rows) {
                val top = floorStart + r * tilePx
                val tileH = tilePx.coerceAtMost((size.height - top).coerceAtLeast(0f))
                for (c in 0 until 8) {
                    if ((r + c) % 2 == 1) {
                        drawRect(
                            color = altTileColor,
                            topLeft = Offset(c * tilePx, top),
                            size = Size(tilePx, tileH)
                        )
                    }
                }
            }
        }

        // Layer 2 & 3: Safe-area interactive arena & floating Top HUD
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .offset(x = shakeOffsetDp.dp)
        ) {
            val arenaW = maxWidth
            val arenaH = maxHeight
            val beltWidth = (arenaW * 0.31f).coerceIn(104.dp, 146.dp)
            val sideSpace = ((arenaW - beltWidth) / 2f).coerceAtLeast(78.dp)

            val trashSize = (arenaH * 0.185f).coerceIn(84.dp, 128.dp)
            val beltHeight = (arenaH - trashSize * 0.78f).coerceAtLeast(240.dp)
            val hudTopInset = if (compactMode) 60.dp else 72.dp

            val skinnyHeight = (arenaH * 0.44f).coerceIn(176.dp, 286.dp)
            val skinnyWidth = (sideSpace * 0.96f).coerceIn(88.dp, 152.dp)
            val chubbyHeight = (arenaH * 0.35f).coerceIn(146.dp, 232.dp)
            val chubbyWidth = (sideSpace * 1.00f).coerceIn(92.dp, 166.dp)
            val feetBottomOffset = arenaH * 0.25f

            val actualSkinnyW = when (skinnyPose) {
                CharacterPose.DEAD -> (sideSpace * 1.24f).coerceIn(112.dp, 198.dp)
                else -> skinnyWidth
            }
            val actualSkinnyH = when (skinnyPose) {
                CharacterPose.DEAD -> (arenaH * 0.23f).coerceIn(92.dp, 152.dp)
                CharacterPose.SAD -> skinnyHeight * 0.78f
                else -> skinnyHeight
            }
            val skinnyStartPad = when (skinnyPose) {
                CharacterPose.DEAD -> 4.dp
                else -> (sideSpace - actualSkinnyW).coerceAtLeast(2.dp)
            }

            val actualChubbyW = when (chubbyPose) {
                CharacterPose.DEAD -> (sideSpace * 1.24f).coerceIn(112.dp, 198.dp)
                else -> chubbyWidth
            }
            val actualChubbyH = when (chubbyPose) {
                CharacterPose.DEAD -> (arenaH * 0.23f).coerceIn(92.dp, 152.dp)
                CharacterPose.SAD -> chubbyHeight * 0.80f
                else -> chubbyHeight
            }
            val chubbyEndPad = when (chubbyPose) {
                CharacterPose.DEAD -> 4.dp
                else -> (sideSpace - actualChubbyW).coerceAtLeast(2.dp)
            }

            // Left Character: Skinny standing / belly-rub / collapsed dead / kneeling crying
            CharacterTargetZone(
                targetType = TargetType.SKINNY,
                isHighlighted = highlightedTarget == TargetType.SKINNY,
                pose = skinnyPose,
                reactionTick = skinnyFedTick,
                onSelectTarget = { onSubmitItem(null, it) },
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(
                        start = skinnyStartPad,
                        bottom = feetBottomOffset
                    )
                    .size(width = actualSkinnyW, height = actualSkinnyH)
            )

            // Right Character: Chubby standing / belly-rub / collapsed dead / kneeling crying
            CharacterTargetZone(
                targetType = TargetType.CHUBBY,
                isHighlighted = highlightedTarget == TargetType.CHUBBY,
                pose = chubbyPose,
                reactionTick = chubbyFedTick,
                onSelectTarget = { onSubmitItem(null, it) },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(
                        end = chubbyEndPad,
                        bottom = feetBottomOffset
                    )
                    .size(width = actualChubbyW, height = actualChubbyH)
            )

            // Bottom Center: Trash Bin aligned under the bottom exit of the belt
            CharacterTargetZone(
                targetType = TargetType.TRASH,
                isHighlighted = highlightedTarget == TargetType.TRASH,
                reactionTick = trashFedTick,
                hasApproachingHazard = hasApproachingHazard,
                onSelectTarget = { onSubmitItem(null, it) },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset(x = trashSize * 0.11f, y = (-6).dp)
                    .size(trashSize)
            )

            // Center Full-Height Vertical Conveyor Belt & Draggable Food Plates (drawn on top when dragged)
            ConveyorBeltLane(
                items = uiState.items,
                beltScrollPhase = beltScrollPhase,
                compactMode = compactMode,
                dragThresholdPx = dragThresholdPx,
                topPlateInset = hudTopInset,
                onHighlightTargetChange = onHighlightTargetChange,
                onSubmitItem = onSubmitItem,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .width(beltWidth)
                    .height(beltHeight)
            )

            // Top HUD floating over the upper kitchen wall & top of conveyor belt
            GameplayTopHud(
                uiState = uiState,
                compactMode = compactMode,
                onPauseClick = onPauseClick,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .padding(horizontal = if (compactMode) 8.dp else 12.dp, vertical = 8.dp)
            )
        }
    }
}

@Composable
private fun GameplayTopHud(
    uiState: GameUiState,
    compactMode: Boolean,
    onPauseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    val pauseCd = stringResource(R.string.gameplay_pause_cd)

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Left: Dark Stepped Pixel Score Plaque + "+1" Popup
        ScoreHudCard(
            score = uiState.score,
            showPlusOnePopup = uiState.showPlusOnePopup,
            compactMode = compactMode
        )

        // Center: Speed Up Banner + 12-segment Golden Arcade Gauge
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            val statusText = when {
                uiState.showSpeedUpBanner -> stringResource(R.string.gameplay_speed_up_banner)
                uiState.isAtMaxSpeed -> stringResource(
                    R.string.gameplay_speed_max_format,
                    uiState.speedMultiplierText
                )
                else -> stringResource(
                    R.string.gameplay_speed_level_format,
                    uiState.speedLevel,
                    uiState.speedMultiplierText
                )
            }

            PixelArcadeBox(
                fillColor = if (uiState.showSpeedUpBanner) {
                    colorScheme.tertiary
                } else {
                    colorScheme.inverseSurface
                },
                borderColor = colorScheme.onSurface,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.Monospace,
                    fontSize = if (compactMode) 11.sp else 13.sp,
                    color = if (uiState.showSpeedUpBanner) {
                        colorScheme.onTertiary
                    } else {
                        colorScheme.inverseOnSurface
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            SegmentedSpeedBar(
                progressFraction = uiState.speedProgress,
                isHighlighted = uiState.showSpeedUpBanner || uiState.isAtMaxSpeed,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Right: Stepped 16-bit Pixel Pause Keycap (48.dp minimum touch target)
        PixelArcadeBox(
            modifier = Modifier
                .size(48.dp)
                .semantics { contentDescription = pauseCd }
                .clickable(role = Role.Button, onClick = onPauseClick),
            fillColor = colorScheme.inverseSurface,
            borderColor = colorScheme.onSurface,
            contentPadding = PaddingValues(11.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val barWidth = (size.width * 0.28f).coerceAtLeast(4.dp.toPx())
                drawRect(
                    color = colorScheme.inverseOnSurface,
                    topLeft = Offset(size.width * 0.14f, 0f),
                    size = Size(barWidth, size.height)
                )
                drawRect(
                    color = colorScheme.inverseOnSurface,
                    topLeft = Offset(size.width - size.width * 0.14f - barWidth, 0f),
                    size = Size(barWidth, size.height)
                )
            }
        }
    }
}

@Composable
private fun ScoreHudCard(
    score: Int,
    showPlusOnePopup: Boolean,
    compactMode: Boolean,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    Box(
        modifier = modifier,
        contentAlignment = Alignment.TopEnd
    ) {
        PixelArcadeBox(
            fillColor = colorScheme.inverseSurface,
            borderColor = colorScheme.onSurface,
            contentPadding = PaddingValues(
                horizontal = if (compactMode) 10.dp else 14.dp,
                vertical = if (compactMode) 4.dp else 6.dp
            )
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(R.string.gameplay_score_label),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    fontSize = if (compactMode) 10.sp else 11.sp,
                    color = colorScheme.inverseOnSurface.copy(alpha = 0.85f),
                    maxLines = 1
                )
                Text(
                    text = score.toString(),
                    style = if (compactMode) {
                        MaterialTheme.typography.titleMedium
                    } else {
                        MaterialTheme.typography.titleLarge
                    },
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.Monospace,
                    color = colorScheme.inverseOnSurface,
                    maxLines = 1
                )
            }
        }

        AnimatedVisibility(
            visible = showPlusOnePopup,
            enter = fadeIn(tween(100)) + scaleIn(tween(120)),
            exit = fadeOut(tween(180)) + scaleOut(tween(180)),
            modifier = Modifier.offset(x = 8.dp, y = (-6).dp)
        ) {
            PixelArcadeBox(
                fillColor = colorScheme.primary,
                borderColor = colorScheme.onSurface,
                cornerStep = 2.dp,
                contentPadding = PaddingValues(horizontal = 5.dp, vertical = 1.dp)
            ) {
                Text(
                    text = stringResource(R.string.gameplay_plus_one),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.Monospace,
                    color = colorScheme.onPrimary
                )
            }
        }
    }
}

@Composable
private fun SegmentedSpeedBar(
    progressFraction: Float,
    isHighlighted: Boolean,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    val totalSegments = 12
    val filledSegments = (progressFraction.coerceIn(0f, 1f) * totalSegments)
        .roundToInt()
        .coerceIn(1, totalSegments)

    PixelArcadeBox(
        modifier = modifier.height(24.dp),
        fillColor = colorScheme.inverseSurface,
        borderColor = colorScheme.onSurface,
        cornerStep = 3.dp,
        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 5.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            for (segmentIndex in 0 until totalSegments) {
                val active = segmentIndex < filledSegments
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(
                            color = when {
                                active && isHighlighted -> colorScheme.tertiary
                                active -> colorScheme.primary
                                else -> colorScheme.surfaceVariant.copy(alpha = 0.28f)
                            }
                        )
                ) {
                    if (active) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(3.dp)
                                .background(colorScheme.surface.copy(alpha = 0.40f))
                        )
                    }
                }
            }
        }
    }
}

@Preview(name = "Compact Day — Yummy Belly Rub (360x640)", widthDp = 360, heightDp = 640, showBackground = true)
@Composable
private fun GameplayCompactDayPreview() {
    MaterialTheme(colorScheme = lightColorScheme()) {
        val sampleItems = listOf(
            ActiveFoodItem(id = 1L, type = FoodType.BURGER, yProgress = 0.24f),
            ActiveFoodItem(id = 2L, type = FoodType.SALAD, yProgress = 0.53f),
            ActiveFoodItem(id = 3L, type = FoodType.FOOD_SCRAPS, yProgress = 0.81f)
        )
        GameplayBoardContent(
            uiState = GameUiState(
                score = 1240,
                speedLevel = 3,
                speedProgress = 0.58f,
                speedMultiplierText = "1.4x",
                items = sampleItems,
                showSpeedUpBanner = true,
                showPlusOnePopup = true
            ),
            beltScrollPhase = 0.30f,
            highlightedTarget = null,
            skinnyPose = CharacterPose.YUMMY,
            chubbyPose = CharacterPose.IDLE,
            skinnyFedTick = 0,
            chubbyFedTick = 0,
            trashFedTick = 0,
            shakeOffsetDp = 0f,
            onHighlightTargetChange = {},
            onSubmitItem = { _, _ -> },
            onPauseClick = {}
        )
    }
}

@Preview(name = "KO Scene — Skinny Dead & Chubby Sad (360x640)", widthDp = 360, heightDp = 640, showBackground = true)
@Composable
private fun GameplaySkinnyDeadChubbySadPreview() {
    MaterialTheme(colorScheme = lightColorScheme()) {
        val sampleItems = listOf(
            ActiveFoodItem(id = 1L, type = FoodType.BURGER, yProgress = 0.32f),
            ActiveFoodItem(id = 2L, type = FoodType.SALAD, yProgress = 0.68f)
        )
        GameplayBoardContent(
            uiState = GameUiState(
                score = 18,
                speedLevel = 4,
                speedProgress = 0.72f,
                speedMultiplierText = "1.7x",
                items = sampleItems,
                isGameOver = true
            ),
            beltScrollPhase = 0.40f,
            highlightedTarget = null,
            skinnyPose = CharacterPose.DEAD,
            chubbyPose = CharacterPose.SAD,
            skinnyFedTick = 0,
            chubbyFedTick = 0,
            trashFedTick = 0,
            shakeOffsetDp = 0f,
            onHighlightTargetChange = {},
            onSubmitItem = { _, _ -> },
            onPauseClick = {}
        )
    }
}

@Preview(name = "KO Scene — Chubby Dead & Skinny Sad (412x915)", widthDp = 412, heightDp = 915, showBackground = true)
@Composable
private fun GameplayLargeNightPreview() {
    MaterialTheme(colorScheme = darkColorScheme()) {
        val sampleItems = listOf(
            ActiveFoodItem(id = 1L, type = FoodType.BURGER, yProgress = 0.22f),
            ActiveFoodItem(id = 2L, type = FoodType.SALAD, yProgress = 0.52f),
            ActiveFoodItem(id = 3L, type = FoodType.FOOD_SCRAPS, yProgress = 0.80f)
        )
        GameplayBoardContent(
            uiState = GameUiState(
                score = 35,
                speedLevel = 7,
                speedProgress = 1.0f,
                speedMultiplierText = "2.4x",
                isAtMaxSpeed = true,
                items = sampleItems,
                isGameOver = true
            ),
            beltScrollPhase = 0.65f,
            highlightedTarget = null,
            skinnyPose = CharacterPose.SAD,
            chubbyPose = CharacterPose.DEAD,
            skinnyFedTick = 0,
            chubbyFedTick = 0,
            trashFedTick = 0,
            shakeOffsetDp = 0f,
            onHighlightTargetChange = {},
            onSubmitItem = { _, _ -> },
            onPauseClick = {}
        )
    }
}

@Preview(name = "LIVE PLAYABLE (Click Interactive Mode)", widthDp = 390, heightDp = 844, showBackground = true)
@Composable
private fun GameplayLiveInteractivePreview() {
    MaterialTheme(colorScheme = lightColorScheme()) {
        GameplayScreen()
    }
}
