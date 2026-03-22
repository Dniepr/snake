package com.snakegame.app.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test
import kotlin.random.Random

class SnakeGameLogicTest {
    @Test
    fun mergeDirection_blocksInstantReverse() {
        assertEquals(Direction.Right, mergeDirection(Direction.Right, Direction.Left))
        assertEquals(Direction.Up, mergeDirection(Direction.Up, Direction.Down))
        assertEquals(Direction.Left, mergeDirection(Direction.Right, Direction.Up))
    }

    @Test
    fun step_noOpWhenNotPlaying() {
        val s =
            SnakeState(
                gridWidth = 20,
                gridHeight = 20,
                snake = listOf(Cell(5, 5)),
                food = Cell(10, 10),
                direction = Direction.Right,
                queuedDirection = Direction.Right,
                status = GameStatus.Ready,
            )
        val after = s.step(Random(1))
        assertEquals(s, after)
    }

    @Test
    fun step_movesForwardAndShortensTail() {
        val s =
            SnakeState(
                gridWidth = 20,
                gridHeight = 20,
                snake = listOf(Cell(5, 5), Cell(4, 5), Cell(3, 5)),
                food = Cell(0, 0),
                direction = Direction.Right,
                queuedDirection = Direction.Right,
                score = 0,
                status = GameStatus.Playing,
            )
        val after = s.step(Random(1))
        assertEquals(GameStatus.Playing, after.status)
        assertEquals(listOf(Cell(6, 5), Cell(5, 5), Cell(4, 5)), after.snake)
        assertEquals(0, after.score)
    }

    @Test
    fun step_wallCollision_gameOver() {
        val s =
            SnakeState(
                gridWidth = 20,
                gridHeight = 20,
                snake = listOf(Cell(19, 5), Cell(18, 5)),
                food = Cell(0, 0),
                direction = Direction.Right,
                queuedDirection = Direction.Right,
                status = GameStatus.Playing,
            )
        val after = s.step(Random(1))
        assertEquals(GameStatus.GameOver, after.status)
    }

    @Test
    fun step_selfCollision_gameOver() {
        val s =
            SnakeState(
                gridWidth = 20,
                gridHeight = 20,
                snake =
                    listOf(
                        Cell(5, 5),
                        Cell(5, 4),
                        Cell(6, 4),
                        Cell(6, 5),
                    ),
                food = Cell(0, 0),
                direction = Direction.Up,
                queuedDirection = Direction.Up,
                status = GameStatus.Playing,
            )
        val after = s.step(Random(1))
        assertEquals(GameStatus.GameOver, after.status)
    }

    @Test
    fun step_eating_growsAndIncreasesScore() {
        val s =
            SnakeState(
                gridWidth = 5,
                gridHeight = 5,
                snake = listOf(Cell(1, 2), Cell(0, 2)),
                food = Cell(2, 2),
                direction = Direction.Right,
                queuedDirection = Direction.Right,
                score = 0,
                status = GameStatus.Playing,
            )
        val after = s.step(Random(0))
        assertEquals(GameStatus.Playing, after.status)
        assertEquals(3, after.snake.size)
        assertEquals(Cell(2, 2), after.snake.first())
        assertEquals(10, after.score)
        assertNotEquals(s.food, after.food)
        assert(after.food !in after.snake.toSet())
    }

    @Test
    fun step_canEnterFormerTailCellWhenNotGrowing() {
        val s =
            SnakeState(
                gridWidth = 20,
                gridHeight = 20,
                snake = listOf(Cell(2, 1), Cell(3, 1)),
                food = Cell(0, 0),
                direction = Direction.Right,
                queuedDirection = Direction.Right,
                status = GameStatus.Playing,
            )
        val after = s.step(Random(1))
        assertEquals(GameStatus.Playing, after.status)
        assertEquals(listOf(Cell(3, 1), Cell(2, 1)), after.snake)
    }
}
