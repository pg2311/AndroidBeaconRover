package com.scsa.abr.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scsa.abr.domain.BeaconRepository
import com.scsa.abr.domain.BlePermissionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val beaconRepository: BeaconRepository,
    private val blePermissionsRepository: BlePermissionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        observeRssi()
    }

    private fun observeRssi() {
        beaconRepository.getRssi()
            .onEach { rssiVal ->
                _uiState.update { it.copy(rssi = rssiVal) }
            }
            .launchIn(viewModelScope)
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
        val allGranted = results.values.all {it}
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