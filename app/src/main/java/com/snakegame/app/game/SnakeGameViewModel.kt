package com.snakegame.app.game

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.snakegame.app.data.HighScoreRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.random.Random

class SnakeGameViewModel(
    application: Application,
) : AndroidViewModel(application) {
    private val repository = HighScoreRepository(application.applicationContext)
    private val random = Random.Default

    private val _gameState =
        MutableStateFlow(
            initialSnakeState(random = Random(System.nanoTime())),
        )
    val gameState: StateFlow<SnakeState> = _gameState.asStateFlow()

    val highScore: StateFlow<Int> =
        repository.highScoreFlow.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            0,
        )

    private var tickJob: Job? = null

    companion object {
        private const val TICK_MS = 150L
    }

    private fun startTickLoop() {
        tickJob?.cancel()
        tickJob =
            viewModelScope.launch {
                while (isActive) {
                    delay(TICK_MS)
                    val current = _gameState.value
                    if (current.status != GameStatus.Playing) continue
                    val next = current.step(random)
                    if (next.status == GameStatus.GameOver) {
                        launch {
                            repository.updateHighScoreIfBetter(next.score)
                        }
                    }
                    _gameState.value = next
                }
            }
    }

    fun queueDirection(direction: Direction) {
        _gameState.update { it.withQueuedDirection(direction) }
    }

    fun beginPlaying() {
        when (_gameState.value.status) {
            GameStatus.Ready -> {
                _gameState.update { it.copy(status = GameStatus.Playing) }
                startTickLoop()
            }
            GameStatus.Paused -> {
                _gameState.update { it.copy(status = GameStatus.Playing) }
                startTickLoop()
            }
            else -> Unit
        }
    }

    fun pause() {
        val s = _gameState.value
        if (s.status != GameStatus.Playing) return
        tickJob?.cancel()
        tickJob = null
        _gameState.update { it.copy(status = GameStatus.Paused) }
    }

    fun restart() {
        tickJob?.cancel()
        tickJob = null
        _gameState.value =
            initialSnakeState(random = Random(System.nanoTime())).copy(
                status = GameStatus.Playing,
            )
        startTickLoop()
    }

    fun dismissGameOverToReady() {
        tickJob?.cancel()
        tickJob = null
        _gameState.value = initialSnakeState(random = Random(System.nanoTime()))
    }

    override fun onCleared() {
        tickJob?.cancel()
        super.onCleared()
    }
}
