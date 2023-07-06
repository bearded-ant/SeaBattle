package com.minigames.seabattle.algoritm

import com.minigames.seabattle.model.Ship
import kotlin.random.Random

class GeneticAlgorithms(val boardSize: Int) {
    companion object {
        var mutCounter = 0
    }

    fun simpleMutation(ships: List<Ship>): List<Ship> {
        val mutateShipIndex = ships.indices.random()
//        val mutateShips = mutableListOf<Ship>()
//        mutateShips.addAll(ships)

        val mutateShips = ships.toMutableList()

        if ((0..100).random() > 90) {
            if (Random.nextBoolean() == mutateShips[mutateShipIndex].orientationVertical) {
                mutateShips[mutateShipIndex].rowStart = (1..boardSize).random()
                mutateShips[mutateShipIndex].columnStart = (1..boardSize).random()
            } else
                mutateShips[mutateShipIndex].orientationVertical = !mutateShips[mutateShipIndex].orientationVertical
            mutCounter++
        }
        return mutateShips
    }

    fun singlePointCrossing(mather: List<Ship>, father: List<Ship>): Pair<List<Ship>, List<Ship>> {
        val crossPoint = mather.indices.random()

        val subMather = mather.subList(crossPoint, mather.size).toMutableList()
        val son: MutableList<Ship> = father.subList(0, crossPoint).toMutableList()
        son.addAll(subMather)
        val subFather = father.subList(crossPoint, father.size).toMutableList()
        val daughter: MutableList<Ship> = mather.subList(0, crossPoint).toMutableList()
        daughter.addAll(subFather)

        return Pair(son, daughter)
    }

    fun kPointCrossing(k: Int, mather: MutableList<Ship>, father: MutableList<Ship>): Pair<List<Ship>, List<Ship>> {

        val sortedCrossPoints = generateCrossPoints(k, mather.lastIndex)
        for (i in 0..sortedCrossPoints.lastIndex step 2) {

            val subMather = mather.subList(sortedCrossPoints[i], sortedCrossPoints[i + 1])
            val subFatherCopy = father.subList(sortedCrossPoints[i], sortedCrossPoints[i + 1]).toMutableList()

            replaceElement(sortedCrossPoints[i], sortedCrossPoints[i + 1], father, subMather)
            replaceElement(sortedCrossPoints[i], sortedCrossPoints[i + 1], mather, subFatherCopy)
        }
        return Pair(mather, father)
    }

    private fun generateCrossPoints(k: Int, chromosomeLength: Int): IntArray {
        val crossPoint = mutableSetOf<Int>()
        while (crossPoint.size < k)
            crossPoint.add((0 until chromosomeLength).random())
        return crossPoint.toSortedSet().toIntArray()
    }

    private fun replaceElement(start: Int, end: Int, big: MutableList<Ship>, small: List<Ship>) {
        for ((iterator, i) in (start until end).withIndex()) {
            big[i] = small[iterator]
        }
    }
}
