package com.scsa.abr.ui

import com.scsa.abr.domain.BleConnectionState

data class BleGattUiState(
    val connectionState: BleConnectionState = BleConnectionState.DISCONNECTED,
    val deviceAddress: String? = null,
    val isConnected: Boolean = false,
    val receivedMessage: String? = null,
    val errorMessage: String? = null
)