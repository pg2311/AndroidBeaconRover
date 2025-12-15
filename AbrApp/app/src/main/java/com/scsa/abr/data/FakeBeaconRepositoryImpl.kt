package com.scsa.abr.data

import com.scsa.abr.domain.BeaconRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class FakeBeaconRepositoryImpl @Inject constructor(
): BeaconRepository{
    override fun startScanning() {
    }

    override fun stopScanning() {
    }

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