package com.scsa.abr.domain.repository

import com.scsa.abr.domain.model.BleConnectionState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import java.util.UUID

interface BleGattRepository {
    /**
     * Observable connection state
     */
    val connectionState: StateFlow<BleConnectionState>

    /**
     * Stream of received data from ESP32
     */
    val receivedData: Flow<ByteArray>

    /**
     * Connect to a BLE device by MAC address
     * @param deviceAddress MAC address (e.g., "AA:BB:CC:DD:EE:FF")
     */
    suspend fun connect(deviceAddress: String): Result<Unit>

    /**
     * Disconnect from the current device
     */
    suspend fun disconnect()

    /**
     * Write data to a characteristic
     * @param serviceUuid Service UUID
     * @param charUuid Characteristic UUID
     * @param data Data to write
     */
    suspend fun writeCharacteristic(
        serviceUuid: UUID,
        charUuid: UUID,
        data: ByteArray
    ): Result<Unit>

    /**
     * Read data from a characteristic
     * @param serviceUuid Service UUID
     * @param charUuid Characteristic UUID
     */
    suspend fun readCharacteristic(
        serviceUuid: UUID,
        charUuid: UUID
    ): Result<ByteArray>

    /**
     * Enable notifications for a characteristic
     * @param serviceUuid Service UUID
     * @param charUuid Characteristic UUID
     */
    fun startNotifications(
        serviceUuid: UUID,
        charUuid: UUID
    ): Result<Unit>

    /**
     * Disable notifications for a characteristic
     */
    fun stopNotifications(
        serviceUuid: UUID,
        charUuid: UUID
    ): Result<Unit>
}
