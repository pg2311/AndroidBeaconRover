package com.scsa.abr.domain.navigation.algorithm

import android.util.Log
import com.scsa.abr.domain.model.NavigationDirection
import com.scsa.abr.domain.model.NavigationMove

class GradientDescentAlgorithm: NavigationAlgorithm{

    private val TAG = "GradientDescentAlgorithm"
    private val threshold = 0.9

    override fun getInitialMove(): NavigationMove {
        return NavigationMove(NavigationDirection.FORWARD, 2)
    }

    override fun getNextMove(oldDistance: Double, curDistance: Double, lastRotationDegree: Int): NavigationMove {
        Log.i(TAG, "$oldDistance -> $curDistance")
        if (oldDistance > curDistance) {
            return NavigationMove(NavigationDirection.FORWARD, 2)
        } else {
            if (lastRotationDegree == 0) {
                return NavigationMove(NavigationDirection.RIGHT, 90)
            } else {
                return NavigationMove(NavigationDirection.FORWARD, 2)
            }
        }
    }

    override fun checkArrival(curDistance: Double): Boolean {
        if (curDistance < threshold) {
            Log.i(TAG, "CheckArrival: TRUE: %.2f< ${threshold}m".format(curDistance))
            return true
        } else {
            Log.i(TAG, "CheckArrival: FALSE: %.2f> ${threshold}m".format(curDistance))
            return false
        }
    }
}