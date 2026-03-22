package com.snakegame.app.game

import kotlin.random.Random

data class Cell(val x: Int, val y: Int)

enum class Direction {
    Up,
    Down,
    Left,
    Right,
}

enum class GameStatus {
    Ready,
    Playing,
    Paused,
    GameOver,
}

data class SnakeState(
    val gridWidth: Int,
    val gridHeight: Int,
    val snake: List<Cell>,
    val food: Cell,
    val direction: Direction,
    val queuedDirection: Direction,
    val score: Int = 0,
    val status: GameStatus = GameStatus.Ready,
)

fun Direction.opposite(): Direction =
    when (this) {
        Direction.Up -> Direction.Down
        Direction.Down -> Direction.Up
        Direction.Left -> Direction.Right
        Direction.Right -> Direction.Left
    }

fun Direction.toDelta(): Pair<Int, Int> =
    when (this) {
        Direction.Up -> 0 to -1
        Direction.Down -> 0 to 1
        Direction.Left -> -1 to 0
        Direction.Right -> 1 to 0
    }

/** Resolves queued input: never instant 180° turn. */
fun mergeDirection(current: Direction, queued: Direction): Direction =
    if (queued.opposite() == current) current else queued

fun spawnFood(
    occupied: Set<Cell>,
    gridWidth: Int,
    gridHeight: Int,
    random: Random,
): Cell {
    val empty =
        buildList {
            for (x in 0 until gridWidth) {
                for (y in 0 until gridHeight) {
                    val c = Cell(x, y)
                    if (c !in occupied) add(c)
                }
            }
        }
    require(empty.isNotEmpty()) { "No empty cell for food" }
    return empty[random.nextInt(empty.size)]
}

fun initialSnakeState(
    gridWidth: Int = 20,
    gridHeight: Int = 20,
    random: Random,
): SnakeState {
    val midX = gridWidth / 2
    val midY = gridHeight / 2
    val snake =
        listOf(
            Cell(midX, midY),
            Cell(midX - 1, midY),
            Cell(midX - 2, midY),
        )
    val food = spawnFood(snake.toSet(), gridWidth, gridHeight, random)
    return SnakeState(
        gridWidth = gridWidth,
        gridHeight = gridHeight,
        snake = snake,
        food = food,
        direction = Direction.Right,
        queuedDirection = Direction.Right,
        score = 0,
        status = GameStatus.Ready,
    )
}

fun SnakeState.withQueuedDirection(dir: Direction): SnakeState = copy(queuedDirection = dir)

/**
 * One simulation tick. No-op unless [SnakeState.status] is [GameStatus.Playing].
 * Uses [random] when food is eaten to pick the next cell.
 */
fun SnakeState.step(random: Random): SnakeState {
    if (status != GameStatus.Playing) return this

    val dir = mergeDirection(direction, queuedDirection)
    val (dx, dy) = dir.toDelta()
    val head = snake.first()
    val newHead = Cell(head.x + dx, head.y + dy)

    if (newHead.x !in 0 until gridWidth || newHead.y !in 0 until gridHeight) {
        return copy(
            direction = dir,
            queuedDirection = dir,
            status = GameStatus.GameOver,
        )
    }

    val bodyWithoutTail = snake.dropLast(1)
    if (newHead in bodyWithoutTail) {
        return copy(
            direction = dir,
            queuedDirection = dir,
            status = GameStatus.GameOver,
        )
    }

    val eating = newHead == food
    val newSnake =
        if (eating) {
            listOf(newHead) + snake
        } else {
            listOf(newHead) + snake.dropLast(1)
        }
    val newScore = if (eating) score + 10 else score
    val newFood =
        if (eating) {
            spawnFood(newSnake.toSet(), gridWidth, gridHeight, random)
        } else {
            food
        }

    return copy(
        snake = newSnake,
        food = newFood,
        direction = dir,
        queuedDirection = dir,
        score = newScore,
    )
}
