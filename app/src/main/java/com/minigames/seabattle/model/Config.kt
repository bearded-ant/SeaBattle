package com.minigames.seabattle.model

data class Config(
    val SHIP_MAX_SIZE: Int = 4,
    val OVER_SIZE_BOARD: Int = SHIP_MAX_SIZE + 2,
    val OVER_SIZE_POINT: Double = 1000.0,
    val LOCKED_CELL_POINT: Double = 0.2,
    val SHIP_POINT: Double = 1.0,
)
