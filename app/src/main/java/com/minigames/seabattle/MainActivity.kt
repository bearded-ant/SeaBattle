package com.minigames.seabattle

import android.animation.ObjectAnimator
import android.app.Activity
import android.graphics.Insets
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import android.view.WindowInsets
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import android.widget.TableRow
import androidx.appcompat.content.res.AppCompatResources
import com.minigames.seabattle.algoritm.GeneticAlgorithms
import com.minigames.seabattle.model.Ship
import com.minigames.seabattle.algoritm.ShipsGenerator
import com.minigames.seabattle.databinding.ActivityMainBinding
import com.minigames.seabattle.logic.BattleGround
import com.minigames.seabattle.model.Cell
import com.minigames.seabattle.model.CellState
import java.util.SortedMap

const val BOARD_SIZE = 10
const val POPULATION_SIZE = 200
val playShips = listOf(4, 3, 3, 2, 2, 2, 1, 1, 1, 1)

class MainActivity : Activity() {
    private lateinit var binding: ActivityMainBinding

    var rectSize: Int = 0
    var tableSize: Int = 10

    private val generator = ShipsGenerator(BOARD_SIZE)
    private val geneticMutator = GeneticAlgorithms(BOARD_SIZE)
    private val battleGround = BattleGround("Player", BOARD_SIZE)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        rectSize = (getScreenWidth() - 180) / tableSize

        var population: SortedMap<Double, List<Ship>> =
            createPopulation(playShips, POPULATION_SIZE)

        var generationCount = 0

        while (!population.containsKey(0.0)) {
            val bestHalf = selectBestHalf(population)
            val afterMutation = mutation(bestHalf)
            population = createPopulation(playShips, POPULATION_SIZE - afterMutation.size)
            population.putAll(afterMutation)
            generationCount++
        }
        val winner = population.getValue(0.0)

        val board = generator.cropToNormalSize(generator.placeShipsOnBoard(winner))
        battleGround.setShip(winner, board)

        drawBoard(winner)

    }

//        --------------------------------

    private fun getScreenWidth(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val windowMetrics = windowManager.currentWindowMetrics
            val insets: Insets = windowMetrics.windowInsets
                .getInsetsIgnoringVisibility(WindowInsets.Type.systemBars())
            windowMetrics.bounds.width() - insets.left - insets.right
        } else {
            val displayMetrics = DisplayMetrics()
            windowManager.defaultDisplay.getMetrics(displayMetrics)
            displayMetrics.widthPixels
        }
    }

    private fun drawBoard(ships: List<Ship>) {
        //Log.d(TAG, "MainActivity@${currentBattleGround.playerName}::drawBoard()")
//            displayAvailableShips()
        binding.activityMainTblBotBoard.removeAllViews()
//            binding.activityMainTvCurrentPlayer.text = currentBattleGround.playerName
        val layoutParams = TableRow.LayoutParams(rectSize, rectSize)
        for (i in 0 until tableSize) {
            val tblRow = TableRow(this)
            for (j in 0 until tableSize) {
                tblRow.addView(cell2View(i, j), layoutParams)
            }
            binding.activityMainTblBotBoard.addView(tblRow)
        }
    }

    private fun cell2View(row: Int, col: Int): ImageView {
        val cell: Cell = battleGround.playerBoard[row][col]
        val cellState2Color = initCellState2Color()
//        val imageView: ImageView = ImageView(this)
////        imageView.setImageDrawable(getDrawable(R.drawable.temcell))
//
//        imageView.setOnClickListener {
//            Toast.makeText(this, "$row :: $col", Toast.LENGTH_SHORT).show()
//            shakeAnimation(imageView)
//        }
//
//        return imageView

        return ImageView(this).apply {
            // drawable_cellstate = default drawable (border already set)
            // cellState2Color = CellState to color map
            setImageDrawable(
                (AppCompatResources.getDrawable(
                    context,
                    R.drawable.drawable_cellstate
                ) as GradientDrawable).apply {
                    cellState2Color[cell.cellState]?.let { setColor(it) }
                })
                setOnClickListener {
//                    when (cell.cellState) {
//                        CellState.SHIP -> cellStateShipClicked(row, col)
//                        CellState.WATER -> cellStateWaterClicked(row, col)
//                        CellState.ERROR -> cellStateErrorClicked()
//                        else -> Log.d(TAG, "cell2View: error click")
//                    }
                }
        }
    }

    private fun shakeAnimation(view: View) {
        val animator = ObjectAnimator.ofFloat(view, "rotation", 0f, -10f, 10f, 0f)
        animator.interpolator = AccelerateDecelerateInterpolator()
        animator.duration = 100
        animator.start()
    }


    //todo вынести  генератор популяции в класс генетических аглгоритмов
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

    private fun mutation(bestHalf: MutableMap<Double, List<Ship>>): SortedMap<Double, List<Ship>> {
        val mutateHalf = mutableMapOf<Double, List<Ship>>()
        val keyList = bestHalf.keys.toList()

        for (i in 0 until keyList.lastIndex step 2) {
            val mather = bestHalf.getValue(keyList[i])
            val father = bestHalf.getValue(keyList[i + 1])

            val mutatePair =
                geneticMutator.kPointCrossing(4, mather.toMutableList(), father.toMutableList())

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

    private fun initCellState2Color() =
        mutableMapOf(
            CellState.SHIP to getColor(R.color.color_cellstate_ship),
            CellState.SUNK to getColor(R.color.color_cellstate_sunk),
            CellState.HIT to getColor(R.color.color_cellstate_hit),
            CellState.MISS to getColor(R.color.color_cellstate_miss),
            CellState.WATER to getColor(R.color.color_cellstate_water),
            CellState.ERROR to getColor(R.color.color_cellstate_error),
            CellState.SELECTED to getColor(R.color.color_cellstate_selected)
        )
}