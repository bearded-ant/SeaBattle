package com.minigames.seabattle.logic

import com.minigames.seabattle.model.Direction

class ShipOld(
    val startRow: Int,
    val startCol: Int,
    var endRow: Int,
    var endCol: Int,
    var direction: Direction
) {
    var length: Int =
        if (direction == Direction.HORIZONTAL) endRow - startRow else endCol - startCol
    var isSunk: Boolean = false
    var undestroyedCount = length

    override fun toString(): String {
        return "$startRow/$startCol -> $endRow/$endCol (Dir: $direction; Len: $length; Sunk: $isSunk; Undestroyed: $undestroyedCount)"
    }
}