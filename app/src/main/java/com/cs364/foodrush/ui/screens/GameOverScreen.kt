package com.cs364.foodrush.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.semantics.Role
import androidx.compose.foundation.clickable
import com.cs364.foodrush.R
import com.cs364.foodrush.data.ScoreEntry
import com.cs364.foodrush.data.ScoreRepository
import com.cs364.foodrush.ui.components.PixelArcadeBox
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope

@Composable
fun GameOverScreen(
    score: Int,
    bestScore: Int,
    failReason: String,
    repository: ScoreRepository,
    onPlayAgain: () -> Unit,
    onViewHistory: () -> Unit,
    onNavigateHome: () -> Unit
) {
    var playerName by rememberSaveable { mutableStateOf("") }
    var isSaving by rememberSaveable { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val colors = MaterialTheme.colorScheme

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Column(Modifier.widthIn(max = 440.dp), horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)) {
            PixelArcadeBox(Modifier.fillMaxWidth(), fillColor = colors.errorContainer, borderColor = colors.onSurface) {
                Text(stringResource(R.string.game_over_title).uppercase(), style = MaterialTheme.typography.headlineSmall,
                    fontFamily = FontFamily.Monospace, fontWeight = FontWeight.ExtraBold,
                    color = colors.onErrorContainer, textAlign = TextAlign.Center)
            }
            PixelArcadeBox(Modifier.fillMaxWidth(), fillColor = colors.surfaceVariant, borderColor = colors.onSurface) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(R.string.game_over_score, score), style = MaterialTheme.typography.headlineMedium,
                        fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = colors.onSurface)
                    Text(stringResource(R.string.game_over_best_score, maxOf(score, bestScore)),
                        style = MaterialTheme.typography.titleMedium, color = colors.primary)
                    Text(failReason, style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant,
                        textAlign = TextAlign.Center)
                }
            }
            OutlinedTextField(
                value = playerName, onValueChange = { playerName = it.take(24) },
                modifier = Modifier.fillMaxWidth(), singleLine = true,
                label = { Text(stringResource(R.string.role4_player_name)) },
                placeholder = { Text(stringResource(R.string.role4_enter_name)) },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
            )
            PixelArcadeBox(
                modifier = Modifier.fillMaxWidth().heightIn(min = 50.dp)
                    .clickable(role = Role.Button, enabled = !isSaving && playerName.isNotBlank()) {
                        isSaving = true
                        scope.launch {
                            repository.saveScore(ScoreEntry(playerName = playerName.trim(), score = score, failReason = failReason))
                            isSaving = false
                            onViewHistory()
                        }
                    },
                fillColor = if (playerName.isNotBlank()) colors.primary else colors.surfaceVariant,
                borderColor = colors.onSurface
            ) {
                Text(stringResource(if (isSaving) R.string.role4_saving else R.string.role4_save_score),
                    color = if (playerName.isNotBlank()) colors.onPrimary else colors.onSurfaceVariant,
                    fontWeight = FontWeight.ExtraBold, fontFamily = FontFamily.Monospace)
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                PixelArcadeBox(Modifier.weight(1f).heightIn(min = 48.dp).clickable(role = Role.Button, onClick = onPlayAgain),
                    fillColor = colors.secondaryContainer, borderColor = colors.onSurface) {
                    Text(stringResource(R.string.btn_restart), color = colors.onSecondaryContainer, fontWeight = FontWeight.Bold)
                }
                PixelArcadeBox(Modifier.weight(1f).heightIn(min = 48.dp).clickable(role = Role.Button, onClick = onNavigateHome),
                    fillColor = colors.surfaceVariant, borderColor = colors.onSurface) {
                    Text(stringResource(R.string.btn_home), color = colors.onSurface, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
