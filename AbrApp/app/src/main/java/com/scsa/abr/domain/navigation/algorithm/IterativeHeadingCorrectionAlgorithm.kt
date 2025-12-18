package com.scsa.abr.domain.navigation.algorithm

import com.scsa.abr.domain.model.NavigationMove

class IterativeHeadingCorrectionAlgorithm : NavigationAlgorithm {

    override fun getInitialMove(): NavigationMove {
        TODO("Not yet implemented")
    }

    override fun getNextMove(
        oldDistance: Double,
        curDistance: Double,
        lastRotationDegree: Int
    ): NavigationMove {
        TODO("Not yet implemented")
    }

    override fun checkArrival(curDistance: Double): Boolean {
        TODO("Not yet implemented")
    }

}