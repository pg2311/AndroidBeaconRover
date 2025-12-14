package com.scsa.abr.domain

import kotlinx.coroutines.flow.Flow

interface BeaconRepository {
    fun startScanning()
    fun stopScanning()
    fun getRssi(): Flow<Double>
}