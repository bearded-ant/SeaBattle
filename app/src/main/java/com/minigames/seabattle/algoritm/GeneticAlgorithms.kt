package com.minigames.seabattle.algoritm

import com.minigames.seabattle.model.Config
import com.minigames.seabattle.model.Ship
import java.util.SortedMap
import kotlin.random.Random

class GeneticAlgorithms(private val boardSize: Int) {
    private val generator: ShipsGenerator = ShipsGenerator(Config().BOARD_SIZE)
    private val config: Config = Config()

    fun simpleMutation(ships: List<Ship>): List<Ship> {
        val mutateShipIndex = ships.indices.random()
        val mutateShips = ships.toMutableList()

        if ((0..100).random() > 90) {
            if (Random.nextBoolean() == mutateShips[mutateShipIndex].orientationVertical) {
                mutateShips[mutateShipIndex].rowStart = (1..boardSize).random()
                mutateShips[mutateShipIndex].columnStart = (1..boardSize).random()
            } else
                mutateShips[mutateShipIndex].orientationVertical =
                    !mutateShips[mutateShipIndex].orientationVertical
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

    private fun kPointCrossing(
        crossPointsCount: Int,
        mather: MutableList<Ship>,
        father: MutableList<Ship>
    ): Pair<List<Ship>, List<Ship>> {

        val sortedCrossPoints = generateCrossPoints(crossPointsCount, mather.lastIndex)
        for (i in 0..sortedCrossPoints.lastIndex step 2) {

            val subMather = mather.subList(sortedCrossPoints[i], sortedCrossPoints[i + 1])
            val subFatherCopy =
                father.subList(sortedCrossPoints[i], sortedCrossPoints[i + 1]).toMutableList()

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

    private fun createPopulation(
        playShips: List<Int>,
        populationSize: Int
    ): SortedMap<Double, List<Ship>> {

        var fitnessSum = Double.MAX_VALUE
        val population = sortedMapOf<Double, List<Ship>>()

        while (population.size < populationSize && fitnessSum != 0.0) {
            val ships = generator.initShips(playShips)

            val newBoard = generator.placeShipsOnBoard(ships)
            fitnessSum = generator.getBoardPointsSum(newBoard)

            population[fitnessSum] = ships
        }
        return population
    }

    private fun crossingMutation(bestHalf: MutableMap<Double, List<Ship>>): SortedMap<Double, List<Ship>> {
        val mutateHalf = mutableMapOf<Double, List<Ship>>()
        val keyList = bestHalf.keys.toList()

        for (i in 0 until keyList.lastIndex step 2) {
            val mather = bestHalf.getValue(keyList[i])
            val father = bestHalf.getValue(keyList[i + 1])

            val mutatePair =
                kPointCrossing(config.crossPoints, mather.toMutableList(), father.toMutableList())

            for (element in mutatePair.toList()) {
                val newBoard = generator.placeShipsOnBoard(element)
                val fitnessSum = generator.getBoardPointsSum(newBoard)
                mutateHalf[fitnessSum] = element
                if (fitnessSum == 0.0)
                    return mutateHalf.toSortedMap()
            }
        }
        return mutateHalf.toSortedMap()
    }

    private fun selectBestHalf(population: SortedMap<Double, List<Ship>>): MutableMap<Double, List<Ship>> {
        var halfSize = population.size / 2
        halfSize = if (halfSize % 2 == 0) halfSize else halfSize + 1
        val keys = population.keys.toList()
        val fromKey = keys.first()
        val toKey = keys[halfSize]

        return population.subMap(fromKey, toKey)
    }

    private fun tournamentSelectionMutation(): List<Ship> {
        var population: SortedMap<Double, List<Ship>> =
            createPopulation(config.playShips, config.POPULATION_SIZE)

        var generationCount = 0

        while (!population.containsKey(0.0)) {
            val bestHalf = selectBestHalf(population)
            val afterMutation = crossingMutation(bestHalf)
            population =
                createPopulation(config.playShips, config.POPULATION_SIZE - afterMutation.size)
            population.putAll(afterMutation)
            generationCount++
        }

        return population.getValue(0.0)
    }
}
