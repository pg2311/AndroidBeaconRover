package com.scsa.abr.domain

import kotlinx.coroutines.flow.Flow

interface BeaconRepository {
    fun getRssi(): Flow<Double>
}