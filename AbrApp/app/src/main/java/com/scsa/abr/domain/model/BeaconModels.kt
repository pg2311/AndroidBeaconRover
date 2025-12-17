package com.scsa.abr.domain.model

import kotlin.math.roundToInt
import kotlin.math.roundToLong

data class Beacon(
    val macAddress: String,
    val name: String
)

data class BeaconScanData(
    val rssi: Int,
    val timestamp: Long,
    val distance: Double
) {
    override fun toString(): String {
        return "RSSI($rssi dBm) D(%.3f m) at $timestamp".format(distance)
    }
}

data class BeaconScanResult(
    val macAddress: String,
    val data: List<BeaconScanData>
)
