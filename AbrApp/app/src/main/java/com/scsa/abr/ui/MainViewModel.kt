package com.scsa.abr.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scsa.abr.data.AbrProfile
import com.scsa.abr.domain.BeaconRepository
import com.scsa.abr.domain.BleGattRepository
import com.scsa.abr.domain.BlePermissionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val beaconRepository: BeaconRepository,
    private val blePermissionsRepository: BlePermissionRepository,
    private val bleGattRepository: BleGattRepository
) : ViewModel() {

    private val TAG = "MainViewModel"

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    // Throttling for joystick commands
    private var lastCommandTime = 0L
    private val COMMAND_THROTTLE_MS = 50L // Send max 20 commands per second

    init {
        observeRssi()
        observeGattConnection()
        observeReceivedData()
    }

    private fun observeRssi() {
        beaconRepository.getRssi()
            .onEach { rssiVal ->
                _uiState.update { it.copy(rssi = rssiVal) }
            }
            .launchIn(viewModelScope)
    }

    private fun observeGattConnection() {
        bleGattRepository.connectionState
            .onEach { state ->
                _uiState.update { it.copy(gattConnectionState = state) }
            }
            .launchIn(viewModelScope)
    }

    private fun observeReceivedData() {
        bleGattRepository.receivedData
            .onEach { data ->
                val message = String(data)
                Log.i(TAG, "Received: $message")
                // TODO: show received data on UI
            }
            .launchIn(viewModelScope)
    }

    fun connectToAbr(deviceAddress: String) {
        viewModelScope.launch {
            bleGattRepository.connect(deviceAddress)
                .onSuccess { Log.i(TAG, "Connected to ABR") }
                .onFailure { e -> Log.e(TAG, "Connection failed", e) }
        }
    }

    fun sendMotorCommand(command: String) {
        viewModelScope.launch {
            val data = command.toByteArray(Charsets.UTF_8)
            bleGattRepository.writeCharacteristic(
                AbrProfile.SERVICE_UUID,
                AbrProfile.CHARACTERISTIC_CONTROL_UUID,
                data
            )
                .onSuccess { Log.i(TAG, "Command sent: $command") }
                .onFailure { e -> Log.e(TAG, "Failed to send command: $command", e) }
        }
    }

    fun startStatusNotifications() {
        bleGattRepository.startNotifications(
            AbrProfile.SERVICE_UUID,
            AbrProfile.CHARACTERISTIC_STATUS_UUID
        )
    }

    fun moveForward(speed: Int = 200) {
        sendMotorCommand("F:$speed")
    }

    fun moveBackward(speed: Int = 200) {
        sendMotorCommand("B:$speed")
    }

    fun turnLeft(speed: Int = 200) {
        sendMotorCommand("L:$speed")
    }

    fun turnRight(speed: Int = 200) {
        sendMotorCommand("R:$speed")
    }

    fun stop() {
        sendMotorCommand("S")
    }

    fun sendJoystickCommand(x: Int, y: Int) {
        val currentTime = System.currentTimeMillis()

        // Throttle: only send if enough time has passed since last command
        if (currentTime - lastCommandTime < COMMAND_THROTTLE_MS) {
            return
        }

        lastCommandTime = currentTime
        sendMotorCommand("J:$x:$y")
    }

    fun startScanning() {
        beaconRepository.startScanning()
        // start BLE scanning here
    }

    fun stopScanning() {
        beaconRepository.stopScanning()
    }

    // BLE
    fun checkPermissions(): Boolean {
        val hasPermissions = blePermissionsRepository.hasAllPermissions()
        _uiState.update {
            it.copy(
                permissionState = if (hasPermissions) {
                    BlePermissionState.GRANTED
                } else {
                    BlePermissionState.DENIED
                }
            )
        }
        return hasPermissions
    }

    fun onPermissionResult(results: Map<String, Boolean>, shouldShowRationale: Boolean) {
        val allGranted = results.values.all { it }
        _uiState.update {
            it.copy(
                permissionState = when {
                    allGranted -> BlePermissionState.GRANTED
                    shouldShowRationale -> BlePermissionState.DENIED
                    else -> BlePermissionState.PERMANENTLY_DENIED
                },
                showPermissionRationale = false
            )
        }

        if (allGranted) {
            // start BLE scanning here
        }
    }

    fun showPermissionRationale() {
        _uiState.update { it.copy(showPermissionRationale = true) }
    }

    fun dismissPermissionRationale() {
        _uiState.update { it.copy(showPermissionRationale = false) }
    }

    fun getRequiredPermissions(): Array<String> {
        return blePermissionsRepository.getRequiredPermissions()
    }
}