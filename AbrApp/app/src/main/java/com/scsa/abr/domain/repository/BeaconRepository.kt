package com.scsa.abr.domain.repository

import com.scsa.abr.domain.model.Beacon
import com.scsa.abr.domain.model.BeaconScanResult
import kotlinx.coroutines.flow.StateFlow

interface BeaconRepository {
    /**
     * Start scanning for configured beacons.
     * Requires Bluetooth and location permissions.
     */
    fun startScanning()

    /**
     * Stop scanning and release resources.
     */
    fun stopScanning()

    /**
     * Returns current scanning state.
     */
    fun isScanning(): StateFlow<Boolean>

    /**
     * Returns scan results mapped by MAC address.
     * Updates in real-time as beacons are detected.
     */
    fun getScanResults(): StateFlow<Map<String, BeaconScanResult>>

    /**
     * Clear all beacon history.
     */
    fun clearHistory()

    /**
     * Returns list of configured beacons.
     * Currently returns hardcoded list.
     */
    fun getBeacons(): List<Beacon>
}