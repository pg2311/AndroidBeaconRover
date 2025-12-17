package com.scsa.abr.domain.navigation.executor

import android.util.Log
import com.scsa.abr.domain.model.AbrProfile
import com.scsa.abr.domain.model.Beacon
import com.scsa.abr.domain.model.BeaconScanData
import com.scsa.abr.domain.model.NavigationDirection
import com.scsa.abr.domain.model.NavigationMove
import com.scsa.abr.domain.repository.BeaconRepository
import com.scsa.abr.domain.repository.BleGattRepository
import com.scsa.abr.domain.usecase.SendMotorCommandUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LowVoltageExecutor @Inject constructor(
    private val beaconRepository: BeaconRepository,
    private val bleGattRepository: BleGattRepository
): NavigationExecutor {

    private val TAG = "LowVoltageExecutor"

    override suspend fun executeMove(move: NavigationMove) {
        when (move.direction) {
            NavigationDirection.FORWARD -> moveForward(move.amount)
            NavigationDirection.BACKWARD-> moveBackward(move.amount)
            NavigationDirection.LEFT-> rotateLeft(move.amount)
            NavigationDirection.RIGHT-> rotateRight(move.amount)
        }
    }

    override suspend fun stop() {
        sendMotorCommand("S")
    }

    override suspend fun measureDistance(): Double {
        val startTime = System.currentTimeMillis()

        var resultList: List<BeaconScanData>? = null
        while (resultList == null || resultList.isEmpty()) {
            resultList = getResultList(startTime)
            Log.i(TAG, "resultListSize: ${resultList?.size}")
        }

        return resultList.map { it.distance }.average()
    }

    private suspend fun getResultList(startTime: Long): List<BeaconScanData>? {
        Log.i(TAG, "getResultList start at: $startTime")
        return withTimeoutOrNull(2500) {
            var list: List<BeaconScanData>
            do {
                delay(100)
                list = beaconRepository.getBeaconHistoryAfter(startTime)
            } while (list.size <= 3)
            list // Return the list when size > 10
        }
    }

    private suspend fun moveForward(amount: Int) {
        val forwardSpeedParam = 200
        val forwardDistParam = 10 * amount
        sendMotorCommand("F:$forwardSpeedParam:$forwardDistParam")
    }

    private suspend fun moveBackward(amount: Int) {
        val backwardSpeedParam = 200
        val backwardDistParam = 10 * amount
        sendMotorCommand("B:$backwardSpeedParam:$backwardDistParam")
    }

    private suspend fun rotateLeft(amount: Int) {
        val leftSpeedParam = 200
        sendMotorCommand("G:$leftSpeedParam:$amount")
    }

    private suspend fun rotateRight(amount: Int) {
        val rightSpeedParam = 200
        sendMotorCommand("H:$rightSpeedParam:$amount")
    }

    private suspend fun sendMotorCommand(command: String) {
        val data = command.toByteArray(Charsets.UTF_8)
        // TODO: if failure?
        bleGattRepository.writeCharacteristic(
            serviceUuid = AbrProfile.SERVICE_UUID,
            charUuid = AbrProfile.CHARACTERISTIC_CONTROL_UUID,
            data
        )
    }
}