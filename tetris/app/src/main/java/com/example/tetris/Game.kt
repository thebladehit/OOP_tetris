package com.example.tetris

import com.example.tetris.constance.Constance
import com.example.tetris.figures.Figure
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread
import kotlin.random.Random

class Game {
    private var isGame = true
    private var score = 0
    private var rows = 0
    private var rowCollected = 0
    private var speed: Long = 500

    private var gameField: MutableList<MutableList<Block?>> = mutableListOf()
    private lateinit var figure: Figure
    private lateinit var nextFigure: Figure

    private lateinit var invalidateCanvas: (list: MutableList<MutableList<Block?>>) -> Unit
    private lateinit var changeScoreView: (score: Int) -> Unit
    private lateinit var changeRowsView: (rows: Int) -> Unit
    private lateinit var changeNextFigureView: (list: MutableList<MutableList<Block?>>) -> Unit
    private lateinit var stopGame: (score: Int) -> Unit

    private fun fillGameField() {
        gameField = mutableListOf()
        for (i in 0..Constance.FIELD_ROWS) {
            val row = mutableListOf<Block?>()
            for (j in 0..9) {
                row.add(null)
            }
            gameField.add(row)
        }
    }

    private fun generateStartFigures() {
        var randomNumber = Random.nextInt(7) + 1
        var figureConstructor = Constance.FIGURE_CONSTRUCTORS[randomNumber]
        nextFigure = figureConstructor!!.invoke()
        randomNumber = Random.nextInt(7) + 1
        figureConstructor = Constance.FIGURE_CONSTRUCTORS[randomNumber]
        figure = figureConstructor!!.invoke()
    }

    private fun generateNextFigure() {
        figure = nextFigure
        val randomNumber = Random.nextInt(7) + 1
        val figureConstructor = Constance.FIGURE_CONSTRUCTORS[randomNumber]
        nextFigure = figureConstructor!!.invoke()
    }

    private fun clearFigure() {
        for (row in gameField.indices) {
            for (col in gameField[row].indices) {
                val block = gameField[row][col]
                if (block != null && block.isMove) {
                    gameField[row][col] = null
                }
            }
        }
    }

    private fun placeFigure() {
        for (y in figure.figureShape.indices) {
            for (x in figure.figureShape[y].indices) {
                if (figure.figureShape[y][x] == 1) {
                    val block = Block(figure.color)
                    gameField[figure.currentRow + y][figure.startPos + x] = block
                }
            }
        }
    }

    private fun canFigureFall() : Boolean {
        for (row in gameField.indices) {
            for (col in gameField[row].indices) {
                val block = gameField[row][col]
                if (block != null && block.isMove) {
                    if (!isBlockYExist(row + 1)) return false
                    val nextBlock = gameField[row + 1][col]
                    if (nextBlock == null || nextBlock.isMove) continue
                    else return false
                }
            }
        }
        return true
    }

    private fun canFigureMoveSide(side: Int) : Boolean {
        for (row in gameField.indices) {
            for (col in gameField[row].indices) {
                val block = gameField[row][col]
                if (block != null && block.isMove) {
                    if (!isBlockXExist(col + side)) return false
                    val nextSideBlock = gameField[row][col + side]
                    if (nextSideBlock == null || nextSideBlock.isMove) continue
                    else return false
                }
            }
        }
        return true
    }

    private fun tryRotateFigure() {
        figure.rotate()
        for (row in figure.figureShape.indices) {
            for (col in figure.figureShape[row].indices) {
                if (figure.figureShape[row][col] == 1) {
                    val curY = figure.currentRow + row
                    val curX = figure.startPos + col
                    if (!isBlockXExist(curX) || !isBlockYExist(curY) || gameField[curY][curX] != null) {
                        for (i in 0..2) figure.rotate()
                    }
                }
            }
        }
    }

    private fun isBlockYExist(y: Int) : Boolean {
        return y <= Constance.FIELD_ROWS - 1
    }

    private fun isBlockXExist(x: Int) : Boolean {
        return x >= 0 && x <= Constance.FIELD_COLS - 1
    }

    private fun stopFigure() {
        for (row in gameField.size - 1 downTo 0) {
            for (col in gameField[row].size - 1 downTo 0) {
                if (gameField[row][col] != null) {
                    if (gameField[row][col]!!.isMove) {
                        gameField[row][col]!!.isMove = false
                    }
                }
            }
        }
    }

    private fun moveFigure() {
        if (canFigureFall()) {
            for (row in gameField.size - 1 downTo 0) {
                for (col in gameField[row].size - 1 downTo 0) {
                    if (gameField[row][col] != null) {
                        if (gameField[row][col]!!.isMove) {
                            gameField[row + 1][col] = gameField[row][col]
                            gameField[row][col] = null
                        }
                    }
                }
            }
            figure.currentRow++
        } else {
            stopFigure()
            deleteFullRow()
            addScore()
            if (!checkGameOver()) {
                generateNextFigure()
                showNextFigure()
                placeFigure()
            }
        }
    }

    fun moveFigureLeft() {
        if (!canFigureMoveSide(Constance.LEFT)) return
        for (row in gameField.size - 1 downTo 0) {
            for (col in gameField[row].indices) {
                val block = gameField[row][col]
                if (block != null && block.isMove) {
                    gameField[row][col + Constance.LEFT] = block
                    gameField[row][col] = null
                }
            }
        }
        figure.startPos += Constance.LEFT
        invalidateCanvas(gameField)
    }

    fun moveFigureRight() {
        if (!canFigureMoveSide(Constance.RIGHT)) return
        for (row in gameField.size - 1 downTo 0) {
            for (col in gameField[row].size - 1 downTo 0) {
                val block = gameField[row][col]
                if (block != null && block.isMove) {
                    gameField[row][col + Constance.RIGHT] = block
                    gameField[row][col] = null
                }
            }
        }
        figure.startPos += Constance.RIGHT
        invalidateCanvas(gameField)
    }

    fun moveFigureDown() {
        if (!canFigureFall()) return
        for (row in gameField.size - 1 downTo 0) {
            for (col in gameField[row].size - 1 downTo 0) {
                if (gameField[row][col] != null) {
                    if (gameField[row][col]!!.isMove) {
                        gameField[row + 1][col] = gameField[row][col]
                        gameField[row][col] = null
                    }
                }
            }
        }
        figure.currentRow++
        invalidateCanvas(gameField)
    }

    fun rotateFigure() {
        clearFigure()
        tryRotateFigure()
        placeFigure()
        invalidateCanvas(gameField)
    }

    private fun deleteFullRow() {
        for (row in gameField.size - 1 downTo 0) {
            var notNullBlockCount = 0
            for (col in gameField[row].indices) {
                if (gameField[row][col] != null) notNullBlockCount++
            }
            if (notNullBlockCount == Constance.FIELD_COLS) {
                gameField.removeAt(row)
                val newRow: MutableList<Block?> = mutableListOf()
                for (i in 0..<Constance.FIELD_COLS) {
                    newRow.add(null)
                }
                gameField.add(0, newRow)
                rowCollected++
                deleteFullRow()
            }
        }
    }

    private fun checkGameOver() : Boolean {
        for (col in gameField[3]) {
            if (col != null) {
                isGame = false
                stopGame(score)
                return true
            }
        }
        return false
    }

    private fun addScore() {
        if (rowCollected == 0) return
        score += Constance.ROWS_COST[rowCollected]!!
        addRows(rowCollected)
        rowCollected = 0
        changeScoreView(score)
    }

    private fun addRows(addRows: Int) {
        rows += addRows
        if (rows % 10 == 0) increaseSpeed()
        changeRowsView(rows)
    }

    private fun showNextFigure() {
        val blocks: MutableList<MutableList<Block?>> = mutableListOf()
        for (row in nextFigure.figureShape) {
            val blocksRow = mutableListOf<Block?>()
            for (col in row) {
                if (col == 1) blocksRow.add(Block(nextFigure.color))
                else blocksRow.add(null)
            }
            blocks.add(blocksRow)
        }
        changeNextFigureView(blocks)
    }

    fun startGame() {
        thread {
            while (isGame) {
                invalidateCanvas(gameField)
                TimeUnit.MILLISECONDS.sleep(speed)
                moveFigure()
            }
        }
    }

    fun initGame() {
        fillGameField()
        generateStartFigures()
        showNextFigure()
        placeFigure()
    }

    private fun setDefaultStartValue() {
        isGame = true
        rowCollected = 0
        rows = 0
        score = 0
        speed = 500
    }

    private fun increaseSpeed() {
        val newSpeed = speed * 0.7
        speed = newSpeed.toLong()
    }

    fun restartGame() {
        initGame()
        setDefaultStartValue()
        changeScoreView(0)
        changeRowsView(0)
        startGame()
    }

    fun setInvalidateCanvas(invalidateFn: (list: MutableList<MutableList<Block?>>) -> Unit) {
        invalidateCanvas = invalidateFn
    }

    fun setChangeNextFigure(fn: (list: MutableList<MutableList<Block?>>) -> Unit) {
        changeNextFigureView = fn
    }

    fun setChangeScore(fn: (score: Int) -> Unit) {
        changeScoreView = fn
    }

    fun setChangeRows(fn: (rows: Int) -> Unit) {
        changeRowsView = fn
    }

    fun setStopGame(fn: (score: Int) -> Unit) {
        stopGame = fn
    }
}