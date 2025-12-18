package com.scsa.abr.domain.navigation.executor

import com.scsa.abr.domain.model.NavigationDirection
import com.scsa.abr.domain.model.NavigationMove
import com.scsa.abr.domain.repository.BeaconRepository
import com.scsa.abr.domain.repository.BleGattRepository
import com.scsa.abr.domain.usecase.SendMotorCommandUseCase
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeExecutor @Inject constructor(
    private val beaconRepository: BeaconRepository,
    private val bleGattRepository: BleGattRepository
): NavigationExecutor {

    override suspend fun executeMove(move: NavigationMove) {
        when (move.direction) {
            NavigationDirection.FORWARD -> moveForward(move.amount)
            NavigationDirection.BACKWARD -> moveBackward(move.amount)
            NavigationDirection.LEFT -> rotateLeft(move.amount)
            NavigationDirection.RIGHT -> rotateRight(move.amount)
        }
    }

    override suspend fun stop() {
    }

    override suspend fun measureDistance(): Double {
        return 1.0
    }

    private suspend fun moveForward(amount: Int) {
        delay(1000)
    }

    private suspend fun moveBackward(amount: Int) {
        delay(1000)
    }

    private suspend fun rotateLeft(amount: Int) {
        delay(1000)
    }

    private suspend fun rotateRight(amount: Int) {
        delay(1000)
    }
}