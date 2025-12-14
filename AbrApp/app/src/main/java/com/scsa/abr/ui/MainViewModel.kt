package com.scsa.abr.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scsa.abr.domain.BeaconRepository
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
    private val beaconRepository: BeaconRepository
): ViewModel() {

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
    }

    fun stopScanning() {
        beaconRepository.stopScanning()
    }
}