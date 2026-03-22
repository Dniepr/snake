package com.snakegame.app.ui

import android.app.Application
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.snakegame.app.game.Direction
import com.snakegame.app.game.GameStatus
import com.snakegame.app.game.SnakeGameViewModel
import com.snakegame.app.game.SnakeState
import com.snakegame.app.ui.theme.BackgroundDeep
import com.snakegame.app.ui.theme.BackgroundElevated
import com.snakegame.app.ui.theme.BoardSurface
import com.snakegame.app.ui.theme.FoodAccent
import com.snakegame.app.ui.theme.FoodGlowOuter
import com.snakegame.app.ui.theme.HudMuted
import com.snakegame.app.ui.theme.SnakeHeadGreen
import com.snakegame.app.ui.theme.SnakeTailGreen
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

@Composable
fun SnakeGameScreen(
    viewModel: SnakeGameViewModel =
        viewModel(
            factory =
                ViewModelProvider.AndroidViewModelFactory.getInstance(
                    LocalContext.current.applicationContext as Application,
                ),
        ),
) {
    val state by viewModel.gameState.collectAsStateWithLifecycle()
    val highScore by viewModel.highScore.collectAsStateWithLifecycle()

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(BackgroundElevated, BackgroundDeep),
                    ),
                )
                .statusBarsPadding(),
    ) {
        GameHud(
            score = state.score,
            highScore = highScore,
            onPause = { viewModel.pause() },
            canPause = state.status == GameStatus.Playing,
        )
        Box(
            modifier =
                Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center,
        ) {
            SnakeBoard(
                state = state,
                onSwipe = { dir -> viewModel.queueDirection(dir) },
            )
            when (state.status) {
                GameStatus.Ready ->
                    CenterOverlayCard(
                        title = "Snake",
                        subtitle = "Swipe to steer · Eat the orbs",
                    ) {
                        FilledTonalButton(onClick = { viewModel.beginPlaying() }) {
                            Text("Play")
                        }
                    }
                GameStatus.Paused ->
                    CenterOverlayCard(
                        title = "Paused",
                        subtitle = "Take a breath",
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            FilledTonalButton(onClick = { viewModel.beginPlaying() }) {
                                Text("Resume")
                            }
                            OutlinedButton(onClick = { viewModel.restart() }) {
                                Text("Restart")
                            }
                        }
                    }
                GameStatus.GameOver ->
                    CenterOverlayCard(
                        title = "Game over",
                        subtitle = "Score ${state.score}",
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            FilledTonalButton(onClick = { viewModel.restart() }) {
                                Text("Play again")
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedButton(onClick = { viewModel.dismissGameOverToReady() }) {
                                Text("Main menu")
                            }
                        }
                    }
                GameStatus.Playing -> Unit
            }
        }
    }
}

@Composable
private fun GameHud(
    score: Int,
    highScore: Int,
    onPause: () -> Unit,
    canPause: Boolean,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Score",
                style = MaterialTheme.typography.labelLarge,
                color = HudMuted,
            )
            Text(
                text = score.toString(),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "Best",
                style = MaterialTheme.typography.labelLarge,
                color = HudMuted,
            )
            Text(
                text = highScore.toString(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Medium,
                color = SnakeHeadGreen,
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        if (canPause) {
            IconButton(onClick = onPause) {
                Icon(
                    imageVector = Icons.Default.Pause,
                    contentDescription = "Pause",
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
        } else {
            Spacer(modifier = Modifier.width(48.dp))
        }
    }
}

@Composable
private fun CenterOverlayCard(
    title: String,
    subtitle: String,
    actions: @Composable () -> Unit,
) {
    Card(
        modifier = Modifier.padding(24.dp),
        shape = RoundedCornerShape(24.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 28.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyLarge,
                color = HudMuted,
            )
            Spacer(modifier = Modifier.height(20.dp))
            actions()
        }
    }
}

@Composable
private fun SnakeBoard(
    state: SnakeState,
    onSwipe: (Direction) -> Unit,
) {
    var dragAccum by remember { mutableStateOf(Offset.Zero) }
    val outline = MaterialTheme.colorScheme.outline

    Canvas(
        modifier =
            Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDrag = { change, dragAmount ->
                            change.consume()
                            dragAccum += dragAmount
                        },
                        onDragEnd = {
                            val dx = dragAccum.x
                            val dy = dragAccum.y
                            val threshold = 24f
                            if (abs(dx) > abs(dy) && abs(dx) > threshold) {
                                onSwipe(if (dx > 0) Direction.Right else Direction.Left)
                            } else if (abs(dy) > threshold) {
                                onSwipe(if (dy > 0) Direction.Down else Direction.Up)
                            }
                            dragAccum = Offset.Zero
                        },
                        onDragCancel = {
                            dragAccum = Offset.Zero
                        },
                    )
                },
    ) {
        val gw = state.gridWidth
        val gh = state.gridHeight
        val maxCells = max(gw, gh).toFloat()
        val cell = min(size.width, size.height) / maxCells
        val boardW = cell * gw
        val boardH = cell * gh
        val ox = (size.width - boardW) / 2f
        val oy = (size.height - boardH) / 2f
        val corner = CornerRadius(18f, 18f)

        drawRoundRect(
            color = BoardSurface,
            topLeft = Offset(ox, oy),
            size = Size(boardW, boardH),
            cornerRadius = corner,
        )
        drawRoundRect(
            brush =
                Brush.linearGradient(
                    colors =
                        listOf(
                            outline.copy(alpha = 0.35f),
                            outline.copy(alpha = 0.08f),
                        ),
                    start = Offset(ox, oy),
                    end = Offset(ox + boardW, oy + boardH),
                ),
            topLeft = Offset(ox, oy),
            size = Size(boardW, boardH),
            cornerRadius = corner,
            style = Stroke(width = 2f),
        )

        fun cellCenter(c: com.snakegame.app.game.Cell): Offset =
            Offset(
                ox + c.x * cell + cell / 2f,
                oy + c.y * cell + cell / 2f,
            )

        val foodCenter = cellCenter(state.food)
        val foodR = cell * 0.42f
        drawCircle(
            color = FoodGlowOuter,
            radius = foodR * 1.35f,
            center = foodCenter,
        )
        drawCircle(
            color = FoodAccent,
            radius = foodR,
            center = foodCenter,
        )

        val n = state.snake.size
        state.snake.forEachIndexed { index, cellPos ->
            val center = cellCenter(cellPos)
            val t =
                if (n <= 1) {
                    1f
                } else {
                    1f - index / (n - 1).toFloat()
                }
            val color = lerp(SnakeTailGreen, SnakeHeadGreen, t)
            val r = cell * (if (index == 0) 0.48f else 0.42f)
            drawCircle(
                color = color,
                radius = r,
                center = center,
            )
            if (index == 0) {
                drawCircle(
                    color = SnakeHeadGreen.copy(alpha = 0.35f),
                    radius = r * 1.2f,
                    center = center,
                )
            }
        }
    }
}
