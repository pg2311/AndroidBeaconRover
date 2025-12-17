package com.scsa.abr.domain.repository

import com.scsa.abr.domain.model.NavigationAlgorithmType
import com.scsa.abr.domain.model.NavigationMove
import com.scsa.abr.domain.model.NavigationState
import kotlinx.coroutines.flow.Flow

interface NavigationRepository {
    val navigationState: Flow<NavigationState>
    val isArrived: Flow<Boolean>
    val lastMove: Flow<NavigationMove>

    suspend fun startNavigation(navigationAlgorithmType: NavigationAlgorithmType)
    suspend fun stopNavigation()
}