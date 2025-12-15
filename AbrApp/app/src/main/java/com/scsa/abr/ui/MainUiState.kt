package com.scsa.abr.ui

import com.scsa.abr.domain.BleConnectionState

data class MainUiState(
    val rssi: Double? = null,
    val permissionState: BlePermissionState = BlePermissionState.UNKNOWN,
    val showPermissionRationale: Boolean = false,
    val gattConnectionState: BleConnectionState = BleConnectionState.DISCONNECTED,
    val receivedMessage: String? = null
)

// TODO: move this or BleConnectionState ?
enum class BlePermissionState {
    UNKNOWN,            // Not yet checked
    GRANTED,            // All permissions granted
    DENIED,             // Denied but can request again
    PERMANENTLY_DENIED  // User selected "Don't ask again"
}