package com.scsa.abr.domain.navigation

import android.util.Log
import com.scsa.abr.domain.model.NavigationDirection
import com.scsa.abr.domain.model.NavigationMove
import com.scsa.abr.domain.model.NavigationState
import com.scsa.abr.domain.navigation.algorithm.NavigationAlgorithm
import com.scsa.abr.domain.navigation.executor.NavigationExecutor

class NavigationStateMachine (
    private val algorithm: NavigationAlgorithm,
    private val executor: NavigationExecutor
) {
    private val TAG = "NavigationStateMachine"
    private var state: NavigationState = NavigationState.IDLE

    fun start() {
        state = NavigationState.GATHERING
        loop()
    }

    fun loop(move: NavigationMove? = null) {
        when (state) {
            NavigationState.IDLE -> {}

            NavigationState.GATHERING -> {
                executor.gatherData()
                state = NavigationState.CALCULATING
                loop()
            }

            NavigationState.CALCULATING -> {
                if (algorithm.checkArrival()) {
                    state = NavigationState.IDLE
                    executor.stop()
                } else {
                    val move = algorithm.getNextMove()
                    if (move.direction == NavigationDirection.FORWARD
                        || move.direction == NavigationDirection.BACKWARD
                    ) {
                        state = NavigationState.MOVING
                    } else {
                        state = NavigationState.TURNING
                    }
                    loop()
                }
            }

            NavigationState.TURNING -> {
                if (move == null)  {
                    Log.e(TAG, "TURNING: move is null")
                } else {
                    executor.executeMove(move)
                }
                state = NavigationState.GATHERING
                loop()
            }

            NavigationState.MOVING -> {
                if (move == null)  {
                    Log.e(TAG, "MOVING: move is null")
                } else {
                    executor.executeMove(move)
                }
                state = NavigationState.GATHERING
                loop()
            }
        }
    }

    fun stop() {
        executor.stop()
        state = NavigationState.IDLE
    }

    fun getState(): NavigationState = state

}