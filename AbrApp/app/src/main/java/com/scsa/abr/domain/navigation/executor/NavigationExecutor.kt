package com.scsa.abr.domain.navigation.executor

import com.scsa.abr.domain.model.NavigationMove

interface NavigationExecutor {
    fun executeMove(move: NavigationMove)
    fun stop()
    fun gatherData()
}