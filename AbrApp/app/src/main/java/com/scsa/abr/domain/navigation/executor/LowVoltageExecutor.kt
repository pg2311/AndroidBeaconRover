package com.scsa.abr.domain.navigation.executor

import com.scsa.abr.domain.model.NavigationDirection
import com.scsa.abr.domain.model.NavigationMove

class LowVoltageExecutor: NavigationExecutor {
    override fun executeMove(move: NavigationMove) {
        when (move.direction) {
            NavigationDirection.FORWARD -> moveForward(move.amount)
            NavigationDirection.BACKWARD-> moveBackward(move.amount)
            NavigationDirection.LEFT-> rotateLeft(move.amount)
            NavigationDirection.RIGHT-> rotateRight(move.amount)
        }
    }

    override fun stop() {
        TODO("Not yet implemented")
    }

    override fun gatherData() {
        TODO("Not yet implemented")
    }

    private fun moveForward(amount: Int) {}
    private fun moveBackward(amount: Int) {}
    private fun rotateLeft(amount: Int) {}
    private fun rotateRight(amount: Int) {}
}