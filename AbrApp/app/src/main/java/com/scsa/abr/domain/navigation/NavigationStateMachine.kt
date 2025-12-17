package com.scsa.abr.domain.navigation

import android.util.Log
import com.scsa.abr.domain.model.NavigationDirection
import com.scsa.abr.domain.model.NavigationMove
import com.scsa.abr.domain.model.NavigationState
import com.scsa.abr.domain.navigation.algorithm.NavigationAlgorithm
import com.scsa.abr.domain.navigation.executor.NavigationExecutor
import kotlinx.coroutines.delay

class NavigationStateMachine(
    private val algorithm: NavigationAlgorithm,
    private val executor: NavigationExecutor,
    private val onNavigationStateChange: (NavigationState) -> Unit,
    private val onIsArrivedChange: (Boolean) -> Unit,
    private val onLastMoveChange: (NavigationMove) -> Unit
) {
    private val TAG = "NavigationStateMachine"
    private var state: NavigationState = NavigationState.IDLE
        set(value) {
            field = value
            onNavigationStateChange(value)
        }

    private var currentMove: NavigationMove? = null
    private var isRunning = false

    suspend fun start() {
        isRunning = true
        state = NavigationState.GATHERING
        runControlLoop()
    }

    private suspend fun runControlLoop() {
        while (isRunning) {
            Log.i(TAG, "state: $state")
            when (state) {
                NavigationState.IDLE -> {}

                NavigationState.GATHERING -> {
                    executor.gatherData()
                    state = NavigationState.CALCULATING
                }

                NavigationState.CALCULATING -> {
                    delay(5 * 1000)
                    if (algorithm.checkArrival()) {
                        Log.i(TAG, "arrived")
                        onIsArrivedChange(true)
                        stop()
                    } else {
                        currentMove = algorithm.getNextMove()
                        onLastMoveChange(currentMove!!)

                        if (currentMove?.direction == NavigationDirection.FORWARD
                            || currentMove?.direction == NavigationDirection.BACKWARD) {
                            state = NavigationState.MOVING
                        } else {
                            state = NavigationState.TURNING
                        }
                    }
                }

                NavigationState.TURNING, NavigationState.MOVING -> {
                    if (currentMove == null) {
                        Log.e(TAG, "currentMove is null")
                        state = NavigationState.GATHERING
                    } else {
                        executor.executeMove(currentMove!!)
                        state = NavigationState.GATHERING
                    }
                }
            }
        }
        state = NavigationState.IDLE
    }

    fun stop() {
        isRunning = false
        executor.stop()
        state = NavigationState.IDLE
    }

    fun getState(): NavigationState = state
}