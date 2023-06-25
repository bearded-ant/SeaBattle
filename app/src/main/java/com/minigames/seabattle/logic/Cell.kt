package com.minigames.seabattle.logic

import com.minigames.seabattle.algoritm.Ship

class Cell(var cellState: CellState) {
//    /**
//     * the states SHIP, HIT, SUNK don't allow a empty ship
//     */
//    init {
//        if (cellState in arrayOf(CellState.SHIP, CellState.HIT, CellState.SUNK) && ship == null)
//            cellState = CellState.ERROR
//    }
//
//    override fun toString(): String {
//        return "$cellState::$ship"
//    }
}