package com.scsa.abr.domain.navigation.algorithm

import android.util.Log
import com.scsa.abr.domain.model.NavigationDirection
import com.scsa.abr.domain.model.NavigationMove
import kotlin.math.roundToInt

class GradientDescentAlgorithm: NavigationAlgorithm{

    private val TAG = "GradientDescentAlgorithm"

    override fun getNextMove(): NavigationMove {
        Log.i(TAG, "getNextMove")
        val nextMove = when ((Math.random() * 4).roundToInt()) {
            0 -> NavigationMove(
                NavigationDirection.FORWARD,
                1
            )
            1 -> NavigationMove(
                NavigationDirection.LEFT,
                2
            )
            2 -> NavigationMove(
                NavigationDirection.LEFT,
                3
            )
            3 -> NavigationMove(
                NavigationDirection.LEFT,
                4
            )
            else -> NavigationMove(
                NavigationDirection.RIGHT,
                5
            )
        }
        return nextMove
    }

    override fun checkArrival(): Boolean {
        Log.i(TAG, "checkArrival")
        return false;
    }
}