package com.minigames.seabattle.algoritm

import kotlin.random.Random

const val SHIP_MAX_SIZE = 4
const val OVER_SIZE_BOARD = SHIP_MAX_SIZE + 2

const val OVER_SIZE_POINT = 1000.0
const val LOCKED_CELL_POINT = 0.2
const val SHIP_POINT = 1.0

class ShipsGenerator(private val boardSize: Int) {
    fun initShips(shipsType: List<Int>): List<Ship> {
        val firstGenerationShips = mutableListOf<Ship>()
        for (ship in shipsType) {
            val row = (1..boardSize).random()
            val column = (1..boardSize).random()
            val orientation = Random.nextBoolean()
            firstGenerationShips.add(Ship(row, column, orientation, ship))
        }
        return firstGenerationShips
    }

    fun initBoard(boardSize: Int): Array<DoubleArray> {

        val normalBoard = Array(boardSize) { DoubleArray(boardSize) }

        val oversizeBoard =
            Array(boardSize + OVER_SIZE_BOARD) { DoubleArray(boardSize + OVER_SIZE_BOARD) { OVER_SIZE_POINT } }

        //добавляем значения рабочего поля на оверсайз доску
        for (i in 1..boardSize)
            for (j in 1..boardSize)
                oversizeBoard[i][j] = normalBoard[i - 1][j - 1]

        return oversizeBoard
    }

    //    fun shipsFitness(shipGeneration: List<Ship>, originalBoard: Array<DoubleArray>): Array<DoubleArray> {
    fun shipsFitness(shipGeneration: List<Ship>): Array<DoubleArray> {
        val board = initBoard(boardSize)
        for (ship in shipGeneration) {
            val resultMask = getShipMask(ship)
            val rowEndIndex = ship.rowStart - 1 + resultMask.lastIndex
            val colEndIndex = ship.columnStart - 1 + resultMask[0].lastIndex

            for ((maskRowIterator, i) in (ship.rowStart - 1..rowEndIndex).withIndex()) {
                for ((maskColIterator, j) in (ship.columnStart - 1..colEndIndex).withIndex()) {
                    board[i][j] += resultMask[maskRowIterator][maskColIterator]
                }
            }
        }
        return board
    }

    fun getFitnessSum(board: Array<DoubleArray>): Double {
        val normalBoardSum = board.sumOf { row ->
            row.filter { selector -> SHIP_POINT < selector && selector < OVER_SIZE_POINT }.sum()
        }
        val overSizeBoardSum = board.sumOf { row ->
            row.filter { selector -> (OVER_SIZE_POINT + 4 * LOCKED_CELL_POINT) < selector }.sum()
        }
        return normalBoardSum + (overSizeBoardSum % OVER_SIZE_POINT)
    }

    private fun getShipMask(ship: Ship): Array<DoubleArray> {
        val shipMask = DoubleArray(ship.length) { SHIP_POINT }
        return if (ship.orientationVertical) {
            val verticalShipMask = Array(ship.length + 2) { DoubleArray(3) { LOCKED_CELL_POINT } }
            for (i in 1..shipMask.size)
                verticalShipMask[i][1] = shipMask[i - 1]
            verticalShipMask
        } else {
            val horizontalShipMask = Array(3) { DoubleArray(ship.length + 2) { LOCKED_CELL_POINT } }
            for (j in 1..shipMask.size)
                horizontalShipMask[1][j] = shipMask[j - 1]
            horizontalShipMask
        }
    }

    fun printMask(mask: Array<DoubleArray>) {
        for (i in 0..mask.lastIndex) {
            println()
            for (j in 0..mask[i].lastIndex)
                if ((mask[i][j] > 1) && (mask[i][j] < OVER_SIZE_BOARD))
                    print("\u001B[31m [${String.format("%.2f", mask[i][j])}] \u001B[0m")
                else if (mask[i][j] in 1.0..1.8)
                    print("\u001B[32m [${String.format("%.2f", mask[i][j])}] \u001B[0m")
                else if (mask[i][j] > (OVER_SIZE_POINT + 1))
                    print("\u001B[31m [${String.format("%.2f", mask[i][j])}] \u001B[0m")
                else print(" \u001B[37m ${String.format("%.2f", mask[i][j])}] \u001B[0m")
        }
        println()
        println("-----")
        println()
    }

    fun cropToNormalSize(oversize: Array<DoubleArray>): Array<DoubleArray> {
        val normalBoard = Array(boardSize) { DoubleArray(boardSize) }
        val lastIndex = oversize.size - OVER_SIZE_BOARD
        for ((cellCounter, i) in (1..lastIndex).withIndex()) {
            for ((rowCounter, j) in (1..lastIndex).withIndex()) {
                normalBoard[cellCounter][rowCounter] = oversize[i][j]
            }
        }
        return normalBoard
    }
}