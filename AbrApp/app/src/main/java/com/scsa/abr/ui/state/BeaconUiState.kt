package com.scsa.abr.ui.state

import com.scsa.abr.domain.model.Beacon
import com.scsa.abr.domain.model.BeaconScanResult

data class BeaconUiState(
    val isScanning: Boolean = false,
    val configuredBeacons: List<Beacon> = emptyList(),
    val scanResults: Map<String, BeaconScanResult> = emptyMap(),
    val errorMessage: String? = null
)
