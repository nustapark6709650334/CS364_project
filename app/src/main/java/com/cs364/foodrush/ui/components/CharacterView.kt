package com.cs364.foodrush.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.cs364.foodrush.R
import com.cs364.foodrush.game.ActiveFoodItem
import com.cs364.foodrush.game.TargetType
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.roundToInt

/**
 * Resolves a player's drag/swipe delta vector into a [TargetType], or `null` if the
 * gesture did not exceed [thresholdPx].
 *
 * The dead-zone protects against accidental micro-taps causing Sudden Death.
 */
fun resolveDragTarget(dragX: Float, dragY: Float, thresholdPx: Float): TargetType? {
    if (thresholdPx <= 0f || hypot(dragX, dragY) < thresholdPx) {
        return null
    }
    return when {
        dragY >= thresholdPx && dragY > abs(dragX) * 0.85f -> TargetType.TRASH
        dragX <= -thresholdPx -> TargetType.SKINNY
        dragX >= thresholdPx -> TargetType.CHUBBY
        else -> null
    }
}

/**
 * Renders a 16-bit pixel art sprite with [FilterQuality.None] so pixels remain razor-sharp.
 */
@Composable
fun PixelSpriteImage(
    @DrawableRes resId: Int,
    contentDescription: String?,
    modifier: Modifier = Modifier
) {
    val bitmap = ImageBitmap.imageResource(id = resId)
    Image(
        bitmap = bitmap,
        contentDescription = contentDescription,
        contentScale = ContentScale.Fit,
        filterQuality = FilterQuality.None,
        modifier = modifier
    )
}

/**
 * Interactive character / trash target zone on the gameplay board.
 * Supports both visual drag-hover highlight and direct tap-to-feed accessibility.
 */
@Composable
fun CharacterTargetZone(
    targetType: TargetType,
    isHighlighted: Boolean,
    compactMode: Boolean,
    onSelectTarget: (TargetType) -> Unit,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    val spriteRes = when (targetType) {
        TargetType.SKINNY -> R.drawable.char_skinny
        TargetType.CHUBBY -> R.drawable.char_chubby
        TargetType.TRASH -> R.drawable.char_trash
    }
    val titleRes = when (targetType) {
        TargetType.SKINNY -> R.string.gameplay_target_skinny
        TargetType.CHUBBY -> R.string.gameplay_target_chubby
        TargetType.TRASH -> R.string.gameplay_target_trash
    }
    val foodHintRes = when (targetType) {
        TargetType.SKINNY -> R.string.gameplay_target_skinny_food
        TargetType.CHUBBY -> R.string.gameplay_target_chubby_food
        TargetType.TRASH -> R.string.gameplay_target_trash_food
    }
    val contentDescRes = when (targetType) {
        TargetType.SKINNY -> R.string.gameplay_cd_skinny
        TargetType.CHUBBY -> R.string.gameplay_cd_chubby
        TargetType.TRASH -> R.string.gameplay_cd_trash
    }

    val containerColor = when (targetType) {
        TargetType.SKINNY -> if (isHighlighted) colorScheme.primaryContainer else colorScheme.surface.copy(alpha = 0.88f)
        TargetType.CHUBBY -> if (isHighlighted) colorScheme.secondaryContainer else colorScheme.surface.copy(alpha = 0.88f)
        TargetType.TRASH -> if (isHighlighted) colorScheme.tertiaryContainer else colorScheme.surface.copy(alpha = 0.88f)
    }
    val accentBorderColor = when (targetType) {
        TargetType.SKINNY -> colorScheme.primary
        TargetType.CHUBBY -> colorScheme.secondary
        TargetType.TRASH -> colorScheme.tertiary
    }
    val badgeBgColor = when (targetType) {
        TargetType.SKINNY -> colorScheme.primaryContainer
        TargetType.CHUBBY -> colorScheme.secondaryContainer
        TargetType.TRASH -> colorScheme.tertiaryContainer
    }
    val badgeContentColor = when (targetType) {
        TargetType.SKINNY -> colorScheme.onPrimaryContainer
        TargetType.CHUBBY -> colorScheme.onSecondaryContainer
        TargetType.TRASH -> colorScheme.onTertiaryContainer
    }

    val animatedScale by animateFloatAsState(
        targetValue = if (isHighlighted) 1.07f else 1.0f,
        animationSpec = tween(durationMillis = 120),
        label = "target_zone_scale"
    )

    val spriteSize = when {
        targetType == TargetType.TRASH && compactMode -> 52.dp
        targetType == TargetType.TRASH -> 64.dp
        compactMode -> 72.dp
        else -> 92.dp
    }

    Surface(
        modifier = modifier
            .graphicsLayer {
                scaleX = animatedScale
                scaleY = animatedScale
            }
            .clip(RoundedCornerShape(8.dp))
            .clickable { onSelectTarget(targetType) },
        shape = RoundedCornerShape(8.dp),
        color = containerColor,
        border = BorderStroke(
            width = if (isHighlighted) 3.dp else 2.dp,
            color = if (isHighlighted) accentBorderColor else colorScheme.outline
        ),
        shadowElevation = if (isHighlighted) 6.dp else 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = if (compactMode) 4.dp else 6.dp,
                    vertical = if (compactMode) 5.dp else 8.dp
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            PixelSpriteImage(
                resId = spriteRes,
                contentDescription = stringResource(contentDescRes),
                modifier = Modifier.size(spriteSize)
            )
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = badgeBgColor,
                border = BorderStroke(1.dp, colorScheme.outline)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(titleRes),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace,
                        color = badgeContentColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = stringResource(foodHintRes),
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = if (compactMode) 9.sp else 10.sp,
                        color = badgeContentColor.copy(alpha = 0.85f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

/**
 * Vertical arcade conveyor belt displaying moving [ActiveFoodItem] plates.
 * Supports dragging individual plates or swiping across the belt lane.
 */
@Composable
fun ConveyorBeltLane(
    items: List<ActiveFoodItem>,
    beltScrollPhase: Float,
    compactMode: Boolean,
    dragThresholdPx: Float,
    onHighlightTargetChange: (TargetType?) -> Unit,
    onSubmitItem: (itemId: Long?, target: TargetType) -> Unit,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    val latestThreshold by rememberUpdatedState(dragThresholdPx)
    val latestHighlightChange by rememberUpdatedState(onHighlightTargetChange)
    val latestSubmitItem by rememberUpdatedState(onSubmitItem)

    val frontMostItemId = remember(items) { items.maxByOrNull { it.yProgress }?.id }
    var bgDragOffset by remember { mutableStateOf(Offset.Zero) }

    BoxWithConstraints(
        modifier = modifier
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = {
                        bgDragOffset = Offset.Zero
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        bgDragOffset += dragAmount
                        latestHighlightChange(
                            resolveDragTarget(bgDragOffset.x, bgDragOffset.y, latestThreshold)
                        )
                    },
                    onDragEnd = {
                        val resolved = resolveDragTarget(bgDragOffset.x, bgDragOffset.y, latestThreshold)
                        bgDragOffset = Offset.Zero
                        latestHighlightChange(null)
                        if (resolved != null) {
                            latestSubmitItem(null, resolved)
                        }
                    },
                    onDragCancel = {
                        bgDragOffset = Offset.Zero
                        latestHighlightChange(null)
                    }
                )
            },
        contentAlignment = Alignment.TopCenter
    ) {
        val beltWidth = (maxWidth * 0.78f).coerceIn(84.dp, 148.dp)
        val plateSize = if (compactMode) 56.dp else 68.dp
        val usableTravelHeight = (maxHeight - plateSize - 8.dp).coerceAtLeast(1.dp)

        // Inner conveyor track & animated slats (clipped inside belt bounds)
        Surface(
            modifier = Modifier
                .width(beltWidth)
                .fillMaxHeight(),
            shape = RoundedCornerShape(6.dp),
            color = colorScheme.surfaceVariant,
            border = BorderStroke(3.dp, colorScheme.outline)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val railWidth = 6.dp.toPx()
                val slatSpacing = 34.dp.toPx()
                val slatHeight = 3.dp.toPx()
                val phaseOffset = (beltScrollPhase % 1f) * slatSpacing

                // Side metallic rails
                drawRect(
                    color = colorScheme.outline.copy(alpha = 0.25f),
                    topLeft = Offset.Zero,
                    size = Size(railWidth, size.height)
                )
                drawRect(
                    color = colorScheme.outline.copy(alpha = 0.25f),
                    topLeft = Offset(size.width - railWidth, 0f),
                    size = Size(railWidth, size.height)
                )

                // Moving horizontal conveyor slats
                var y = -slatSpacing + phaseOffset
                while (y < size.height) {
                    if (y >= 0f) {
                        drawRect(
                            color = colorScheme.outline.copy(alpha = 0.35f),
                            topLeft = Offset(railWidth + 2.dp.toPx(), y),
                            size = Size((size.width - (railWidth + 2.dp.toPx()) * 2f).coerceAtLeast(0f), slatHeight)
                        )
                    }
                    y += slatSpacing
                }

                // Bottom danger line warning strip just above Trash Bin
                val dangerHeight = 8.dp.toPx()
                drawRect(
                    color = colorScheme.errorContainer.copy(alpha = 0.65f),
                    topLeft = Offset(railWidth, size.height - dangerHeight),
                    size = Size((size.width - railWidth * 2f).coerceAtLeast(0f), dangerHeight)
                )
            }
        }

        // Draggable food plates (unclipped so players can drag plates visually onto characters)
        items.forEach { item ->
            key(item.id) {
                var plateDragOffset by remember(item.id) { mutableStateOf(Offset.Zero) }
                var isDraggingPlate by remember(item.id) { mutableStateOf(false) }

                DisposableEffect(item.id) {
                    onDispose {
                        if (isDraggingPlate) {
                            latestHighlightChange(null)
                        }
                    }
                }

                val isFrontMost = item.id == frontMostItemId
                val yOffset = 4.dp + usableTravelHeight * item.yProgress.coerceIn(0f, 1f)

                Surface(
                    modifier = Modifier
                        .zIndex(if (isDraggingPlate) 10f else if (isFrontMost) 2f else 1f)
                        .offset(y = yOffset)
                        .offset {
                            IntOffset(
                                x = plateDragOffset.x.roundToInt(),
                                y = plateDragOffset.y.roundToInt()
                            )
                        }
                        .size(plateSize)
                        .graphicsLayer {
                            val scale = if (isDraggingPlate) 1.12f else 1.0f
                            scaleX = scale
                            scaleY = scale
                        }
                        .pointerInput(item.id) {
                            detectDragGestures(
                                onDragStart = {
                                    isDraggingPlate = true
                                    plateDragOffset = Offset.Zero
                                },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    plateDragOffset += dragAmount
                                    latestHighlightChange(
                                        resolveDragTarget(
                                            plateDragOffset.x,
                                            plateDragOffset.y,
                                            latestThreshold
                                        )
                                    )
                                },
                                onDragEnd = {
                                    val resolved = resolveDragTarget(
                                        plateDragOffset.x,
                                        plateDragOffset.y,
                                        latestThreshold
                                    )
                                    isDraggingPlate = false
                                    plateDragOffset = Offset.Zero
                                    latestHighlightChange(null)
                                    if (resolved != null) {
                                        latestSubmitItem(item.id, resolved)
                                    }
                                },
                                onDragCancel = {
                                    isDraggingPlate = false
                                    plateDragOffset = Offset.Zero
                                    latestHighlightChange(null)
                                }
                            )
                        },
                    shape = CircleShape,
                    color = colorScheme.surface,
                    border = BorderStroke(
                        width = if (isDraggingPlate || isFrontMost) 3.dp else 2.dp,
                        color = when {
                            isDraggingPlate -> colorScheme.primary
                            isFrontMost -> colorScheme.tertiary
                            else -> colorScheme.outline
                        }
                    ),
                    shadowElevation = if (isDraggingPlate) 8.dp else 3.dp
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        PixelSpriteImage(
                            resId = item.type.spriteResId,
                            contentDescription = stringResource(item.type.nameResId),
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}

/**
 * Three thumb-friendly 16-bit arcade action buttons at the bottom of the gameplay screen:
 * Left (Skinny), Center (Trash), Right (Chubby).
 */
@Composable
fun ArcadeActionBar(
    compactMode: Boolean,
    highlightedTarget: TargetType?,
    onActionClick: (TargetType) -> Unit,
    modifier: Modifier = Modifier
) {
    val minBtnHeight = if (compactMode) 48.dp else 56.dp
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ArcadeTargetButton(
            targetType = TargetType.SKINNY,
            title = stringResource(R.string.gameplay_target_skinny),
            subtitle = stringResource(R.string.gameplay_target_skinny_food),
            spriteResId = R.drawable.food_burger,
            isHighlighted = highlightedTarget == TargetType.SKINNY,
            minHeight = minBtnHeight,
            onClick = { onActionClick(TargetType.SKINNY) },
            modifier = Modifier.weight(1f)
        )
        ArcadeTargetButton(
            targetType = TargetType.TRASH,
            title = stringResource(R.string.gameplay_target_trash),
            subtitle = stringResource(R.string.gameplay_target_trash_food),
            spriteResId = R.drawable.food_hazard,
            isHighlighted = highlightedTarget == TargetType.TRASH,
            minHeight = minBtnHeight,
            onClick = { onActionClick(TargetType.TRASH) },
            modifier = Modifier.weight(1f)
        )
        ArcadeTargetButton(
            targetType = TargetType.CHUBBY,
            title = stringResource(R.string.gameplay_target_chubby),
            subtitle = stringResource(R.string.gameplay_target_chubby_food),
            spriteResId = R.drawable.food_salad,
            isHighlighted = highlightedTarget == TargetType.CHUBBY,
            minHeight = minBtnHeight,
            onClick = { onActionClick(TargetType.CHUBBY) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ArcadeTargetButton(
    targetType: TargetType,
    title: String,
    subtitle: String,
    @DrawableRes spriteResId: Int,
    isHighlighted: Boolean,
    minHeight: androidx.compose.ui.unit.Dp,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    val containerColor = when (targetType) {
        TargetType.SKINNY -> colorScheme.primaryContainer
        TargetType.TRASH -> colorScheme.tertiaryContainer
        TargetType.CHUBBY -> colorScheme.secondaryContainer
    }
    val contentColor = when (targetType) {
        TargetType.SKINNY -> colorScheme.onPrimaryContainer
        TargetType.TRASH -> colorScheme.onTertiaryContainer
        TargetType.CHUBBY -> colorScheme.onSecondaryContainer
    }
    val highlightColor = when (targetType) {
        TargetType.SKINNY -> colorScheme.primary
        TargetType.TRASH -> colorScheme.tertiary
        TargetType.CHUBBY -> colorScheme.secondary
    }

    Button(
        onClick = onClick,
        modifier = modifier.heightIn(min = minHeight),
        shape = RoundedCornerShape(6.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        border = BorderStroke(
            width = if (isHighlighted) 3.dp else 2.dp,
            color = if (isHighlighted) highlightColor else colorScheme.outline
        ),
        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            PixelSpriteImage(
                resId = spriteResId,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Column(horizontalAlignment = Alignment.Start) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.Monospace,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Start
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    color = contentColor.copy(alpha = 0.85f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Start
                )
            }
        }
    }
}
