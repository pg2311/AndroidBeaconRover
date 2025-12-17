package com.scsa.abr.domain.navigation.executor

import com.scsa.abr.domain.model.NavigationDirection
import com.scsa.abr.domain.model.NavigationMove
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeExecutor @Inject constructor(): NavigationExecutor {
    override suspend fun executeMove(move: NavigationMove) {
        when (move.direction) {
            NavigationDirection.FORWARD -> moveForward(move.amount)
            NavigationDirection.BACKWARD -> moveBackward(move.amount)
            NavigationDirection.LEFT -> rotateLeft(move.amount)
            NavigationDirection.RIGHT -> rotateRight(move.amount)
        }
    }

    override fun stop() {
    }

    override suspend fun gatherData() {
        delay(2 * 1000)
    }

    private suspend fun moveForward(amount: Int) {
        delay(2 * 1000)
    }

    private suspend fun moveBackward(amount: Int) {
        delay(2 * 1000)
    }

    private suspend fun rotateLeft(amount: Int) {
        delay(4 * 1000)
    }

    private suspend fun rotateRight(amount: Int) {
        delay(44441000)
    }
}