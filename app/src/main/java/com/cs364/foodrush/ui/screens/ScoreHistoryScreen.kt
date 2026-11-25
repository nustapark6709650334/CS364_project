package com.cs364.foodrush.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.util.Date
import com.cs364.foodrush.R
import com.cs364.foodrush.data.ScoreEntry
import com.cs364.foodrush.data.ScoreRepository
import com.cs364.foodrush.ui.components.ConfirmDeleteDialog
import com.cs364.foodrush.ui.components.PixelArcadeBox

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScoreHistoryScreen(repository: ScoreRepository, onNavigateBack: () -> Unit) {
    val scores by repository.scores.collectAsStateWithLifecycle(initialValue = emptyList())
    var selectedId by rememberSaveable { mutableStateOf<String?>(null) }
    var showClearAll by rememberSaveable { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val colors = MaterialTheme.colorScheme

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.leaderboard_title), fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Text("←", color = colors.onSurface)
                    }
                }
            )
        },
        containerColor = colors.background
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(Modifier.widthIn(max = 520.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (scores.isEmpty()) {
                    PixelArcadeBox(Modifier.fillMaxWidth(), fillColor = colors.surfaceVariant, borderColor = colors.onSurface) {
                        Text(stringResource(R.string.leaderboard_empty), textAlign = TextAlign.Center,
                            color = colors.onSurfaceVariant, style = MaterialTheme.typography.bodyLarge)
                    }
                } else {
                    scores.forEachIndexed { index, entry ->
                        ScoreHistoryRow(index + 1, entry, onDelete = { selectedId = entry.id })
                    }
                    PixelArcadeBox(Modifier.fillMaxWidth().heightIn(min = 48.dp).clickable { showClearAll = true }, fillColor = colors.errorContainer,
                        borderColor = colors.onSurface) {
                        Text(stringResource(R.string.role4_clear_all), color = colors.onErrorContainer,
                            fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(4.dp))
                    }
                }
                PixelArcadeBox(Modifier.fillMaxWidth().heightIn(min = 48.dp).clickable(onClick = onNavigateBack), fillColor = colors.primary,
                    borderColor = colors.onSurface) {
                    androidx.compose.foundation.layout.Box(Modifier.fillMaxWidth().padding(4.dp), contentAlignment = Alignment.Center) {
                        Text(stringResource(R.string.btn_back), color = colors.onPrimary, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    selectedId?.let { id ->
        ConfirmDeleteDialog(
            title = stringResource(R.string.role4_delete_title),
            message = stringResource(R.string.role4_delete_message),
            onDismiss = { selectedId = null },
            onConfirm = {
                selectedId = null
                scope.launch { repository.deleteScore(id) }
            }
        )
    }
    if (showClearAll) {
        ConfirmDeleteDialog(
            title = stringResource(R.string.role4_clear_title),
            message = stringResource(R.string.role4_clear_message),
            onDismiss = { showClearAll = false },
            onConfirm = {
                showClearAll = false
                scope.launch { repository.clearAllScores() }
            }
        )
    }
}

@Composable
private fun ScoreHistoryRow(rank: Int, entry: ScoreEntry, onDelete: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    PixelArcadeBox(Modifier.fillMaxWidth(), fillColor = colors.surfaceVariant, borderColor = colors.onSurface) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("#$rank", color = colors.primary, fontWeight = FontWeight.ExtraBold, fontFamily = FontFamily.Monospace)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(entry.playerName, color = colors.onSurface, fontWeight = FontWeight.Bold)
                Text(DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(Date(entry.timestamp)),
                    color = colors.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
                if (entry.failReason.isNotBlank()) Text(entry.failReason, color = colors.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall)
            }
            Text(entry.score.toString(), color = colors.primary, fontWeight = FontWeight.ExtraBold)
            IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.role4_delete), tint = colors.error) }
        }
    }
}
