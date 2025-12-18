package com.scsa.abr.domain.navigation.algorithm

import com.scsa.abr.domain.model.NavigationMove

interface NavigationAlgorithm {
    fun getInitialMove(): NavigationMove
    fun getNextMove(
        oldDistance: Double,
        curDistance: Double,
        lastRotationDegree: Int
    ): NavigationMove

    fun checkArrival(curDistance: Double): Boolean
}