package com.scsa.abr.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scsa.abr.domain.repository.BeaconRepository
import com.scsa.abr.domain.repository.BlePermissionRepository
import com.scsa.abr.ui.state.BeaconUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.sample
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@HiltViewModel
class BeaconViewModel @Inject constructor(
    private val beaconRepository: BeaconRepository,
    private val permissionRepository: BlePermissionRepository
): ViewModel() {

    private val _uiState = MutableStateFlow(BeaconUiState())
    val uiState: StateFlow<BeaconUiState> = _uiState.asStateFlow()

    init {
        loadConfiguredBeacons()
        observeScanResults()
        observeScanningState()
    }

    fun onToggleScan() {
        if (_uiState.value.isScanning) {
            stopScanning()
        } else {
            startScanning()
        }
    }

    fun onClearHistory() {
        beaconRepository.clearHistory()
    }

    private fun startScanning() {
        if (!permissionRepository.hasAllPermissions()) {
            _uiState.update {
                it.copy(errorMessage = "Bluetooth permissions required")
            }
            return
        }

        beaconRepository.startScanning()
        _uiState.update { it.copy(errorMessage = null) }
    }

    private fun stopScanning() {
        beaconRepository.stopScanning()
    }

    private fun loadConfiguredBeacons() {
        val beacons = beaconRepository.getBeacons()
        _uiState.update { it.copy(configuredBeacons = beacons) }
    }

    @OptIn(FlowPreview::class)
    private fun observeScanResults() {
        beaconRepository.getScanResults()
            .sample(100.milliseconds)  // Throttle to max 10 updates/second
            .onEach { results ->
                _uiState.update { it.copy(scanResults = results) }
            }
            .launchIn(viewModelScope)
    }

    private fun observeScanningState() {
        beaconRepository.isScanning()
            .onEach { isScanning ->
                _uiState.update { it.copy(isScanning = isScanning) }
            }
            .launchIn(viewModelScope)
    }

    override fun onCleared() {
        super.onCleared()
        beaconRepository.stopScanning()
    }
}