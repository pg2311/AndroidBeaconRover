package com.scsa.abr.ui.state

import com.scsa.abr.domain.model.BleConnectionState


data class BleGattUiState(
    val connectionState: BleConnectionState = BleConnectionState.DISCONNECTED,
    val deviceAddress: String? = null,
    val isConnected: Boolean = false,
    val receivedMessage: String? = null,
    val errorMessage: String? = null
)