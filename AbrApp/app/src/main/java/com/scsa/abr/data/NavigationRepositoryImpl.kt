package com.scsa.abr.data

import com.scsa.abr.domain.model.NavigationAlgorithmType
import com.scsa.abr.domain.model.NavigationState
import com.scsa.abr.domain.navigation.algorithm.NavigationAlgorithmFactory
import com.scsa.abr.domain.navigation.executor.NavigationExecutor
import com.scsa.abr.domain.navigation.NavigationStateMachine
import com.scsa.abr.domain.repository.NavigationRepository
import javax.inject.Inject

class NavigationRepositoryImpl @Inject constructor(
    private val executor: NavigationExecutor
): NavigationRepository {
    private var machine: NavigationStateMachine? = null

    override fun startNavigation(navigationAlgorithmType: NavigationAlgorithmType) {
        val algorithm = NavigationAlgorithmFactory.createAlgorithm(navigationAlgorithmType)
        machine = NavigationStateMachine(algorithm, executor)
        machine?.start()
    }

    override fun stopNavigation() {
        machine?.stop()
        machine = null
    }

    override fun getNavigationState(): NavigationState {
        return machine?.getState() ?: NavigationState.IDLE
    }
}