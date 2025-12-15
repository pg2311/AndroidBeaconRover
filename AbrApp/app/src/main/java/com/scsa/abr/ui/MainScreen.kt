package com.scsa.abr.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun MainScreen(
    viewModel: MainViewModel = hiltViewModel(),
    onRequestPermissions: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.checkPermissions()
    }

    LaunchedEffect(uiState.permissionState) {
        if (uiState.permissionState == BlePermissionState.DENIED
            || uiState.permissionState == BlePermissionState.PERMANENTLY_DENIED
        ) {
            viewModel.showPermissionRationale()
        }
    }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        if (uiState.permissionState == BlePermissionState.GRANTED) {
            viewModel.startScanning()
        } else {
            if (viewModel.checkPermissions()) {
                viewModel.startScanning()
            }
        }
    }

    LifecycleEventEffect(Lifecycle.Event.ON_PAUSE) {
        viewModel.stopScanning()
    }

    Column(Modifier.padding(100.dp)) {
        when (uiState.permissionState) {
            BlePermissionState.GRANTED -> Text(uiState.rssi?.let { "RSSI: $it" } ?: "Scanning...")
            else -> Text("Bluetooth permissions required.")
        }
    }

    if (uiState.showPermissionRationale) {
        PermissionRationaleDialog(
            isPermanentlyDenied = uiState.permissionState == BlePermissionState.PERMANENTLY_DENIED,
            onRequestPermissions = {
                viewModel.dismissPermissionRationale()
                onRequestPermissions()
            },
            onDismiss = {viewModel.dismissPermissionRationale()}
        )
    }

}