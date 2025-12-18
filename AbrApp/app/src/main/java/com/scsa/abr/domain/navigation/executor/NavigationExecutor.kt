package com.scsa.abr.domain.navigation.executor

import com.scsa.abr.domain.model.NavigationMove

interface NavigationExecutor {
    suspend fun executeMove(move: NavigationMove)
    suspend fun stop()
    suspend fun measureDistance(): Double
}