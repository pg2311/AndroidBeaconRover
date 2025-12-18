package com.scsa.abr.domain.navigation.executor

import android.util.Log
import com.scsa.abr.domain.model.AbrProfile
import com.scsa.abr.domain.model.BeaconScanData
import com.scsa.abr.domain.model.NavigationDirection
import com.scsa.abr.domain.model.NavigationMove
import com.scsa.abr.domain.repository.BeaconRepository
import com.scsa.abr.domain.repository.BleGattRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.sqrt

private const val TAG = "HighVoltageExecutor"

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
        sendMotorCommand("S")
    }

    override suspend fun measureDistance(): Double {
        val startTime = System.currentTimeMillis()
        var count = 0

        var resultList: List<BeaconScanData>? = null
        while (resultList == null || resultList.isEmpty()) {
            count += 1
            resultList = getResultList(startTime)
        }

        return getStableDistance(resultList.map { it.distance })
    }

    private fun getStableDistance(data: List<Double>): Double {
        val threshold = 1.5
        val median = data.median()
        val std = data.standardDeviation()

        val firstPass = data.filter { abs(it - median) <= threshold * std }

        if (firstPass.size < 3) {
            return median
        }

        val cleanMean = firstPass.average()
        val cleanStd = firstPass.standardDeviation()

        val secondPass = firstPass.filter { abs(it - cleanMean) <= threshold * cleanStd }

        return if (secondPass.isNotEmpty()) {
            secondPass.average()
        } else {
            firstPass.average()
        }
    }

    private suspend fun getResultList(startTime: Long): List<BeaconScanData>? {
        return withTimeoutOrNull(3100) {
            var list: List<BeaconScanData>
            do {
                delay(1500)
                list = beaconRepository.getBeaconHistoryAfter(startTime)
                Log.i(TAG, "curDataSize: ${list.size}")
            } while (list.size <= 11)
            list
        }
    }

    private fun List<Double>.median(): Double {
        if (isEmpty()) return 0.0
        val sorted = sorted()
        val mid = size / 2
        return if (size % 2 == 0) {
            (sorted[mid - 1] + sorted[mid]) / 2.0
        } else {
            sorted[mid]
        }
    }

    private fun List<Double>.standardDeviation(): Double {
        if (size < 2) return 0.0
        val mean = average()
        val variance = sumOf { (it - mean) * (it - mean) } / (size - 1)
        return sqrt(variance)
    }

    private suspend fun moveForward(amount: Int) {
        val forwardSpeedParam = 200
        val forwardDistParam = 1000 * amount
        val command = "F:$forwardSpeedParam:$forwardDistParam"
        Log.i(TAG, "moveForward: $command")
        sendMotorCommand(command)
    }

    private suspend fun moveBackward(amount: Int) {
        val backwardSpeedParam = 200
        val backwardDistParam = 1000 * amount
        val command = "B:$backwardSpeedParam:$backwardDistParam"
        Log.i(TAG, "moveBackward: $command")
        sendMotorCommand(command)
    }

    private suspend fun rotateLeft(amount: Int) {
        val leftSpeedParam = 185
        val leftTurnParam = 850
        val command = "G:$leftSpeedParam:${(amount / 90.0 * leftTurnParam).roundToInt()}"
        Log.i(TAG, "rotateLeft: $command")
        sendMotorCommand(command)
    }

    private suspend fun rotateRight(amount: Int) {
        val rightSpeedParam = 185
        val rightTurnParam = 850
        val command = "H:$rightSpeedParam:${(amount / 90.0 * rightTurnParam).roundToInt()}"
        Log.i(TAG, "rotateRight: $command")
        sendMotorCommand(command)
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