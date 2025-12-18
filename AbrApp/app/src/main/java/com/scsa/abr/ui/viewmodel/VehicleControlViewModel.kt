package com.scsa.abr.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scsa.abr.domain.repository.BeaconRepository
import com.scsa.abr.domain.repository.BleGattRepository
import com.scsa.abr.domain.repository.BlePermissionRepository
import com.scsa.abr.domain.usecase.ConnectToAbrUseCase
import com.scsa.abr.domain.usecase.SendMotorCommandUseCase
import com.scsa.abr.domain.usecase.StartStatusNotificationUseCase
import com.scsa.abr.ui.state.BlePermissionState
import com.scsa.abr.ui.state.MainUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "VehicleControlViewModel"
private const val COMMAND_THROTTLE_MS = 100L

@HiltViewModel
class VehicleControlViewModel @Inject constructor(
    private val beaconRepository: BeaconRepository,
    private val blePermissionsRepository: BlePermissionRepository,
    private val bleGattRepository: BleGattRepository,
    private val connectToAbrUseCase: ConnectToAbrUseCase,
    private val startStatusNotificationsUseCase: StartStatusNotificationUseCase,
    private val sendMotorCommandUseCase: SendMotorCommandUseCase
) : ViewModel() {


    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private var lastCommandTime = 0L

    init {
        observeGattConnection()
        observeReceivedData()
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

    fun connectToAbr() {
        viewModelScope.launch {
            connectToAbrUseCase()
                .onSuccess { Log.i(TAG, "Connected to ABR") }
                .onFailure { e -> Log.e(TAG, "Connection failed", e) }
        }
    }

    fun sendMotorCommand(command: String) {
        viewModelScope.launch {
            sendMotorCommandUseCase(command)
                .onSuccess { Log.i(TAG, "Command sent: $command") }
                .onFailure { e ->
                    Log.e(TAG, "Failed to send command: $command", e)
                    if (command == "S") {
                        sendMotorCommandUseCase(command)
                            .onSuccess { Log.i(TAG, "Stop Retry Success") }
                            .onFailure { e -> Log.e(TAG, "Stop Retry Failed: $command", e) }
                    }
                }
        }
    }

    fun startStatusNotifications() {
        startStatusNotificationsUseCase()
    }

    fun stop() {
        sendMotorCommand("S")
    }

    fun sendJoystickCommand(x: Int, y: Int) {
        val currentTime = System.currentTimeMillis()

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