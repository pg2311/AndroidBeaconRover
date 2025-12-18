package com.scsa.abr.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scsa.abr.domain.model.NavigationAlgorithmType
import com.scsa.abr.domain.model.NavigationState
import com.scsa.abr.domain.repository.NavigationRepository
import com.scsa.abr.ui.state.NavigationMonitorUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NavigationMonitorViewModel @Inject constructor(
    private val navigationRepository: NavigationRepository
): ViewModel() {

    private val TAG = "NavigationMonitorViewModel"

    private val _uiState = MutableStateFlow(NavigationMonitorUiState())
    val uiState: StateFlow<NavigationMonitorUiState> = _uiState.asStateFlow()

    private var navStartTime = 0L

    init {
        observeNavigationState()
        observeIsArrived()
        observeLastMove()
    }

    private fun observeNavigationState() {
        viewModelScope.launch {
            navigationRepository.navigationState.collect { state ->
                _uiState.update { it ->
                    it.copy(
                        currentState = state,
                        isNavigating = state != NavigationState.IDLE
                    )
                }
            }
        }
    }

    private fun observeIsArrived() {
        viewModelScope.launch {
            navigationRepository.isArrived.collect { isArrived ->
                _uiState.update { it ->
                    it.copy(
                        isArrived = isArrived
                    )
                }
            }
        }
    }

    private fun observeLastMove() {
        viewModelScope.launch {
            navigationRepository.lastMove.collect { lastMove ->
                _uiState.update { it ->
                    it.copy(
                        lastMove = lastMove
                    )
                }
            }
        }
    }

    fun onToggleNavigation() {
        if (_uiState.value.isNavigating) {
            stopNavigation()
        } else {
            startNavigation()
        }
    }

    private fun startNavigation() {
        viewModelScope.launch {
            try {
                navStartTime = System.currentTimeMillis()
                navigationRepository.startNavigation(NavigationAlgorithmType.GRADIENT_DESCENT)
                _uiState.update { it.copy(errorMessage = null) }
                Log.i(TAG, "Navigation finished")
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message) }
            }
        }
    }

    private fun stopNavigation() {
        viewModelScope.launch {
            try {
                navigationRepository.stopNavigation()
                _uiState.update { it.copy(
                    errorMessage = null,
                ) }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message) }
            }
        }
    }
}