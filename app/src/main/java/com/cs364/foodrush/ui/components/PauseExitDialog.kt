package com.cs364.foodrush.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.cs364.foodrush.R

/**
 * Role 2: 16-Bit Pixel Art Arcade Pause & Exit Confirmation Dialog (Issue #5, Rubric 4.2 & 4.4).
 *
 * Styled with stepped pixel arcade borders matching `docs/ui/ui_pixel_subscreens.png`.
 * Shown when the player taps Pause, presses system Back during gameplay,
 * or returns after Activity `ON_PAUSE` / `ON_STOP`.
 */
@Composable
fun PauseExitDialog(
    score: Int,
    speedLabel: String,
    onResume: () -> Unit,
    onConfirmExit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme

    Dialog(
        onDismissRequest = onResume,
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
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Dark stepped arcade header plaque
                PixelArcadeBox(
                    modifier = Modifier.fillMaxWidth(),
                    fillColor = colorScheme.inverseSurface,
                    borderColor = colorScheme.onSurface,
                    cornerStep = 4.dp,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = stringResource(R.string.pause_dialog_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace,
                        color = colorScheme.inverseOnSurface,
                        textAlign = TextAlign.Center
                    )
                }

                // Preserved score & speed status box
                PixelArcadeBox(
                    modifier = Modifier.fillMaxWidth(),
                    fillColor = colorScheme.surfaceVariant,
                    borderColor = colorScheme.onSurface,
                    cornerStep = 4.dp,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 14.dp)
                ) {
                    Text(
                        text = stringResource(R.string.pause_dialog_body, score, speedLabel),
                        style = MaterialTheme.typography.bodyMedium,
                        fontFamily = FontFamily.Monospace,
                        color = colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }

                // 3D Pixel Keycap Action Buttons: Resume vs Exit to Main Menu
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PixelArcadeBox(
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp)
                            .clickable(role = Role.Button, onClick = onResume),
                        fillColor = colorScheme.primary,
                        borderColor = colorScheme.onSurface,
                        cornerStep = 3.dp,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.pause_dialog_resume),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace,
                            color = colorScheme.onPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
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
                            text = stringResource(R.string.pause_dialog_exit),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace,
                            color = colorScheme.onErrorContainer,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Preview(name = "Pause Dialog Day", showBackground = true)
@Composable
private fun PauseExitDialogDayPreview() {
    MaterialTheme(colorScheme = lightColorScheme()) {
        PauseExitDialog(
            score = 18,
            speedLabel = "LV.4 • 1.7x",
            onResume = {},
            onConfirmExit = {}
        )
    }
}

@Preview(name = "Pause Dialog Night", showBackground = true)
@Composable
private fun PauseExitDialogNightPreview() {
    MaterialTheme(colorScheme = darkColorScheme()) {
        PauseExitDialog(
            score = 42,
            speedLabel = "MAX • 2.4x",
            onResume = {},
            onConfirmExit = {}
        )
    }
}
