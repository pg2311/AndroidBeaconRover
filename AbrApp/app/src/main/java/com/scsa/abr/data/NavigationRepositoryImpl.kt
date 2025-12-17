package com.scsa.abr.data

import com.scsa.abr.domain.model.NavigationAlgorithmType
import com.scsa.abr.domain.model.NavigationDirection
import com.scsa.abr.domain.model.NavigationMove
import com.scsa.abr.domain.model.NavigationState
import com.scsa.abr.domain.navigation.algorithm.NavigationAlgorithmFactory
import com.scsa.abr.domain.navigation.executor.NavigationExecutor
import com.scsa.abr.domain.navigation.NavigationStateMachine
import com.scsa.abr.domain.repository.NavigationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import javax.inject.Inject

class NavigationRepositoryImpl @Inject constructor(
    private val executor: NavigationExecutor
) : NavigationRepository {
    private var machine: NavigationStateMachine? = null

    private val _navigationState = MutableStateFlow(NavigationState.IDLE)
    override val navigationState = _navigationState.asStateFlow()

    private val _isArrived = MutableStateFlow(false)
    override val isArrived = _isArrived.asStateFlow()

    private val _lastMove = MutableStateFlow(NavigationMove(NavigationDirection.FORWARD, 100))
    override val lastMove: Flow<NavigationMove> = _lastMove.asStateFlow()

    override suspend fun startNavigation(navigationAlgorithmType: NavigationAlgorithmType) {
        val algorithm = NavigationAlgorithmFactory.createAlgorithm(navigationAlgorithmType)
        machine = NavigationStateMachine(
            algorithm,
            executor,
            onNavigationStateChange = ::onNavigationStateChange,
            onIsArrivedChange = ::onIsArrivedChange,
            onLastMoveChange = ::onLastMoveChange
        )

        _isArrived.value = false
        withContext(Dispatchers.Default) {
            machine?.start()
        }
    }

    override suspend fun stopNavigation() {
        machine?.stop()
        machine = null
    }

    private fun onNavigationStateChange(newState: NavigationState) {
        _navigationState.update { it -> newState }
    }

    private fun onIsArrivedChange(isArrived: Boolean) {
        _isArrived.update { it -> isArrived }
    }

    private fun onLastMoveChange(lastMove: NavigationMove) {
        _lastMove.update { it -> lastMove }
    }
}