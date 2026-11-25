package com.cs364.foodrush.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.cs364.foodrush.R
import com.cs364.foodrush.game.ActiveFoodItem
import com.cs364.foodrush.game.TargetType
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.roundToInt
import kotlin.math.sin

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
 * Reaction poses for Skinny & Chubby sprites (Issue #4):
 * - [IDLE]: Waiting eagerly by the conveyor belt
 * - [YUMMY]: Happy belly-rub after receiving the right food
 * - [DEAD]: Collapsed flat on the floor with `X_X` eyes after wrong food/hazard
 * - [SAD]: Standing and wiping tears `T_T` when the other character collapses
 */
enum class CharacterPose {
    IDLE,
    YUMMY,
    DEAD,
    SAD
}

/**
 * Renders a 16-bit pixel art sprite with [FilterQuality.None] so pixels stay razor-sharp.
 */
@Composable
fun PixelSpriteImage(
    @DrawableRes resId: Int,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    alignment: Alignment = Alignment.Center
) {
    val resources = LocalContext.current.resources
    val configuration = LocalConfiguration.current
    val painters = remember(resources, configuration) { mutableMapOf<Int, BitmapPainter>() }
    // Crop only at draw time: source PNGs and their alpha remain untouched.
    // Ignore near-transparent generator speckles when measuring the silhouette.
    val painter = painters.getOrPut(resId) {
        val bitmap = ImageBitmap.imageResource(resources, resId)
        val pixels = IntArray(bitmap.width * bitmap.height)
        bitmap.readPixels(pixels)
        val bounds = spriteContentBounds(pixels, bitmap.width, bitmap.height)
        BitmapPainter(bitmap, IntOffset(bounds.left, bounds.top),
            IntSize(bounds.width, bounds.height), FilterQuality.None)
    }
    Image(
        painter = painter,
        contentDescription = contentDescription,
        alignment = alignment,
        contentScale = ContentScale.Fit,
        modifier = modifier
    )
}

/**
 * 16-bit stepped pixel-art arcade container with notched pixel corners,
 * crisp dark outline, top-left specular bevel, and bottom shadow lip.
 */
@Composable
fun PixelArcadeBox(
    modifier: Modifier = Modifier,
    fillColor: Color = MaterialTheme.colorScheme.inverseSurface,
    borderColor: Color = MaterialTheme.colorScheme.onSurface,
    highlightColor: Color = MaterialTheme.colorScheme.surface.copy(alpha = 0.28f),
    shadowColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f),
    cornerStep: Dp = 4.dp,
    contentPadding: PaddingValues = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val s = cornerStep.toPx().coerceAtMost(size.minDimension / 4f)
            val b = 3.dp.toPx().coerceAtMost(size.minDimension / 6f)
            val bevel = 2.dp.toPx()
            val lip = 3.dp.toPx()

            // Outer notched pixel border
            drawRect(
                color = borderColor,
                topLeft = Offset(s, 0f),
                size = Size((size.width - 2f * s).coerceAtLeast(0f), size.height)
            )
            drawRect(
                color = borderColor,
                topLeft = Offset(0f, s),
                size = Size(size.width, (size.height - 2f * s).coerceAtLeast(0f))
            )

            // Inner container fill
            drawRect(
                color = fillColor,
                topLeft = Offset(s, b),
                size = Size((size.width - 2f * s).coerceAtLeast(0f), (size.height - 2f * b).coerceAtLeast(0f))
            )
            drawRect(
                color = fillColor,
                topLeft = Offset(b, s),
                size = Size((size.width - 2f * b).coerceAtLeast(0f), (size.height - 2f * s).coerceAtLeast(0f))
            )

            // Top & left highlight bevel
            drawRect(
                color = highlightColor,
                topLeft = Offset(s, b),
                size = Size((size.width - 2f * s).coerceAtLeast(0f), bevel)
            )
            drawRect(
                color = highlightColor,
                topLeft = Offset(b, s),
                size = Size(bevel, (size.height - 2f * s).coerceAtLeast(0f))
            )

            // Bottom inner shadow lip
            drawRect(
                color = shadowColor,
                topLeft = Offset(s, (size.height - b - lip).coerceAtLeast(b)),
                size = Size((size.width - 2f * s).coerceAtLeast(0f), lip)
            )
        }

        Box(
            modifier = Modifier.padding(contentPadding),
            contentAlignment = Alignment.Center,
            content = content
        )
    }
}

/**
 * Full-body unboxed 16-bit character / trash bin standing directly on the checkered floor
 * with 3-layer procedural pixel animations & symmetric reaction pose sprites:
 * 1) Continuous idle breathing & bobbing (or hazard rattle on Trash Bin / crying tremble on [CharacterPose.SAD])
 * 2) Drag-hover lunge toward the conveyor belt + floor target highlight
 * 3) Chomp / gulp squash-and-stretch bounce + rising "+1" popup on [CharacterPose.YUMMY], or collapse thud on [CharacterPose.DEAD]
 */
@Composable
fun CharacterTargetZone(
    targetType: TargetType,
    isHighlighted: Boolean,
    pose: CharacterPose = CharacterPose.IDLE,
    enabled: Boolean = true,
    reactionTick: Int = 0,
    hasApproachingHazard: Boolean = false,
    onSelectTarget: (TargetType) -> Unit,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    val spriteRes = when (targetType) {
        TargetType.SKINNY -> when (pose) {
            CharacterPose.IDLE -> R.drawable.char_skinny
            CharacterPose.YUMMY -> R.drawable.char_skinny_yum
            CharacterPose.DEAD -> R.drawable.char_skinny_dead
            CharacterPose.SAD -> R.drawable.char_skinny_sad
        }
        TargetType.CHUBBY -> when (pose) {
            CharacterPose.IDLE -> R.drawable.char_chubby
            CharacterPose.YUMMY -> R.drawable.char_chubby_yum
            CharacterPose.DEAD -> R.drawable.char_chubby_dead
            CharacterPose.SAD -> R.drawable.char_chubby_sad
        }
        TargetType.TRASH -> R.drawable.char_trash
    }
    val contentDescRes = when (targetType) {
        TargetType.SKINNY -> R.string.gameplay_cd_skinny
        TargetType.CHUBBY -> R.string.gameplay_cd_chubby
        TargetType.TRASH -> R.string.gameplay_cd_trash
    }

    // Layer 1: Continuous idle breathing/bobbing, hazard rattle, & sad crying tremble
    val infiniteTransition = rememberInfiniteTransition(label = "idle_$targetType")
    val idleCycle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when {
                    pose == CharacterPose.YUMMY -> 480
                    targetType == TargetType.SKINNY -> 1100
                    targetType == TargetType.CHUBBY -> 1300
                    else -> 680
                },
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "idle_cycle"
    )
    val hazardRattle by infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 135, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "hazard_rattle"
    )
    val deadDropOffsetDp = remember { Animatable(0f) }
    LaunchedEffect(pose) {
        if (pose == CharacterPose.DEAD) {
            deadDropOffsetDp.snapTo(-6f)
            deadDropOffsetDp.animateTo(0f, tween(210, easing = FastOutSlowInEasing))
        } else {
            deadDropOffsetDp.snapTo(0f)
        }
    }

    // Keep the pixel silhouette rigid: small whole-dp motion instead of continuous
    // non-uniform scaling and rotation, which shimmer and distort pixel edges.
    val idleBobDp = if (!enabled) 0f else when (pose) {
        CharacterPose.DEAD -> deadDropOffsetDp.value.roundToInt().toFloat()
        CharacterPose.SAD -> 0f
        CharacterPose.YUMMY -> -2f * idleCycle
        CharacterPose.IDLE -> if (targetType == TargetType.TRASH) 0f else -idleCycle
    }.roundToInt().toFloat()
    val idleTiltDeg = if (enabled && targetType == TargetType.TRASH && hasApproachingHazard) {
        hazardRattle * 1.5f
    } else 0f
    // Layer 2: Drag-hover eager lunge toward belt
    val canInteract = enabled && pose != CharacterPose.DEAD && pose != CharacterPose.SAD
    val hoverScale by animateFloatAsState(
        targetValue = if (isHighlighted && canInteract) 1.03f else 1.0f,
        animationSpec = tween(durationMillis = 120),
        label = "hover_scale"
    )
    val hoverLeanXDp by animateFloatAsState(
        targetValue = when {
            !isHighlighted || !canInteract -> 0f
            targetType == TargetType.SKINNY -> 3f
            targetType == TargetType.CHUBBY -> -3f
            else -> 0f
        },
        animationSpec = tween(durationMillis = 120),
        label = "hover_lean_x"
    )
    val hoverPopYDp by animateFloatAsState(
        targetValue = if (isHighlighted && canInteract && targetType == TargetType.TRASH) -6f else 0f,
        animationSpec = tween(durationMillis = 120),
        label = "hover_pop_y"
    )

    // Layer 3: Eat / chomp / gulp squash-and-stretch reaction + floating "+1"
    val chompScale = remember { Animatable(1f) }
    val plusOneAlpha = remember { Animatable(0f) }
    val plusOneRiseDp = remember { Animatable(0f) }

    LaunchedEffect(reactionTick) {
        if (reactionTick > 0) {
            launch {
                chompScale.snapTo(1f)
                chompScale.animateTo(0.98f, tween(80))
                chompScale.animateTo(1.03f, tween(120))
                chompScale.animateTo(1.0f, tween(110))
            }
            launch {
                plusOneAlpha.snapTo(1f)
                plusOneRiseDp.snapTo(0f)
                launch {
                    plusOneRiseDp.animateTo(-30f, tween(420, easing = FastOutSlowInEasing))
                }
                plusOneAlpha.animateTo(0f, tween(420))
            }
        }
    }

    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .clickable(
                enabled = canInteract,
                interactionSource = interactionSource,
                indication = null
            ) { onSelectTarget(targetType) },
        contentAlignment = Alignment.BottomCenter
    ) {
        // Pixel floor oval shadow aligned directly beneath shoes / kneeling knees / collapsed body
        Canvas(
            modifier = Modifier
                .fillMaxSize()
        ) {
            val shadowWidth = when {
                pose == CharacterPose.DEAD -> size.width * 0.88f
                targetType == TargetType.SKINNY -> size.width * 0.48f
                targetType == TargetType.CHUBBY -> size.width * 0.62f
                else -> size.width * 0.72f
            }
            val shadowHeight = (size.height * 0.085f).coerceIn(10.dp.toPx(), 22.dp.toPx())
            val shadowLeft = when {
                pose != CharacterPose.IDLE -> (size.width - shadowWidth) * 0.5f
                targetType == TargetType.SKINNY -> size.width * 0.08f
                targetType == TargetType.CHUBBY -> size.width * 0.28f
                else -> (size.width - shadowWidth) * 0.36f
            }
            val shadowTop = size.height - shadowHeight * 0.90f

            if (isHighlighted && canInteract) {
                drawOval(
                    color = colorScheme.primary.copy(alpha = 0.50f),
                    topLeft = Offset(shadowLeft - 6.dp.toPx(), shadowTop - 3.dp.toPx()),
                    size = Size(shadowWidth + 12.dp.toPx(), shadowHeight + 6.dp.toPx())
                )
            }
            drawOval(
                color = colorScheme.onSurface.copy(alpha = 0.24f),
                topLeft = Offset(shadowLeft, shadowTop),
                size = Size(shadowWidth, shadowHeight)
            )
        }

        // Animated 16-bit pixel character sprite (feet/base anchored at Alignment.BottomCenter)
        PixelSpriteImage(
            resId = spriteRes,
            contentDescription = stringResource(contentDescRes),
            alignment = Alignment.BottomCenter,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    transformOrigin = TransformOrigin(0.5f, 1.0f)
                    translationX = hoverLeanXDp.dp.toPx()
                    translationY = (idleBobDp + hoverPopYDp).dp.toPx()
                    scaleX = hoverScale * chompScale.value
                    scaleY = hoverScale * chompScale.value
                    rotationZ = idleTiltDeg
                }
        )

        // Floating "+1" pixel arcade popup right above character when fed
        if (plusOneAlpha.value > 0.01f) {
            PixelArcadeBox(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = plusOneRiseDp.value.dp)
                    .graphicsLayer { alpha = plusOneAlpha.value },
                fillColor = colorScheme.primary,
                borderColor = colorScheme.onSurface,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = stringResource(R.string.gameplay_plus_one),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.Monospace,
                    color = colorScheme.onPrimary
                )
            }
        }
    }
}

/**
 * Full-height 3D metallic arcade conveyor belt with animated horizontal slats,
 * side-rail rivets, and direct pixel-art food plates (no duplicate circles).
 */
@Composable
fun ConveyorBeltLane(
    items: List<ActiveFoodItem>,
    enabled: Boolean = true,
    beltScrollPhase: Float,
    compactMode: Boolean,
    dragThresholdPx: Float,
    topPlateInset: Dp = 56.dp,
    onHighlightTargetChange: (TargetType?) -> Unit,
    onSubmitItem: (itemId: Long?, target: TargetType) -> Unit,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    val latestThreshold by rememberUpdatedState(dragThresholdPx)
    val latestHighlightChange by rememberUpdatedState(onHighlightTargetChange)
    val latestSubmitItem by rememberUpdatedState(onSubmitItem)

    val frontMostItemId = remember(items) { items.maxByOrNull { it.yProgress }?.id }

    BoxWithConstraints(
        modifier = modifier,
        contentAlignment = Alignment.TopCenter
    ) {
        val beltWidth = maxWidth
        val plateSize = (beltWidth * 0.86f).coerceIn(if (compactMode) 68.dp else 82.dp, 118.dp)
        val usableTravelHeight = (maxHeight - topPlateInset - plateSize).coerceAtLeast(1.dp)

        // 3D Metallic Pixel Conveyor Track (Canvas)
        Canvas(modifier = Modifier.fillMaxSize()) {
            val outerBorder = 3.dp.toPx()
            val railWidth = 8.dp.toPx()
            val innerGroove = 2.dp.toPx()
            val slatSpacing = 44.dp.toPx()
            val phaseOffset = (beltScrollPhase % 1f) * slatSpacing

            // Outer dark pixel frame
            drawRect(
                color = colorScheme.onSurface,
                topLeft = Offset.Zero,
                size = size
            )

            // Left & right metallic rails
            drawRect(
                color = colorScheme.secondaryContainer,
                topLeft = Offset(outerBorder, 0f),
                size = Size(railWidth, size.height)
            )
            drawRect(
                color = colorScheme.secondaryContainer,
                topLeft = Offset(size.width - outerBorder - railWidth, 0f),
                size = Size(railWidth, size.height)
            )

            // Specular highlight along outer edge of both rails
            drawRect(
                color = colorScheme.surface.copy(alpha = 0.55f),
                topLeft = Offset(outerBorder, 0f),
                size = Size(2.dp.toPx(), size.height)
            )
            drawRect(
                color = colorScheme.surface.copy(alpha = 0.45f),
                topLeft = Offset(size.width - outerBorder - railWidth, 0f),
                size = Size(2.dp.toPx(), size.height)
            )

            // Inner dark rail grooves
            val trackLeft = outerBorder + railWidth
            val trackWidth = (size.width - 2f * trackLeft).coerceAtLeast(0f)
            drawRect(
                color = colorScheme.onSurface,
                topLeft = Offset(trackLeft, 0f),
                size = Size(innerGroove, size.height)
            )
            drawRect(
                color = colorScheme.onSurface,
                topLeft = Offset(size.width - trackLeft - innerGroove, 0f),
                size = Size(innerGroove, size.height)
            )

            // Conveyor center belt surface
            val beltInnerLeft = trackLeft + innerGroove
            val beltInnerWidth = (trackWidth - 2f * innerGroove).coerceAtLeast(0f)
            drawRect(
                color = colorScheme.surfaceVariant,
                topLeft = Offset(beltInnerLeft, 0f),
                size = Size(beltInnerWidth, size.height)
            )
            drawRect(
                color = colorScheme.onSurface.copy(alpha = 0.14f),
                topLeft = Offset(beltInnerLeft, 0f),
                size = Size(beltInnerWidth, size.height)
            )

            // Scrolling horizontal slats + side-rail bolts driven by beltScrollPhase
            val seamDark = colorScheme.onSurface.copy(alpha = 0.48f)
            val seamHighlight = colorScheme.surface.copy(alpha = 0.48f)
            val rivetColor = colorScheme.onSurface.copy(alpha = 0.6f)
            var y = -slatSpacing + phaseOffset
            while (y < size.height) {
                if (y >= 0f) {
                    drawRect(
                        color = seamDark,
                        topLeft = Offset(beltInnerLeft, y),
                        size = Size(beltInnerWidth, 3.dp.toPx())
                    )
                    drawRect(
                        color = seamHighlight,
                        topLeft = Offset(beltInnerLeft, y + 3.dp.toPx()),
                        size = Size(beltInnerWidth, 2.dp.toPx())
                    )
                    // Scrolling rail rivets
                    drawRect(
                        color = rivetColor,
                        topLeft = Offset(outerBorder + 3.dp.toPx(), y),
                        size = Size(3.dp.toPx(), 3.dp.toPx())
                    )
                    drawRect(
                        color = rivetColor,
                        topLeft = Offset(size.width - outerBorder - railWidth + 3.dp.toPx(), y),
                        size = Size(3.dp.toPx(), 3.dp.toPx())
                    )
                }
                y += slatSpacing
            }

            // Bottom danger edge strip right above Trash Bin
            val dangerHeight = 8.dp.toPx()
            drawRect(
                color = colorScheme.error.copy(alpha = 0.55f),
                topLeft = Offset(beltInnerLeft, (size.height - outerBorder - dangerHeight).coerceAtLeast(0f)),
                size = Size(beltInnerWidth, dangerHeight)
            )
            drawRect(
                color = colorScheme.onSurface,
                topLeft = Offset(0f, (size.height - outerBorder).coerceAtLeast(0f)),
                size = Size(size.width, outerBorder)
            )
        }

        // Direct pixel-art food plates on the belt (unclipped so drags glide over characters!)
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
                val yOffset = topPlateInset + usableTravelHeight * item.yProgress.coerceIn(0f, 1f)
                val rollerBobDp = if (isDraggingPlate) 0f else {
                    sin((beltScrollPhase + item.yProgress * 2f) * 2.0 * PI).toFloat() * 0.9f
                }

                Box(
                    modifier = Modifier
                        .zIndex(if (isDraggingPlate) 20f else if (isFrontMost) 2f else 1f)
                        .offset(y = yOffset + rollerBobDp.dp)
                        .offset {
                            IntOffset(
                                x = plateDragOffset.x.roundToInt(),
                                y = plateDragOffset.y.roundToInt()
                            )
                        }
                        .size(plateSize)
                        .graphicsLayer {
                            val scale = if (isDraggingPlate) 1.16f else 1.0f
                            scaleX = scale
                            scaleY = scale
                        }
                        .pointerInput(item.id, enabled) {
                            if (!enabled) return@pointerInput
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
                    contentAlignment = Alignment.Center
                ) {
                    // Subtle dark oval drop shadow under the plate directly on the belt
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawOval(
                            color = colorScheme.onSurface.copy(alpha = if (isDraggingPlate) 0.18f else 0.30f),
                            topLeft = Offset(size.width * 0.12f, size.height * 0.62f),
                            size = Size(size.width * 0.76f, size.height * 0.28f)
                        )
                    }

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
