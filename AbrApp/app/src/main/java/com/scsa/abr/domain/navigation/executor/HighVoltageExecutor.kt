package com.scsa.abr.domain.navigation.executor

import com.scsa.abr.domain.model.NavigationDirection
import com.scsa.abr.domain.model.NavigationMove
import com.scsa.abr.domain.repository.BeaconRepository
import com.scsa.abr.domain.repository.BleGattRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HighVoltageExecutor @Inject constructor(
    private val beaconRepository: BeaconRepository,
    private val bleGattRepository: BleGattRepository
) : NavigationExecutor {
    override suspend fun executeMove(move: NavigationMove) {
        when (move.direction) {
            NavigationDirection.FORWARD -> moveForward(move.amount)
            NavigationDirection.BACKWARD -> moveBackward(move.amount)
            NavigationDirection.LEFT -> rotateLeft(move.amount)
            NavigationDirection.RIGHT -> rotateRight(move.amount)
        }
    }

    override suspend fun stop() {
        TODO("Not yet implemented")
    }

    override suspend fun measureDistance(): Double {
        return 0.0
    }

    private fun moveForward(amount: Int) {}
    private fun moveBackward(amount: Int) {}
    private fun rotateLeft(amount: Int) {}
    private fun rotateRight(amount: Int) {}
}