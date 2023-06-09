package com.minigames.seabattle.logic

import android.util.Log

const val TAG = "SeaBattleLog"

class BattleGround(
    var playerName: String,
    private val initAvailableShips: MutableMap<Int, Int>,
    private val tableSize: Int
) {
    var playerBoard: Array<Array<Cell>> = Array(tableSize) {
        Array(tableSize) {
            Cell(CellState.WATER, null)
        }
    } // board of player
    private var availableShips: MutableMap<Int, Int> = mutableMapOf() // available ships
    private var gameStartedFlag: Boolean = false // flag for game state (see startGame)

    init {
        startGame(false)
    }

    /**
     * @param gameState: false = init ships mode; true = game started
     * init ships mode
     * - playerBoard for game initialization
     * - availableShips for maximum ships count
     * game started
     * - playerBoard will now be the shooting board of the opponent
     * - reset availableShips for progress information
     */
    fun startGame(gameState: Boolean) {
        availableShips.putAll(initAvailableShips) // copy
        gameStartedFlag = gameState
        Log.d(TAG, "BattleGround@$playerName::startGame() game state changed to $gameStartedFlag")
    }

    /**
     * getter for gameStarted
     * @return gameStarted
     */
    fun getGameState(): Boolean {
        return gameStartedFlag
    }

    /**
     * @param ship: ship should get onto the playerBoard
     * @return true if game not started, ship is available, setShipChecks(), false if not added
     */
    fun setShip(ship: Ship): Boolean {
        Log.d(TAG, "BattleGround@$playerName::setShip() try to set ship onto playerBoard - $ship")
        if (!gameStartedFlag && availableShips.containsKey(ship.length) && availableShips[ship.length]!! > 0 && setShipChecks(
                ship
            )
        ) {
            availableShips[ship.length] = availableShips[ship.length]!! - 1
            return iterateOverShip(ship) { row, col ->
                playerBoard[row][col] = Cell(CellState.SHIP, ship)
                return@iterateOverShip true
            }
        }
        Log.d(
            TAG,
            "BattleGround@$playerName::setShip() ship not added (game started OR ship unavailable OR setShipChecks() failed)"
        )
        return false
    }

    /**
     * @param ship: check this ship
     * there cannot be 2 ships on one field
     * the ships must not collide with each other
     * @return true if the ship is allowed to get onto the playerBoard
     */
    fun setShipChecks(ship: Ship): Boolean {
        //Log.d(TAG, "BattleGround@$playerName::setShipChecks() checks if there is already a ship OR ships are colliding")
        return iterateOverShip(ship) { row, col ->
            var cell: Cell = playerBoard[row][col]
            if (cell.cellState == CellState.SHIP) {
                Log.d(
                    TAG,
                    "BattleGround@$playerName::setShipChecks() already a ship desired position - $ship"
                )
                return@iterateOverShip false
            }
            for (i in -1..1) {
                for (j in -1..1) {
                    if (!(i == 0 && j == 0)) {
                        var newI = row + i
                        var newJ = col + j
                        if (newI in 0 until tableSize && newJ in 0 until tableSize) {
                            cell = playerBoard[newI][newJ]
                            if (cell.cellState == CellState.SHIP) {
                                Log.d(
                                    TAG,
                                    "BattleGround@$playerName::setShipChecks() ship is colliding with ${cell.ship}"
                                )
                                return@iterateOverShip false
                            }
                        }
                    }
                }
            }
            //Log.d(TAG, "BattleGround@$playerName::setShipChecks() success")
            return@iterateOverShip true
        }
    }

    /**
     * @param ship: ship should get deleted at playerBoard
     * @return true if game not started, ship length is available; false if not removed
     */
    fun removeShip(ship: Ship): Boolean {
        Log.d(
            TAG,
            "BattleGround@$playerName::removeShip() try to remove ship from playerBoard - $ship"
        )
        if (!gameStartedFlag) {
            if (playerBoard[ship.startRow][ship.endRow].cellState == CellState.SHIP && availableShips.containsKey(
                    ship.length
                )
            ) {
                availableShips[ship.length] = availableShips[ship.length]!! + 1
            }
            return iterateOverShip(ship) { row, col ->
                playerBoard[row][col] = Cell(CellState.WATER, null)
                return@iterateOverShip true
            }
        }
        Log.d(
            TAG,
            "BattleGround@$playerName::removeShip() ship not removed (game started OR ship length was never available"
        )
        return false
    }

    /**
     * @return available ships
     */
    fun getAvailableShips(): MutableMap<Int, Int> {
        return availableShips
    }

    /**
     * @return available ships count
     */
    fun getAvailableShipsCount(): Int {
        return availableShips.values.sum()
    }

    /**
     * @param row: row to shoot
     * @param col: col to shoot
     * @return HIT (ship hit); SUNK (ship sunk); MISS (no ship hit); ERROR(game not started); current Cell if already HIT, SUNK, MISS
     */
    fun shoot(row: Int, col: Int): Cell {
        Log.d(TAG, "BattleGround@$playerName::shoot() shoot on $row/$col")
        if (gameStartedFlag) {
            when (playerBoard[row][col].cellState) {
                CellState.SHIP -> {
                    val cell = hitShip(row, col)
                    playerBoard[row][col] = cell
                    Log.d(TAG, "BattleGround@$playerName::shoot() ${cell.cellState}")
                    return cell
                }

                CellState.WATER -> {
                    playerBoard[row][col] = Cell(CellState.MISS, null)
                    Log.d(TAG, "BattleGround@$playerName::shoot() ${CellState.MISS}")
                    return Cell(CellState.MISS, null)
                }

                else -> {
                    val cell = playerBoard[row][col]
                    Log.d(
                        TAG,
                        "BattleGround@$playerName::shoot() shooting on this cell not possible ${cell.cellState}"
                    )
                    return cell
                }
            }
        }
        Log.d(TAG, "BattleGround@$playerName::shoot() ${CellState.ERROR} (game not started)")
        return Cell(CellState.ERROR, null)
    }

    /**
     * decrement undestroyedCount of ship
     * if undestroyedCount is 0 - ship is set to be SUNK
     * otherwise ship is HIT
     */
    private fun hitShip(row: Int, col: Int): Cell {
        var cell = playerBoard[row][col]
        var ship = cell.ship!!
        ship.undestroyedCount--
        cell.cellState = CellState.HIT
        if (ship.undestroyedCount <= 0) {
            Log.d(TAG, "BattleGround@$playerName::hitShip() ship destroyed - $ship")
            iterateOverShip(ship) { x, y ->
                playerBoard[x][y] = Cell(CellState.SUNK, ship)
                return@iterateOverShip true
            }
            ship.isSunk = true
            cell.cellState = CellState.SUNK
            availableShips[ship.length] = availableShips[ship.length]!! - 1
        }
        return cell
    }

    /**
     * @param ship: iterating ship
     * @param function: { row, col -> **to something with row and col of ship cell** return true / false(stops loop and returns false) }
     * start and other start are based on the ship's direction
     * @return false if start is bigger or equals as end OR if other start is not equals than other end (would be diagonal)
     */
    private fun iterateOverShip(ship: Ship, function: (Int, Int) -> Boolean): Boolean {
        if (ship.direction == Direction.HORIZONTAL) {
            if (ship.startCol >= ship.endCol || ship.startRow != ship.endRow) {
                Log.d(
                    TAG,
                    "BattleGround@$playerName::iterateOverShip() startCol >= endCol OR startRow != endRow - $ship"
                )
                return false
            }
            for (i in ship.startCol..ship.endCol) {
                if (!function(ship.startRow, i))
                    return false
            }
        } else if (ship.direction == Direction.VERTICAL) {
            if (ship.startRow >= ship.endRow || ship.startCol != ship.endCol) {
                Log.d(
                    TAG,
                    "BattleGround@$playerName::iterateOverShip() startRow >= endRow OR startCol != endCol - $ship"
                )
                return false
            }
            for (i in ship.startRow..ship.endRow) {
                if (!function(i, ship.startCol))
                    return false
            }
        }
        return true
    }

    fun generateShips(availableShips: MutableMap<Int, Int>, playerBoard: Array<Array<Cell>>) {
        //для всех доступных моделей кораблей
        for (ship in availableShips) {
            //для всех кораблей этой модели
            for (i in 0..ship.value) {
                //взять размер корабля и поместить его на поле
                //выбрать рандомное направление
                //выбрать ранодомный старт
                // если все норм - разместить, если нет повторить выбор

                val direction = (0..1).random()
                val start: Pair<Int, Int> = Pair((0..10).random(), (0..10).random())
            }
        }
    }

    private fun spaceToPair(space: Int): Pair<Int, Int> {
        val row = space / 10
        val col = space % 10
        return Pair(row, col)
    }

    private fun pairToSpace(pair: Pair<Int, Int>) = pair.first * 10 + pair.second

    private fun availableCells(playerBoard: Array<Array<Cell>>) =
        squareArray((playerBoard.indices).toList()).toMutableList()


    private fun squareArray(array: List<Int>): List<Pair<Int, Int>> {
        return array.flatMap { i -> array.map { j -> Pair(i, j) } }
    }
}