package com.scsa.abr.domain.repository

import com.scsa.abr.domain.model.NavigationAlgorithmType
import com.scsa.abr.domain.model.NavigationState

interface NavigationRepository {
    fun startNavigation(navigationAlgorithmType: NavigationAlgorithmType)
    fun stopNavigation()
    fun getNavigationState(): NavigationState
}