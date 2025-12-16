package com.scsa.abr.domain.model

data class BeaconScanResult(
    val macAddress: String,
    val rssiHistory: List<Int>,
    val intervalHistory: List<Long>,
    val distanceHistory: List<Double>,
    val lastSeenTimestamp: Long
)
