package com.scsa.abr.data

import android.content.Context
import android.util.Log
import com.scsa.abr.domain.BeaconRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import org.altbeacon.beacon.BeaconManager
import org.altbeacon.beacon.BeaconParser
import org.altbeacon.beacon.Identifier
import org.altbeacon.beacon.RangeNotifier
import org.altbeacon.beacon.Region
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AltBeaconRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
): BeaconRepository {

    private val TAG = "AltBeaconRepositoryImpl"

    private val beaconManager: BeaconManager =
        BeaconManager.getInstanceForApplication(context)

    private val _rssiFlow = MutableSharedFlow<Double>(replay = 1)

    private val region = Region(
        "room",
        listOf(
            Identifier.parse("fda50693-a4e2-4fb1-afcf-c6eb07647825"),
            Identifier.parse("10004"),
            Identifier.parse("54480")
        ),
    )

    private val rangeNotifier = RangeNotifier { beacons, _ ->
        beacons.minByOrNull { it.distance }?.let {
            _rssiFlow.tryEmit(it.distance)
        }
    }

    init {
        beaconManager.beaconParsers.add(
            BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24")
        )
    }

    override fun startScanning() {
        Log.i(TAG, "startScanning==============================")
        beaconManager.addRangeNotifier(rangeNotifier)
        beaconManager.startRangingBeacons(region)
        Log.i(TAG, "startScanning==============================")
    }

    override fun stopScanning() {
        Log.i(TAG, "stopScanning==============================")
        beaconManager.removeRangeNotifier(rangeNotifier)
        beaconManager.stopRangingBeacons(region)
        Log.i(TAG, "stopScanning==============================")
    }

    override fun getRssi(): Flow<Double> {
        return _rssiFlow.asSharedFlow()
    }

}