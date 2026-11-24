package com.cs364.foodrush.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cs364.foodrush.R
import com.cs364.foodrush.game.GameEngine
import com.cs364.foodrush.game.GameUiState
import com.cs364.foodrush.game.TargetType
import com.cs364.foodrush.ui.components.ArcadeActionBar
import com.cs364.foodrush.ui.components.CharacterTargetZone
import com.cs364.foodrush.ui.components.ConveyorBeltLane
import kotlinx.coroutines.isActive
import kotlin.math.ceil
import kotlin.math.roundToInt

/**
 * Role 2: Main 16-Bit Pixel Art Arcade Gameplay Screen (Issue #4).
 *
 * Drives [GameEngine] via frame Delta Time (`dt`), renders the vertical conveyor belt,
 * left Skinny / right Chubby / bottom-center Trash targets, drag-and-tap controls,
 * `+1` popup, `SPEED UP!` banner, and pre-Game-Over screen shake.
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
    val latestNavigateToGameOver by rememberUpdatedState(onNavigateToGameOver)
    val latestCorrectFeedback by rememberUpdatedState(onCorrectMatchFeedback)
    val latestSpeedUpFeedback by rememberUpdatedState(onSpeedLevelUpFeedback)
    val latestGameOverFeedback by rememberUpdatedState(onGameOverFeedback)
    val latestPauseAudio by rememberUpdatedState(onPauseAudio)

    val engine = remember(gameEngine) {
        gameEngine ?: GameEngine(
            onCorrectMatch = { latestCorrectFeedback() },
            onSpeedLevelUp = { newLevel -> latestSpeedUpFeedback(newLevel) }
        )
    }

    val uiState by engine.uiState.collectAsStateWithLifecycle()
    var highlightedTarget by remember { mutableStateOf<TargetType?>(null) }
    var beltScrollPhase by remember { mutableFloatStateOf(0f) }

    val shakeOffsetX = remember { Animatable(0f) }
    var gameOverNavigated by remember { mutableStateOf(false) }

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

    // Sudden Death screen shake FX before navigating to GameOverScreen.
    LaunchedEffect(uiState.isGameOver) {
        if (uiState.isGameOver && !gameOverNavigated) {
            gameOverNavigated = true
            highlightedTarget = null
            latestGameOverFeedback()
            val shakeSequence = listOf(14f, -14f, 10f, -10f, 6f, -6f, 0f)
            for (targetX in shakeSequence) {
                shakeOffsetX.animateTo(
                    targetValue = targetX,
                    animationSpec = tween(durationMillis = 52)
                )
            }
            latestNavigateToGameOver(
                uiState.score,
                uiState.failReasonTemplateResId,
                uiState.failFoodNameResId
            )
        } else if (!uiState.isGameOver) {
            gameOverNavigated = false
            shakeOffsetX.snapTo(0f)
        }
    }

    GameplayBoardContent(
        uiState = uiState,
        beltScrollPhase = beltScrollPhase,
        highlightedTarget = highlightedTarget,
        shakeOffsetDp = shakeOffsetX.value,
        onHighlightTargetChange = { highlightedTarget = it },
        onSubmitItem = { itemId, target ->
            engine.submitItemToTarget(itemId = itemId, target = target)
        },
        onPauseClick = {
            if (!uiState.isGameOver) {
                engine.pauseGame()
                latestPauseAudio()
            }
        },
        modifier = modifier
    )
}

@Composable
private fun GameplayBoardContent(
    uiState: GameUiState,
    beltScrollPhase: Float,
    highlightedTarget: TargetType?,
    shakeOffsetDp: Float,
    onHighlightTargetChange: (TargetType?) -> Unit,
    onSubmitItem: (itemId: Long?, target: TargetType) -> Unit,
    onPauseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    val dragThresholdPx = with(LocalDensity.current) { 42.dp.toPx() }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(colorScheme.surface)
    ) {
        val compactMode = maxHeight < 680.dp || maxWidth < 360.dp

        // 16-bit checkered kitchen floor background using MaterialTheme colors only
        Canvas(modifier = Modifier.fillMaxSize()) {
            val tilePx = 36.dp.toPx()
            val cols = ceil(size.width / tilePx).toInt()
            val rows = ceil(size.height / tilePx).toInt()
            val altTileColor = colorScheme.surfaceVariant.copy(alpha = 0.45f)
            for (r in 0..rows) {
                for (c in 0..cols) {
                    if ((r + c) % 2 == 1) {
                        drawRect(
                            color = altTileColor,
                            topLeft = Offset(c * tilePx, r * tilePx),
                            size = Size(tilePx, tilePx)
                        )
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .offset(x = shakeOffsetDp.dp)
                .padding(horizontal = if (compactMode) 8.dp else 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(if (compactMode) 6.dp else 10.dp)
        ) {
            GameplayTopHud(
                uiState = uiState,
                compactMode = compactMode,
                onPauseClick = onPauseClick
            )

            // Center interactive arena: Skinny (Left) | Conveyor + Trash (Center) | Chubby (Right)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(if (compactMode) 6.dp else 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CharacterTargetZone(
                    targetType = TargetType.SKINNY,
                    isHighlighted = highlightedTarget == TargetType.SKINNY,
                    compactMode = compactMode,
                    onSelectTarget = { onSubmitItem(null, it) },
                    modifier = Modifier.weight(0.28f)
                )

                Column(
                    modifier = Modifier
                        .weight(0.44f)
                        .fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(if (compactMode) 4.dp else 6.dp)
                ) {
                    ConveyorBeltLane(
                        items = uiState.items,
                        beltScrollPhase = beltScrollPhase,
                        compactMode = compactMode,
                        dragThresholdPx = dragThresholdPx,
                        onHighlightTargetChange = onHighlightTargetChange,
                        onSubmitItem = onSubmitItem,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    )

                    CharacterTargetZone(
                        targetType = TargetType.TRASH,
                        isHighlighted = highlightedTarget == TargetType.TRASH,
                        compactMode = compactMode,
                        onSelectTarget = { onSubmitItem(null, it) },
                        modifier = Modifier.fillMaxWidth(0.92f)
                    )
                }

                CharacterTargetZone(
                    targetType = TargetType.CHUBBY,
                    isHighlighted = highlightedTarget == TargetType.CHUBBY,
                    compactMode = compactMode,
                    onSelectTarget = { onSubmitItem(null, it) },
                    modifier = Modifier.weight(0.28f)
                )
            }

            // Control instruction hint bar
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(4.dp),
                color = colorScheme.surfaceVariant.copy(alpha = 0.85f),
                border = BorderStroke(1.dp, colorScheme.outline.copy(alpha = 0.6f))
            ) {
                Text(
                    text = stringResource(R.string.gameplay_hint_controls),
                    style = MaterialTheme.typography.labelSmall,
                    color = colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            // Bottom 3-way arcade button bar
            ArcadeActionBar(
                compactMode = compactMode,
                highlightedTarget = highlightedTarget,
                onActionClick = { target -> onSubmitItem(null, target) }
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
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left: Score Card + "+1" Popup
        ScoreHudCard(
            score = uiState.score,
            showPlusOnePopup = uiState.showPlusOnePopup,
            compactMode = compactMode
        )

        // Center: Speed Up Banner & 10-segment Speed Gauge
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
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

            Surface(
                shape = RoundedCornerShape(4.dp),
                color = if (uiState.showSpeedUpBanner) {
                    colorScheme.tertiaryContainer
                } else {
                    colorScheme.surfaceVariant
                },
                border = BorderStroke(
                    width = if (uiState.showSpeedUpBanner) 2.dp else 1.dp,
                    color = if (uiState.showSpeedUpBanner) colorScheme.tertiary else colorScheme.outline
                )
            ) {
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.Monospace,
                    color = if (uiState.showSpeedUpBanner) {
                        colorScheme.onTertiaryContainer
                    } else {
                        colorScheme.onSurfaceVariant
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }

            SegmentedSpeedBar(
                progressFraction = uiState.speedProgress,
                isHighlighted = uiState.showSpeedUpBanner || uiState.isAtMaxSpeed,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Right: Arcade Pause Button (min 48.dp touch target)
        Button(
            onClick = onPauseClick,
            modifier = Modifier
                .size(48.dp)
                .semantics { contentDescription = pauseCd },
            shape = RoundedCornerShape(6.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colorScheme.secondaryContainer,
                contentColor = colorScheme.onSecondaryContainer
            ),
            border = BorderStroke(2.dp, colorScheme.outline),
            contentPadding = PaddingValues(0.dp)
        ) {
            Text(
                text = stringResource(R.string.gameplay_pause_symbol),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.Monospace,
                fontSize = 18.sp
            )
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
        Surface(
            shape = RoundedCornerShape(6.dp),
            color = colorScheme.surfaceVariant,
            border = BorderStroke(2.dp, colorScheme.outline),
            shadowElevation = 2.dp
        ) {
            Column(
                modifier = Modifier.padding(
                    horizontal = if (compactMode) 10.dp else 12.dp,
                    vertical = 6.dp
                ),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.gameplay_score_label),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = colorScheme.onSurfaceVariant,
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
                    color = colorScheme.onSurface,
                    maxLines = 1
                )
            }
        }

        AnimatedVisibility(
            visible = showPlusOnePopup,
            enter = fadeIn(tween(100)) + scaleIn(tween(120)),
            exit = fadeOut(tween(180)) + scaleOut(tween(180)),
            modifier = Modifier.offset(x = 6.dp, y = (-6).dp)
        ) {
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = colorScheme.primary,
                border = BorderStroke(1.dp, colorScheme.outline)
            ) {
                Text(
                    text = stringResource(R.string.gameplay_plus_one),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.Monospace,
                    color = colorScheme.onPrimary,
                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
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
    val totalSegments = 10
    val filledSegments = (progressFraction.coerceIn(0f, 1f) * totalSegments)
        .roundToInt()
        .coerceIn(1, totalSegments)

    Surface(
        modifier = modifier.height(18.dp),
        shape = RoundedCornerShape(4.dp),
        color = colorScheme.surface,
        border = BorderStroke(2.dp, colorScheme.outline)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(2.dp),
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
                                else -> colorScheme.surfaceVariant
                            }
                        )
                )
            }
        }
    }
}

@Preview(name = "Compact Day (360x640)", widthDp = 360, heightDp = 640, showBackground = true)
@Composable
private fun GameplayCompactDayPreview() {
    MaterialTheme(colorScheme = lightColorScheme()) {
        val previewEngine = remember { GameEngine() }
        val state by previewEngine.uiState.collectAsStateWithLifecycle()
        GameplayBoardContent(
            uiState = state.copy(score = 12, speedLevel = 3, speedProgress = 0.55f, showSpeedUpBanner = true, showPlusOnePopup = true),
            beltScrollPhase = 0.25f,
            highlightedTarget = TargetType.SKINNY,
            shakeOffsetDp = 0f,
            onHighlightTargetChange = {},
            onSubmitItem = { _, _ -> },
            onPauseClick = {}
        )
    }
}

@Preview(name = "Large Night (412x915)", widthDp = 412, heightDp = 915, showBackground = true)
@Composable
private fun GameplayLargeNightPreview() {
    MaterialTheme(colorScheme = darkColorScheme()) {
        val previewEngine = remember { GameEngine() }
        val state by previewEngine.uiState.collectAsStateWithLifecycle()
        GameplayBoardContent(
            uiState = state.copy(score = 35, speedLevel = 8, speedProgress = 1.0f, isAtMaxSpeed = true),
            beltScrollPhase = 0.6f,
            highlightedTarget = null,
            shakeOffsetDp = 0f,
            onHighlightTargetChange = {},
            onSubmitItem = { _, _ -> },
            onPauseClick = {}
        )
    }
}
