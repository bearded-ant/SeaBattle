package com.minigames.seabattle

import android.animation.ObjectAnimator
import android.app.Activity
import android.graphics.Insets
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.WindowInsets
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import android.widget.TableRow
import android.widget.Toast
import com.minigames.seabattle.databinding.ActivityMainBinding

class MainActivity : Activity() {
    private lateinit var binding: ActivityMainBinding

    var rectSize: Int = 0
    var tableSize: Int = 10
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        rectSize = (getScreenWidth() - 180) / tableSize
        Log.d("TAG", "onCreate: ${getScreenWidth()}")
        drawBoard()


    }

    fun getScreenWidth(): Int {
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

    private fun drawBoard() {
        //Log.d(TAG, "MainActivity@${currentBattleGround.playerName}::drawBoard()")
//            displayAvailableShips()
        binding.activityMainTblBoard.removeAllViews()
//            binding.activityMainTvCurrentPlayer.text = currentBattleGround.playerName
        val layoutParams = TableRow.LayoutParams(rectSize, rectSize)
        for (i in 0 until tableSize) {
            var tblRow = TableRow(this)
            for (j in 0 until tableSize) {
                tblRow.addView(cell2View(i, j), layoutParams)
            }
            binding.activityMainTblBoard.addView(tblRow)
        }
    }

    private fun cell2View(row: Int, col: Int): ImageView {
//            val cell: Cell = currentBattleGround.playerBoard[row][col]

        val imageView: ImageView = ImageView(this)
        imageView.setImageDrawable(getDrawable(R.drawable.temcell))
        imageView.setOnClickListener {
            Toast.makeText(this, "$row :: $col", Toast.LENGTH_SHORT).show()
//            shakeImageView(imageView)
            shakeAnimation(imageView)
        }

        return imageView

//            return ImageView(this).apply {
//                // drawable_cellstate = default drawable (border already set)
//                // cellState2Color = CellState to color map
//                setImageDrawable((getDrawable(R.drawable.drawable_cellstate) as GradientDrawable).apply {
//                    cellState2Color[cell.cellState]?.let { setColor(it) }
//                })
//                setOnClickListener {
//                    when (cell.cellState) {
//                        CellState.SHIP -> cellStateShipClicked(row, col)
//                        CellState.WATER -> cellStateWaterClicked(row, col)
//                        CellState.ERROR -> cellStateErrorClicked()
//                        else -> Log.d(TAG, "cell2View: error click")
//                    }
//                }
//            }
    }

    fun shakeImageView(imageView: ImageView) {
        val shakeDuration = 150L // Длительность вздрагивания в миллисекундах
        val shakeAngle = 15f // Угол вздрагивания в градусах
        val shakeRepeatCount = 1 // Количество повторений вздрагивания

        val shakeAnimator = ObjectAnimator.ofFloat(imageView, "rotation", shakeAngle, -shakeAngle)
        shakeAnimator.interpolator = AccelerateDecelerateInterpolator()
        shakeAnimator.duration = shakeDuration
        shakeAnimator.repeatCount = shakeRepeatCount
        shakeAnimator.start()
    }

    fun shakeAnimation(view: View) {
        val animator = ObjectAnimator.ofFloat(view, "rotation", 0f, -10f, 10f, 0f)
        animator.interpolator = AccelerateDecelerateInterpolator()
        animator.duration = 100
        animator.start()
    }
}
