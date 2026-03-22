package com.snakegame.app.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.snakeDataStore: DataStore<Preferences> by preferencesDataStore(name = "snake_game")

private val HighScoreKey = intPreferencesKey("high_score")

class HighScoreRepository(
    context: Context,
) {
    private val dataStore = context.applicationContext.snakeDataStore

    val highScoreFlow: Flow<Int> =
        dataStore.data.map { prefs ->
            prefs[HighScoreKey] ?: 0
        }

    suspend fun updateHighScoreIfBetter(score: Int) {
        dataStore.edit { prefs ->
            val current = prefs[HighScoreKey] ?: 0
            if (score > current) {
                prefs[HighScoreKey] = score
            }
        }
    }
}
