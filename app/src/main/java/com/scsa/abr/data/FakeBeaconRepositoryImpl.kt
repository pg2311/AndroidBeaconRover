package com.scsa.abr.data

import com.scsa.abr.domain.BeaconRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.random.Random

class FakeBeaconRepositoryImpl: BeaconRepository{
    override fun getRssi(): Flow<Double>
    = flow {
        while (true) {
            val randomRssi = -70.0 + Random.nextDouble(20.0)
            emit(
                randomRssi
            )
            delay(1000)
        }
    }
}