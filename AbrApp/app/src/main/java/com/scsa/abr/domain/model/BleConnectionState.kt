package com.scsa.abr.domain.model

enum class BleConnectionState {
    DISCONNECTED,   // Not connected
    CONNECTING,     // Connection in progress
    CONNECTED,      // Connected and ready
    DISCONNECTING,  // Disconnection in progress
    ERROR          // Connection error occurred
}
