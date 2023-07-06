package com.minigames.seabattle.logic

import android.util.Log
import com.minigames.seabattle.model.Cell
import com.minigames.seabattle.model.CellState
import com.minigames.seabattle.model.Direction
import com.minigames.seabattle.model.Ship

const val TAG = "SeaBattleLog"

class BattleGround(
    var playerName: String,
//    private val initAvailableShips: MutableMap<Int, Int>,
    private val tableSize: Int
) {
    var playerBoard: Array<Array<Cell>> = Array(tableSize) {
        Array(tableSize) {
            Cell(CellState.WATER)
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
//        availableShips.putAll(initAvailableShips) // copy
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
    //     */

    fun setShip(ships: List<Ship>, board: Array<DoubleArray>): Boolean {
        if (!gameStartedFlag) {
            for (i in 0 .. board.lastIndex)
                for (j in 0 .. board[i].lastIndex)
                    if (board[i][j] == 1.0)
                        playerBoard[i][j] = Cell(CellState.SHIP)
        }
        return true
    }

//    fun setShip(shipOld: ShipOld): Boolean {
//        Log.d(
//            TAG,
//            "BattleGround@$playerName::setShip() try to set ship onto playerBoard - $shipOld"
//        )
//        if (!gameStartedFlag && availableShips.containsKey(shipOld.length) && availableShips[shipOld.length]!! > 0 && setShipChecks(
//                shipOld
//            )
//        ) {
//            availableShips[shipOld.length] = availableShips[shipOld.length]!! - 1
//            return iterateOverShip(shipOld) { row, col ->
//                playerBoard[row][col] = Cell(CellState.SHIP, shipOld)
//                return@iterateOverShip true
//            }
//        }
//        Log.d(
//            TAG,
//            "BattleGround@$playerName::setShip() ship not added (game started OR ship unavailable OR setShipChecks() failed)"
//        )
//        return false
//    }

    /**
     * @param shipOld: check this ship
     * there cannot be 2 ships on one field
     * the ships must not collide with each other
     * @return true if the ship is allowed to get onto the playerBoard
     */
//    fun setShipChecks(shipOld: ShipOld): Boolean {
//        //Log.d(TAG, "BattleGround@$playerName::setShipChecks() checks if there is already a ship OR ships are colliding")
//        return iterateOverShip(shipOld) { row, col ->
//            var cell: Cell = playerBoard[row][col]
//            if (cell.cellState == CellState.SHIP) {
//                Log.d(
//                    TAG,
//                    "BattleGround@$playerName::setShipChecks() already a ship desired position - $shipOld"
//                )
//                return@iterateOverShip false
//            }
//            for (i in -1..1) {
//                for (j in -1..1) {
//                    if (!(i == 0 && j == 0)) {
//                        var newI = row + i
//                        var newJ = col + j
//                        if (newI in 0 until tableSize && newJ in 0 until tableSize) {
//                            cell = playerBoard[newI][newJ]
//                            if (cell.cellState == CellState.SHIP) {
//                                Log.d(
//                                    TAG,
//                                    "BattleGround@$playerName::setShipChecks() ship is colliding with ${cell.shipOld}"
//                                )
//                                return@iterateOverShip false
//                            }
//                        }
//                    }
//                }
//            }
//            //Log.d(TAG, "BattleGround@$playerName::setShipChecks() success")
//            return@iterateOverShip true
//        }
//    }

    /**
     * @param shipOld: ship should get deleted at playerBoard
     * @return true if game not started, ship length is available; false if not removed
     */
    fun removeShip(shipOld: ShipOld): Boolean {
        Log.d(
            TAG,
            "BattleGround@$playerName::removeShip() try to remove ship from playerBoard - $shipOld"
        )
        if (!gameStartedFlag) {
            if (playerBoard[shipOld.startRow][shipOld.endRow].cellState == CellState.SHIP && availableShips.containsKey(
                    shipOld.length
                )
            ) {
                availableShips[shipOld.length] = availableShips[shipOld.length]!! + 1
            }
            return iterateOverShip(shipOld) { row, col ->
                playerBoard[row][col] = Cell(CellState.WATER)
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
//    fun shoot(row: Int, col: Int): Cell {
//        Log.d(TAG, "BattleGround@$playerName::shoot() shoot on $row/$col")
//        if (gameStartedFlag) {
//            when (playerBoard[row][col].cellState) {
//                CellState.SHIP -> {
//                    val cell = hitShip(row, col)
//                    playerBoard[row][col] = cell
//                    Log.d(TAG, "BattleGround@$playerName::shoot() ${cell.cellState}")
//                    return cell
//                }
//
//                CellState.WATER -> {
//                    playerBoard[row][col] = Cell(CellState.MISS, null)
//                    Log.d(TAG, "BattleGround@$playerName::shoot() ${CellState.MISS}")
//                    return Cell(CellState.MISS, null)
//                }
//
//                else -> {
//                    val cell = playerBoard[row][col]
//                    Log.d(
//                        TAG,
//                        "BattleGround@$playerName::shoot() shooting on this cell not possible ${cell.cellState}"
//                    )
//                    return cell
//                }
//            }
//        }
//        Log.d(TAG, "BattleGround@$playerName::shoot() ${CellState.ERROR} (game not started)")
//        return Cell(CellState.ERROR, null)
//    }

    /**
     * decrement undestroyedCount of ship
     * if undestroyedCount is 0 - ship is set to be SUNK
     * otherwise ship is HIT
     */
//    private fun hitShip(row: Int, col: Int): Cell {
//        var cell = playerBoard[row][col]
//        var ship = cell.ship!!
//        ship.undestroyedCount--
//        cell.cellState = CellState.HIT
//        if (ship.undestroyedCount <= 0) {
//            Log.d(TAG, "BattleGround@$playerName::hitShip() ship destroyed - $ship")
//            iterateOverShip(ship) { x, y ->
//                playerBoard[x][y] = Cell(CellState.SUNK, ship)
//                return@iterateOverShip true
//            }
//            ship.isSunk = true
//            cell.cellState = CellState.SUNK
//            availableShips[ship.length] = availableShips[ship.length]!! - 1
//        }
//        return cell
//    }

    /**
     * @param shipOld: iterating ship
     * @param function: { row, col -> **to something with row and col of ship cell** return true / false(stops loop and returns false) }
     * start and other start are based on the ship's direction
     * @return false if start is bigger or equals as end OR if other start is not equals than other end (would be diagonal)
     */
    private fun iterateOverShip(shipOld: ShipOld, function: (Int, Int) -> Boolean): Boolean {
        if (shipOld.direction == Direction.HORIZONTAL) {
            if (shipOld.startCol >= shipOld.endCol || shipOld.startRow != shipOld.endRow) {
                Log.d(
                    TAG,
                    "BattleGround@$playerName::iterateOverShip() startCol >= endCol OR startRow != endRow - $shipOld"
                )
                return false
            }
            for (i in shipOld.startCol..shipOld.endCol) {
                if (!function(shipOld.startRow, i))
                    return false
            }
        } else if (shipOld.direction == Direction.VERTICAL) {
            if (shipOld.startRow >= shipOld.endRow || shipOld.startCol != shipOld.endCol) {
                Log.d(
                    TAG,
                    "BattleGround@$playerName::iterateOverShip() startRow >= endRow OR startCol != endCol - $shipOld"
                )
                return false
            }
            for (i in shipOld.startRow..shipOld.endRow) {
                if (!function(i, shipOld.startCol))
                    return false
            }
        }
        return true
    }

    private fun availableCells(playerBoard: Array<Array<Cell>>) =
        squareArray((playerBoard.indices).toList()).toMutableList()


    private fun squareArray(array: List<Int>): List<Pair<Int, Int>> {
        return array.flatMap { i -> array.map { j -> Pair(i, j) } }
    }
}