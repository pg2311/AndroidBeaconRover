package com.scsa.abr.domain.navigation.algorithm

import com.scsa.abr.domain.model.NavigationMove

interface NavigationAlgorithm {
    fun getNextMove(): NavigationMove
    fun checkArrival(): Boolean
}