package com.scsa.abr.domain.navigation.algorithm

import android.util.Log
import com.scsa.abr.domain.model.NavigationDirection
import com.scsa.abr.domain.model.NavigationMove

private const val TAG = "GradientDescentAlgorithm"
private const val THRESHOLD = 0.7

class GradientDescentAlgorithm : NavigationAlgorithm {


    override fun getInitialMove(): NavigationMove {
        return NavigationMove(NavigationDirection.FORWARD, 3)
    }

    override fun getNextMove(
        oldDistance: Double,
        curDistance: Double,
        lastRotationDegree: Int
    ): NavigationMove {
        Log.i(TAG, "$oldDistance -> $curDistance")
        if (oldDistance > curDistance) {
            return NavigationMove(NavigationDirection.FORWARD, 3)
        } else {
            if (lastRotationDegree == 0) {
                return NavigationMove(NavigationDirection.RIGHT, 90)
            } else {
                return NavigationMove(NavigationDirection.FORWARD, 3)
            }
        }
    }

    override fun checkArrival(curDistance: Double): Boolean {
        if (curDistance < THRESHOLD) {
            Log.i(TAG, "CheckArrival: TRUE: %.2f< ${THRESHOLD}m".format(curDistance))
            return true
        } else {
            Log.i(TAG, "CheckArrival: FALSE: %.2f> ${THRESHOLD}m".format(curDistance))
            return false
        }
    }
}