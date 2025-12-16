package com.scsa.abr.data

import android.content.Context
import com.scsa.abr.domain.repository.BeaconRepository
import com.scsa.abr.domain.model.Beacon
import com.scsa.abr.domain.model.BeaconScanResult
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.altbeacon.beacon.BeaconManager
import org.altbeacon.beacon.BeaconParser
import org.altbeacon.beacon.RangeNotifier
import org.altbeacon.beacon.Region
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AltBeaconRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
): BeaconRepository, RangeNotifier {

    private val TAG = "AltBeaconRepositoryImpl"

    private val beaconManager: BeaconManager =
        BeaconManager.getInstanceForApplication(context)

    private val scanRegion = Region("room",null,null,null)

    private val _isScanning = MutableStateFlow(false)
    override fun isScanning(): StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _scanResults = MutableStateFlow<Map<String, BeaconScanResult>>(emptyMap())
    override fun getScanResults(): StateFlow<Map<String, BeaconScanResult>> = _scanResults.asStateFlow()

    private val beaconHistory = ConcurrentHashMap<String, BeaconHistory>()

    init {
        setupBeaconManager()
    }

    private fun setupBeaconManager() {
        beaconManager.beaconParsers.add(
            BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24")
        )
        beaconManager.addRangeNotifier(this)

    }

    override fun startScanning() {
        if (_isScanning.value) {
            return
        }

        try {
            beaconManager.startRangingBeacons(scanRegion)
            _isScanning.value = true
        } catch (e: Exception) {
            _isScanning.value = false
        }
    }

    override fun stopScanning() {
        if (!_isScanning.value) {
            return
        }

        beaconManager.stopRangingBeacons(scanRegion)
        _isScanning.value = false
    }



    override fun clearHistory() {
        beaconHistory.clear()
        _scanResults.value = emptyMap()
    }

    override fun getBeacons(): List<Beacon> = listOf(
        Beacon(
            macAddress = "F0:F8:F2:04:4F:81",
            name = "pBeacon"
        ),
        Beacon(
            macAddress = "C3:00:00:1C:65:0A",
            name = "mBeacon"
        )
    )


    override fun didRangeBeaconsInRegion(
        beacons: Collection<org.altbeacon.beacon.Beacon?>?,
        region: Region?
    ) {
        val configuredMacs = getBeacons()
            .map { it.macAddress.uppercase() }
            .toSet()

        beacons?.forEach { beacon ->
            if (beacon != null) {
                val macAddress = beacon.bluetoothAddress.uppercase()

                if (macAddress in configuredMacs) {
                    val rssi = beacon.rssi
                    updateBeaconReading(macAddress, rssi)
                }
            }
        }
    }

    private fun updateBeaconReading(macAddress: String, rssi: Int) {
        val  currentTime = System.currentTimeMillis()
        val history = beaconHistory.getOrPut(macAddress){ BeaconHistory() }
        val interval = if (history.lastTimestamp > 0) {
            currentTime - history.lastTimestamp
        } else { 0 }

        history.addRssi(rssi)
        history.lastTimestamp = currentTime
        if (interval > 0) {
            history.addInterval(interval)
        }

        val scanResult = BeaconScanResult(
            macAddress = macAddress,
            rssiHistory =  history.getRssiHistory(),
            intervalHistory = history.getIntervalHistory(),
            lastSeenTimestamp = currentTime
        )

        val currentResults = _scanResults.value.toMutableMap()
        currentResults[macAddress] = scanResult
        _scanResults.value = currentResults
    }

    private class BeaconHistory {
        private val rssiQueue = ArrayDeque<Int>(10)
        private val intervalQueue = ArrayDeque<Long>(10)
        var lastTimestamp: Long = 0L

        @Synchronized
        fun addRssi(rssi: Int) {
            if (rssiQueue.size >= 10) {
                rssiQueue.removeFirst()
            }
            rssiQueue.addLast(rssi)
        }

        @Synchronized
        fun addInterval(interval: Long) {
            if (intervalQueue.size >= 10) {
                intervalQueue.removeFirst()
            }
            intervalQueue.addLast(interval)
        }

        @Synchronized
        fun getRssiHistory(): List<Int> = rssiQueue.toList()

        @Synchronized
        fun getIntervalHistory(): List<Long> = intervalQueue.toList()
    }
}