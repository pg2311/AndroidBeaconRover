package com.scsa.abr.data

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.util.Log
import com.scsa.abr.domain.BleConnectionState
import com.scsa.abr.domain.BleGattRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class BleGattRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
): BleGattRepository {

    private val TAG = "BleGattRepository"

    private val bluetoothManager = context.getSystemService(BluetoothManager::class.java)
    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager?.adapter

    private var bluetoothGatt: BluetoothGatt? = null

    private val _connectionState = MutableStateFlow(BleConnectionState.DISCONNECTED)
    override val connectionState: StateFlow<BleConnectionState> = _connectionState.asStateFlow()

    private val _receivedData = MutableSharedFlow<ByteArray>(replay = 0)
    override val receivedData: Flow<ByteArray> = _receivedData.asSharedFlow()

    private val CLIENT_CHARACTERISTIC_CONFIG_UUID = UUID.fromString("ad3dc2eb-08ab-44c9-8c58-c2360c000907")
    private val gattCallback = object: BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    Log.i(TAG, "Connected to GATT server")
                    _connectionState.value = BleConnectionState.CONNECTED
                    gatt?.discoverServices()
                }

                BluetoothProfile.STATE_DISCONNECTED -> {
                    Log.i(TAG, "Disconnected from GATT server")
                    _connectionState.value = BleConnectionState.DISCONNECTED
                }
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            if (gatt != null && status == BluetoothGatt.GATT_SUCCESS) {
                Log.i(TAG, "Services discovered: ${gatt.services?.size}")
                gatt.services.forEach { service ->
                    Log.i(TAG, "Service: ${service.uuid}")
                    service.characteristics.forEach {characteristic ->
                        Log.i(TAG, "    Characteristic: ${characteristic.uuid}")
                    }
                }
            } else {
                Log.w(TAG, "Service discovery failed: $status")
            }
        }

        @Deprecated("Deprecated in API 33")
        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                @Suppress("DEPRECATION")
                val data = characteristic.value
                Log.i(TAG, "Characteristic read: ${data.contentToString()}")
                _receivedData.tryEmit(data)
            }
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray,
            status: Int
        ) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.i(TAG, "Characteristic read: ${value.contentToString()}")
                _receivedData.tryEmit(value)
            }
        }

        @Deprecated("Deprecated in API 33")
        override fun onCharacteristicWrite(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.i(TAG, "Characteristic write successful")
            } else {
                Log.w(TAG, "Characteristic write failed: $status")
            }
        }

        @Deprecated("Deprecated in API 33")
        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic
        ) {
            @Suppress("DEPRECATION")
            val data = characteristic.value
            Log.i(TAG, "Characteristic changed: ${data.contentToString()}")
            _receivedData.tryEmit(data)
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray
        ) {
            Log.i(TAG, "Characteristic changed: ${value.contentToString()}")
            _receivedData.tryEmit(value)
        }
    }

    override suspend fun connect(deviceAddress: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            if (bluetoothAdapter == null) {
                return@withContext Result.failure(Exception("Bluetooth not supported"))
            }

            _connectionState.value = BleConnectionState.CONNECTING

            val device = bluetoothAdapter.getRemoteDevice(deviceAddress)
            bluetoothGatt = device.connectGatt(context, false, gattCallback)

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Connection failed", e)
            _connectionState.value = BleConnectionState.ERROR
            Result.failure(e)
        }
    }


    override suspend fun disconnect() {
        _connectionState.value = BleConnectionState.DISCONNECTING
        bluetoothGatt?.disconnect()
        bluetoothGatt?.close()
        bluetoothGatt = null
        _connectionState.value = BleConnectionState.DISCONNECTED
    }


    override suspend fun writeCharacteristic(
        serviceUuid: UUID,
        charUuid: UUID,
        data: ByteArray
    ): Result<Unit> = withContext(Dispatchers.IO) {
        suspendCancellableCoroutine { continuation ->
            try {
                val service = bluetoothGatt?.getService(serviceUuid)
                val characteristic = service?.getCharacteristic(charUuid)

                if (characteristic == null) {
                    continuation.resume(Result.failure(Exception("Characteristic not found")))
                    return@suspendCancellableCoroutine
                }

                characteristic.value = data
                val success = bluetoothGatt?.writeCharacteristic(characteristic) ?: false

                if (success) {
                    continuation.resume(Result.success(Unit))
                } else {
                    continuation.resume(Result.failure(Exception("Write operation failed")))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Write characteristic failed", e)
                continuation.resume(Result.failure(e))
            }
        }
    }

    @SuppressLint("MissingPermission")
    override suspend fun readCharacteristic(
        serviceUuid: UUID,
        charUuid: UUID
    ): Result<ByteArray> = withContext(Dispatchers.IO) {
        suspendCancellableCoroutine { continuation ->
            try {
                val service = bluetoothGatt?.getService(serviceUuid)
                val characteristic = service?.getCharacteristic(charUuid)

                if (characteristic == null) {
                    continuation.resume(Result.failure(Exception("Characteristic not found")))
                    return@suspendCancellableCoroutine
                }

                val success = bluetoothGatt?.readCharacteristic(characteristic) ?: false

                if (!success) {
                    continuation.resume(Result.failure(Exception("Read operation failed")))
                }
                // Result will be delivered via gattCallback.onCharacteristicRead
                // For simplicity, this implementation doesn't wait for the callback
                // In production, you'd want to use a continuation-based approach
            } catch (e: Exception) {
                Log.e(TAG, "Read characteristic failed", e)
                continuation.resume(Result.failure(e))
            }
        }
    }

    @SuppressLint("MissingPermission")
    override fun startNotifications(serviceUuid: UUID, charUuid: UUID): Result<Unit> {
        try {
            val service = bluetoothGatt?.getService(serviceUuid)
            val characteristic = service?.getCharacteristic(charUuid)

            if (characteristic == null) {
                return Result.failure(Exception("Characteristic not found"))
            }

            // Enable local notifications
            val success = bluetoothGatt?.setCharacteristicNotification(characteristic, true) ?: false

            if (!success) {
                return Result.failure(Exception("Failed to enable notifications"))
            }

            // Enable remote notifications by writing to descriptor
            val descriptor = characteristic.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG_UUID)
            if (descriptor != null) {
                descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                bluetoothGatt?.writeDescriptor(descriptor)
            }

            return Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Start notifications failed", e)
            return Result.failure(e)
        }
    }

    @SuppressLint("MissingPermission")
    override fun stopNotifications(serviceUuid: UUID, charUuid: UUID): Result<Unit> {
        try {
            val service = bluetoothGatt?.getService(serviceUuid)
            val characteristic = service?.getCharacteristic(charUuid)

            if (characteristic == null) {
                return Result.failure(Exception("Characteristic not found"))
            }

            // Disable local notifications
            bluetoothGatt?.setCharacteristicNotification(characteristic, false)

            // Disable remote notifications
            val descriptor = characteristic.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG_UUID)
            if (descriptor != null) {
                descriptor.value = BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE
                bluetoothGatt?.writeDescriptor(descriptor)
            }

            return Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Stop notifications failed", e)
            return Result.failure(e)
        }
    }
}