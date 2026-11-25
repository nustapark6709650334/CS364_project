package com.cs364.foodrush.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.UUID

private val Context.scoreDataStore: DataStore<Preferences> by preferencesDataStore(name = "food_rush_score_history")

/** A single locally persisted game result. */
data class ScoreEntry(
    val id: String = UUID.randomUUID().toString(),
    val playerName: String,
    val score: Int,
    val failReason: String,
    val timestamp: Long = System.currentTimeMillis()
)

/** Persistent score history backed by Preferences DataStore and Android's built-in JSON APIs. */
class ScoreRepository(context: Context) {
    private val appContext = context.applicationContext
    private val historyKey = stringPreferencesKey("score_history_json")

    val scores: Flow<List<ScoreEntry>> = appContext.scoreDataStore.data
        .catch { error ->
            if (error is IOException) emit(emptyPreferences()) else throw error
        }
        .map { preferences -> decode(preferences[historyKey].orEmpty()) }

    suspend fun saveScore(entry: ScoreEntry) {
        appContext.scoreDataStore.edit { preferences ->
            val current = decode(preferences[historyKey].orEmpty()).toMutableList()
            current.removeAll { it.id == entry.id }
            current.add(entry.copy(playerName = entry.playerName.trim().ifBlank { "Player" }, score = entry.score.coerceAtLeast(0) ))
            preferences[historyKey] = encode(current.sortedByDescending { it.score })
        }
    }

    suspend fun deleteScore(id: String) {
        appContext.scoreDataStore.edit { preferences ->
            val remaining = decode(preferences[historyKey].orEmpty()).filterNot { it.id == id }
            preferences[historyKey] = encode(remaining)
        }
    }

    suspend fun clearAllScores() {
        appContext.scoreDataStore.edit { preferences -> preferences[historyKey] = "[]" }
    }

    suspend fun getHighScore(): Int = scores.first().maxOfOrNull { it.score } ?: 0

    private fun encode(entries: List<ScoreEntry>): String = JSONArray().apply {
        entries.forEach { entry ->
            put(JSONObject().apply {
                put("id", entry.id)
                put("playerName", entry.playerName)
                put("score", entry.score)
                put("failReason", entry.failReason)
                put("timestamp", entry.timestamp)
            })
        }
    }.toString()

    private fun decode(raw: String): List<ScoreEntry> = try {
        val array = JSONArray(raw.ifBlank { "[]" })
        buildList {
            for (index in 0 until array.length()) {
                val item = array.optJSONObject(index) ?: continue
                add(ScoreEntry(
                    id = item.optString("id", UUID.randomUUID().toString()),
                    playerName = item.optString("playerName", "Player"),
                    score = item.optInt("score", 0),
                    failReason = item.optString("failReason", ""),
                    timestamp = item.optLong("timestamp", 0L)
                ))
            }
        }.sortedByDescending { it.score }
    } catch (_: Exception) { emptyList() }
}
