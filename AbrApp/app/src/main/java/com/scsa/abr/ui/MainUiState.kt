package com.scsa.abr.ui

data class MainUiState(
    val rssi: Double? = null,
    val permissionState: BlePermissionState = BlePermissionState.UNKNOWN,
    val showPermissionRationale: Boolean = false
)

enum class BlePermissionState {
    UNKNOWN,            // Not yet checked
    GRANTED,            // All permissions granted
    DENIED,             // Denied but can request again
    PERMANENTLY_DENIED  // User selected "Don't ask again"
}