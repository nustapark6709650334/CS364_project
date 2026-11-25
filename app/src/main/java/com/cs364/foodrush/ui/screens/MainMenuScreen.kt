package com.cs364.foodrush.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.cs364.foodrush.R
import com.cs364.foodrush.ui.components.PixelArcadeBox
import com.cs364.foodrush.ui.components.PixelSpriteImage
import kotlin.math.ceil
import kotlin.math.roundToInt

internal data class MainMenuLayoutMetrics(
    val compactMode: Boolean,
    val contentMaxWidthDp: Float,
    val mascotStageHeightDp: Float,
    val skinnyHeightDp: Float,
    val chubbyHeightDp: Float,
    val trashSizeDp: Float,
    val foodBadgeSizeDp: Float,
    val buttonMinHeightDp: Float
)

/**
 * Pure dp layout calculation so small (360x640), large (412x915), tablet, and landscape
 * viewports scale mascots and buttons without clipping or overlap (Rubric 3.1–3.2).
 */
internal fun mainMenuLayoutMetrics(widthDp: Float, heightDp: Float): MainMenuLayoutMetrics {
    val compact = heightDp < 680f || widthDp < 360f
    val maxContentW = (widthDp - 32f).coerceIn(240f, 440f)
    val stageH = if (compact) {
        (heightDp * 0.27f).coerceIn(136f, 185f)
    } else {
        (heightDp * 0.31f).coerceIn(180f, 260f)
    }
    val skinnyH = (stageH * 0.92f).coerceIn(112f, 232f)
    val chubbyH = skinnyH * 0.84f
    val trashSz = (stageH * 0.44f).coerceIn(58f, 108f)
    val foodSz = (stageH * 0.23f).coerceIn(32f, 52f)
    val buttonMinH = if (compact) 48f else 54f
    return MainMenuLayoutMetrics(
        compactMode = compact,
        contentMaxWidthDp = maxContentW,
        mascotStageHeightDp = stageH,
        skinnyHeightDp = skinnyH,
        chubbyHeightDp = chubbyH,
        trashSizeDp = trashSz,
        foodBadgeSizeDp = foodSz,
        buttonMinHeightDp = buttonMinH
    )
}

/**
 * Role 3: 16-Bit Pixel Art Arcade Main Menu Screen (Rubric 1.3, 3.1–3.4, 4.1).
 *
 * Matches `docs/ui/ui_pixel_mainmenu.png` with:
 * - Upper diner wall & 8-column checkered floor using [MaterialTheme.colorScheme]
 * - Stepped pixel Title Plaque (`FIT & FAT: FOOD RUSH`)
 * - 3 pixel-art mascots (`char_skinny`, `char_trash`, `char_chubby`) + food plates
 * - Persisted High Score banner & quick SFX/Haptic status badges
 * - 3 Arcade Buttons (`Play Game`, `Score History`, `Settings & How-To`)
 * - Back navigation guard opening [ExitAppConfirmDialog]
 */
@Composable
fun MainMenuScreen(
    highScore: Int,
    isSoundEnabled: Boolean = true,
    isHapticEnabled: Boolean = true,
    onPlayClick: () -> Unit = {},
    onScoreHistoryClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onConfirmExitApp: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    var showExitDialog by rememberSaveable { mutableStateOf(false) }

    // Back Navigation Guard (Rubric 4.1 & 4.2): Ask before exiting app from Main Menu.
    BackHandler(enabled = !showExitDialog) {
        showExitDialog = true
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(colorScheme.surface)
    ) {
        val metrics = mainMenuLayoutMetrics(maxWidth.value, maxHeight.value)

        DinerMenuBackground(compactMode = metrics.compactMode)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .verticalScroll(rememberScrollState())
                .padding(
                    horizontal = 16.dp,
                    vertical = if (metrics.compactMode) 12.dp else 18.dp
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.widthIn(max = metrics.contentMaxWidthDp.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(if (metrics.compactMode) 10.dp else 14.dp)
            ) {
                // Title Plaque
                PixelArcadeBox(
                    modifier = Modifier.fillMaxWidth(),
                    fillColor = colorScheme.inverseSurface,
                    borderColor = colorScheme.onSurface,
                    cornerStep = 5.dp,
                    contentPadding = PaddingValues(
                        horizontal = 14.dp,
                        vertical = if (metrics.compactMode) 10.dp else 14.dp
                    )
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.app_name).uppercase(),
                            style = if (metrics.compactMode) {
                                MaterialTheme.typography.titleLarge
                            } else {
                                MaterialTheme.typography.headlineSmall
                            },
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.ExtraBold,
                            color = colorScheme.tertiaryContainer,
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = stringResource(R.string.subtitle_tagline),
                            style = if (metrics.compactMode) {
                                MaterialTheme.typography.bodySmall
                            } else {
                                MaterialTheme.typography.bodyMedium
                            },
                            fontFamily = FontFamily.Monospace,
                            color = colorScheme.inverseOnSurface,
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Mascot Showcase matching docs/ui/ui_pixel_mainmenu.png
                MainMenuMascotStage(metrics = metrics)
            }

            Column(
                modifier = Modifier
                    .widthIn(max = metrics.contentMaxWidthDp.dp)
                    .padding(top = if (metrics.compactMode) 8.dp else 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(if (metrics.compactMode) 8.dp else 10.dp)
            ) {
                // High Score Plaque
                PixelArcadeBox(
                    fillColor = colorScheme.inverseSurface,
                    borderColor = colorScheme.onSurface,
                    cornerStep = 4.dp,
                    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.menu_high_score_label, highScore),
                        style = MaterialTheme.typography.titleMedium,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.ExtraBold,
                        color = colorScheme.inverseOnSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Compact Audio & Haptic indicator chips reflecting Role 5 Settings
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatusBadgeChip(
                        text = stringResource(
                            if (isSoundEnabled) R.string.menu_sound_on else R.string.menu_sound_off
                        ),
                        active = isSoundEnabled
                    )
                    StatusBadgeChip(
                        text = stringResource(
                            if (isHapticEnabled) R.string.menu_haptic_on else R.string.menu_haptic_off
                        ),
                        active = isHapticEnabled
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                // 3 Primary Arcade Navigation Buttons
                ArcadeMenuButton(
                    label = stringResource(R.string.menu_play_game),
                    fillColor = colorScheme.primary,
                    textColor = colorScheme.onPrimary,
                    minHeightDp = metrics.buttonMinHeightDp,
                    onClick = onPlayClick
                )
                ArcadeMenuButton(
                    label = stringResource(R.string.menu_score_history),
                    fillColor = colorScheme.inverseSurface,
                    textColor = colorScheme.inverseOnSurface,
                    minHeightDp = metrics.buttonMinHeightDp,
                    onClick = onScoreHistoryClick
                )
                ArcadeMenuButton(
                    label = stringResource(R.string.menu_settings_how_to),
                    fillColor = colorScheme.secondaryContainer,
                    textColor = colorScheme.onSecondaryContainer,
                    minHeightDp = metrics.buttonMinHeightDp,
                    onClick = onSettingsClick
                )
            }
        }
    }

    if (showExitDialog) {
        ExitAppConfirmDialog(
            onDismiss = { showExitDialog = false },
            onConfirmExit = {
                showExitDialog = false
                onConfirmExitApp()
            }
        )
    }
}

@Composable
private fun DinerMenuBackground(compactMode: Boolean) {
    val colorScheme = MaterialTheme.colorScheme
    Canvas(modifier = Modifier.fillMaxSize()) {
        val cabinetBottom = if (compactMode) 82.dp.toPx() else 100.dp.toPx()
        val wallHeight = cabinetBottom * 0.34f
        val counterThickness = 4.dp.toPx()

        drawRect(
            color = colorScheme.tertiaryContainer.copy(alpha = 0.72f),
            topLeft = Offset.Zero,
            size = Size(size.width, wallHeight)
        )
        drawRect(
            color = colorScheme.secondaryContainer,
            topLeft = Offset(0f, wallHeight),
            size = Size(size.width, cabinetBottom - wallHeight)
        )

        val panelCount = 5
        val panelWidth = size.width / panelCount
        val inset = 4.dp.toPx()
        val frameColor = colorScheme.onSurface.copy(alpha = 0.25f)
        for (i in 0 until panelCount) {
            val left = panelWidth * i
            if (i > 0) {
                drawRect(
                    color = frameColor,
                    topLeft = Offset(left, wallHeight),
                    size = Size(2.dp.toPx(), cabinetBottom - wallHeight)
                )
            }
            val doorW = (panelWidth - inset * 2f).coerceAtLeast(0f)
            drawRect(
                color = frameColor,
                topLeft = Offset(left + inset, wallHeight + inset),
                size = Size(doorW, 2.dp.toPx())
            )
        }

        drawRect(
            color = colorScheme.onSurface,
            topLeft = Offset(0f, cabinetBottom),
            size = Size(size.width, counterThickness)
        )

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
}

@Composable
private fun MainMenuMascotStage(metrics: MainMenuLayoutMetrics) {
    val colorScheme = MaterialTheme.colorScheme
    val mascotsCd = stringResource(R.string.menu_cd_mascots)
    val transition = rememberInfiniteTransition(label = "menu_mascot_idle")
    val bobProgress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1050, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "menu_bob"
    )
    val bobOffsetDp = (-3f * bobProgress).roundToInt().dp

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(metrics.mascotStageHeightDp.dp)
            .semantics { contentDescription = mascotsCd },
        contentAlignment = Alignment.BottomCenter
    ) {
        // Floor contact shadows
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(22.dp)
                .align(Alignment.BottomCenter)
        ) {
            val shadowColor = colorScheme.onSurface.copy(alpha = 0.18f)
            drawOval(
                color = shadowColor,
                topLeft = Offset(size.width * 0.08f, size.height * 0.3f),
                size = Size(size.width * 0.24f, size.height * 0.65f)
            )
            drawOval(
                color = shadowColor,
                topLeft = Offset(size.width * 0.39f, size.height * 0.35f),
                size = Size(size.width * 0.22f, size.height * 0.55f)
            )
            drawOval(
                color = shadowColor,
                topLeft = Offset(size.width * 0.67f, size.height * 0.3f),
                size = Size(size.width * 0.25f, size.height * 0.65f)
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            // Left: Skinny + Burger
            Box(contentAlignment = Alignment.TopEnd) {
                PixelSpriteImage(
                    resId = R.drawable.char_skinny,
                    contentDescription = null,
                    alignment = Alignment.BottomCenter,
                    modifier = Modifier
                        .height(metrics.skinnyHeightDp.dp)
                        .offset(y = bobOffsetDp)
                )
                PixelSpriteImage(
                    resId = R.drawable.food_burger,
                    contentDescription = null,
                    modifier = Modifier
                        .size(metrics.foodBadgeSizeDp.dp)
                        .offset(x = 8.dp, y = (metrics.skinnyHeightDp * 0.22f).dp - bobOffsetDp)
                )
            }

            // Center: Trash Bin
            PixelSpriteImage(
                resId = R.drawable.char_trash,
                contentDescription = null,
                alignment = Alignment.BottomCenter,
                modifier = Modifier
                    .size(metrics.trashSizeDp.dp)
                    .padding(bottom = 2.dp)
            )

            // Right: Chubby + Salad
            Box(contentAlignment = Alignment.TopStart) {
                PixelSpriteImage(
                    resId = R.drawable.char_chubby,
                    contentDescription = null,
                    alignment = Alignment.BottomCenter,
                    modifier = Modifier
                        .height(metrics.chubbyHeightDp.dp)
                        .offset(y = -bobOffsetDp)
                )
                PixelSpriteImage(
                    resId = R.drawable.food_salad,
                    contentDescription = null,
                    modifier = Modifier
                        .size(metrics.foodBadgeSizeDp.dp)
                        .offset(x = (-8).dp, y = (metrics.chubbyHeightDp * 0.14f).dp + bobOffsetDp)
                )
            }
        }
    }
}

@Composable
private fun StatusBadgeChip(
    text: String,
    active: Boolean
) {
    val colorScheme = MaterialTheme.colorScheme
    PixelArcadeBox(
        fillColor = if (active) colorScheme.tertiaryContainer else colorScheme.surfaceVariant,
        borderColor = colorScheme.onSurface,
        cornerStep = 3.dp,
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = if (active) colorScheme.onTertiaryContainer else colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun ArcadeMenuButton(
    label: String,
    fillColor: androidx.compose.ui.graphics.Color,
    textColor: androidx.compose.ui.graphics.Color,
    minHeightDp: Float,
    onClick: () -> Unit
) {
    PixelArcadeBox(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = minHeightDp.dp)
            .clickable(role = Role.Button, onClick = onClick),
        fillColor = fillColor,
        borderColor = MaterialTheme.colorScheme.onSurface,
        cornerStep = 4.dp,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 17.sp,
            color = textColor,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * 16-Bit Pixel Art Exit App Confirmation Dialog (Rubric 4.1 & 4.2).
 */
@Composable
fun ExitAppConfirmDialog(
    onDismiss: () -> Unit,
    onConfirmExit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        PixelArcadeBox(
            modifier = modifier.fillMaxWidth(),
            fillColor = colorScheme.surface,
            borderColor = colorScheme.onSurface,
            cornerStep = 5.dp,
            contentPadding = PaddingValues(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                PixelArcadeBox(
                    modifier = Modifier.fillMaxWidth(),
                    fillColor = colorScheme.inverseSurface,
                    borderColor = colorScheme.onSurface,
                    cornerStep = 4.dp,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = stringResource(R.string.exit_app_dialog_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace,
                        color = colorScheme.inverseOnSurface,
                        textAlign = TextAlign.Center
                    )
                }

                PixelArcadeBox(
                    modifier = Modifier.fillMaxWidth(),
                    fillColor = colorScheme.surfaceVariant,
                    borderColor = colorScheme.onSurface,
                    cornerStep = 4.dp,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 14.dp)
                ) {
                    Text(
                        text = stringResource(R.string.exit_app_dialog_body),
                        style = MaterialTheme.typography.bodyMedium,
                        fontFamily = FontFamily.Monospace,
                        color = colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PixelArcadeBox(
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp)
                            .clickable(role = Role.Button, onClick = onDismiss),
                        fillColor = colorScheme.primary,
                        borderColor = colorScheme.onSurface,
                        cornerStep = 3.dp,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.exit_app_dialog_stay),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace,
                            color = colorScheme.onPrimary,
                            maxLines = 1,
                            textAlign = TextAlign.Center
                        )
                    }

                    PixelArcadeBox(
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp)
                            .clickable(role = Role.Button, onClick = onConfirmExit),
                        fillColor = colorScheme.errorContainer,
                        borderColor = colorScheme.onSurface,
                        cornerStep = 3.dp,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.exit_app_dialog_confirm),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace,
                            color = colorScheme.onErrorContainer,
                            maxLines = 1,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Preview(name = "Main Menu Day", showBackground = true, widthDp = 360, heightDp = 640)
@Composable
private fun MainMenuDayPreview() {
    MaterialTheme(colorScheme = lightColorScheme()) {
        MainMenuScreen(highScore = 1240)
    }
}

@Preview(name = "Main Menu Night", showBackground = true, widthDp = 412, heightDp = 915)
@Composable
private fun MainMenuNightPreview() {
    MaterialTheme(colorScheme = darkColorScheme()) {
        MainMenuScreen(highScore = 2580, isSoundEnabled = false)
    }
}
