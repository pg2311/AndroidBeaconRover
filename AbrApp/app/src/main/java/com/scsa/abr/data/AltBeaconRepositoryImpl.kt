package com.scsa.abr.data

import android.content.Context
import com.scsa.abr.domain.model.Beacon
import com.scsa.abr.domain.model.BeaconScanData
import com.scsa.abr.domain.model.BeaconScanResult
import com.scsa.abr.domain.repository.BeaconRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.altbeacon.beacon.BeaconManager
import org.altbeacon.beacon.BeaconParser
import org.altbeacon.beacon.RangeNotifier
import org.altbeacon.beacon.Region
import org.altbeacon.beacon.service.ArmaRssiFilter
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AltBeaconRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : BeaconRepository, RangeNotifier {

    private val beaconManager: BeaconManager =
        BeaconManager.getInstanceForApplication(context)

    private val scanRegion = Region("room", null, null, null)

    private val _isScanning = MutableStateFlow(false)
    override fun isScanning(): StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _scanResults = MutableStateFlow<Map<String, BeaconScanResult>>(emptyMap())
    override fun getScanResults(): StateFlow<Map<String, BeaconScanResult>> =
        _scanResults.asStateFlow()

    private val beaconHistory = ConcurrentHashMap<String, BeaconHistory>()

    init {
        setupBeaconManager()
    }

    private fun setupBeaconManager() {
        ArmaRssiFilter.setDEFAULT_ARMA_SPEED(0.6)
        BeaconManager.setRssiFilterImplClass(ArmaRssiFilter::class.java)
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
            name = "pBeaconRight"
        ),
        Beacon(
            macAddress = "C3:00:00:1C:65:0A",
            name = "mBeaconRight"
        ),
        Beacon(
            macAddress = "00:81:F9:E2:6A:0A",
            name = "pBeaconLeft"
        ),
        Beacon(
            macAddress = "C3:00:00:1C:65:0B",
            name = "mBeaconLeft"
        ),
    )

    override fun getBeaconHistoryAfter(startTime: Long): List<BeaconScanData> {
        val currentResults = _scanResults.value.toMutableMap()
        val ret = mutableListOf<BeaconScanData>()

        currentResults.values.forEach { beaconScanResult ->
            val temp = beaconScanResult.data.filter { beaconScanData ->
                beaconScanData.timestamp >= startTime
            }
            ret.addAll(temp)
        }

        return ret;
    }

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
                    updateBeaconReading(
                        macAddress,
                        beacon.rssi,
                        beacon.distance
                    )
                }
            }
        }
    }

    private fun updateBeaconReading(macAddress: String, rssi: Int, distance: Double) {
        val currentTime = System.currentTimeMillis()
        val history = beaconHistory.getOrPut(macAddress) { BeaconHistory() }

        val newData = BeaconScanData(
            rssi = rssi,
            timestamp = currentTime,
            distance = distance
        )
        history.addHistory(newData)

        val scanResult = BeaconScanResult(
            macAddress = macAddress,
            data = history.getHistory()
        )

        val currentResults = _scanResults.value.toMutableMap()
        currentResults[macAddress] = scanResult
        _scanResults.value = currentResults
    }

    private class BeaconHistory {
        private val historySize = 100
        private val historyData = ArrayDeque<BeaconScanData>(historySize)

        @Synchronized
        fun addHistory(beaconScanData: BeaconScanData) {
            if (historyData.size >= historySize) {
                historyData.removeFirst()
            }
            historyData.addLast(beaconScanData)
        }

        @Synchronized
        fun getHistory() = historyData.toList()
    }
}