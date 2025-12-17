package com.scsa.abr.domain.navigation

import android.util.Log
import com.scsa.abr.domain.model.NavigationDirection
import com.scsa.abr.domain.model.NavigationMove
import com.scsa.abr.domain.model.NavigationState
import com.scsa.abr.domain.navigation.algorithm.NavigationAlgorithm
import com.scsa.abr.domain.navigation.executor.NavigationExecutor

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
    private var lastRotationDegree = 0
    private val distanceDeque = ArrayDeque<Double>(2)

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
                    getAndSaveDistance()
                    state = NavigationState.CALCULATING
                }

                NavigationState.CALCULATING -> {
                    if (algorithm.checkArrival(distanceDeque.last())) {
                        Log.i(TAG, "arrived")
                        onIsArrivedChange(true)
                        stop()
                    } else {
                        currentMove = if (distanceDeque.size < 2) {
                            algorithm.getInitialMove()
                        } else {
                            algorithm.getNextMove(
                                distanceDeque.first(),
                                distanceDeque.last(),
                                lastRotationDegree
                            )
                        }
                        onLastMoveChange(currentMove!!)

                        if (currentMove?.direction == NavigationDirection.FORWARD
                            || currentMove?.direction == NavigationDirection.BACKWARD
                        ) {
                            state = NavigationState.MOVING
                        } else {
                            state = NavigationState.TURNING
                        }
                    }
                }

                NavigationState.TURNING -> {
                    if (currentMove == null) {
                        Log.e(TAG, "currentMove is null")
                        state = NavigationState.GATHERING
                    } else {
                        Log.i(TAG, "Turn: $currentMove")
                        executor.executeMove(currentMove!!)
                        lastRotationDegree = currentMove!!.amount
                        state = NavigationState.CALCULATING // go to next move
                    }
                }
                NavigationState.MOVING -> {
                    if (currentMove == null) {
                        Log.e(TAG, "currentMove is null")
                        state = NavigationState.GATHERING
                    } else {
                        Log.i(TAG, "Move: $currentMove")
                        executor.executeMove(currentMove!!)
                        lastRotationDegree = 0
                        state = NavigationState.GATHERING
                    }
                }
            }
        }
        state = NavigationState.IDLE
    }

    suspend fun stop() {
        isRunning = false
        executor.stop()
        state = NavigationState.IDLE
    }

    private suspend fun getAndSaveDistance() {
        val distance = executor.measureDistance()
        if (distanceDeque.size >= 2) {
            distanceDeque.removeFirst()
        }
        distanceDeque.addLast(distance)
    }
}