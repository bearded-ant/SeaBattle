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
import com.minigames.seabattle.databinding.ActivityMainBinding
import com.minigames.seabattle.logic.BattleGround
import com.minigames.seabattle.model.Cell
import com.minigames.seabattle.model.CellState
import com.minigames.seabattle.model.Config
import com.minigames.seabattle.model.Ship


class MainActivity : Activity() {
    private lateinit var binding: ActivityMainBinding
    private val config: Config = Config()
    private val battleGround = BattleGround("Player", config.BOARD_SIZE)
    private val rectSize = (getScreenWidth() - 180) / config.BOARD_SIZE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val winner = ships()

        val board = generator.cropToNormalSize(generator.placeShipsOnBoard(winner))
        battleGround.setShip(winner, board)

        drawBoard(winner)

    }

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
        for (i in 0 until config.BOARD_SIZE) {
            val tblRow = TableRow(this)
            for (j in 0 until config.BOARD_SIZE) {
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