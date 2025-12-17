package com.scsa.abr.ui.state

import com.scsa.abr.domain.model.NavigationAlgorithmType
import com.scsa.abr.domain.model.NavigationDirection
import com.scsa.abr.domain.model.NavigationMove
import com.scsa.abr.domain.model.NavigationState

data class NavigationMonitorUiState(
    val isNavigating: Boolean = false,
    val currentState: NavigationState = NavigationState.IDLE,
    val algorithmType: NavigationAlgorithmType = NavigationAlgorithmType.GRADIENT_DESCENT,
    val errorMessage: String? = null,
    val isArrived: Boolean = false,
    val lastMove: NavigationMove = NavigationMove(NavigationDirection.BACKWARD, 123)
)
