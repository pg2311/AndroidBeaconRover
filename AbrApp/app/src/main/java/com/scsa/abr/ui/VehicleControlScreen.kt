package com.scsa.abr.ui

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.scsa.abr.domain.model.BleConnectionState
import com.scsa.abr.ui.components.PermissionRationaleDialog
import com.scsa.abr.ui.components.VehicleJoystickControl
import com.scsa.abr.ui.state.BlePermissionState
import com.scsa.abr.ui.viewmodel.VehicleControlViewModel
import kotlin.text.forEach

@Composable
fun VehicleControlScreen(
    viewModel: VehicleControlViewModel,
    modifier: Modifier = Modifier,
    onRequestPermissions: () -> Unit
) {
    val TAG = "MainScreen"

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.checkPermissions()
        viewModel.connectToAbr("58:8C:81:30:4F:C2")
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

    var useJoystick by remember { mutableStateOf(true) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Status information at top
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Android Beacon Rover",
                    style = MaterialTheme.typography.headlineMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                when (uiState.permissionState) {
                    BlePermissionState.GRANTED -> {
                        Text("RSSI: ${uiState.rssi?.let { "%.2f".format(it) } ?: "Scanning..."}")
                        Text(
                            text = "Connection: ${uiState.gattConnectionState}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = when (uiState.gattConnectionState) {
                                BleConnectionState.CONNECTED -> MaterialTheme.colorScheme.primary
                                BleConnectionState.CONNECTING -> MaterialTheme.colorScheme.secondary
                                else -> MaterialTheme.colorScheme.error
                            }
                        )
                    }
                    else -> Text("Bluetooth permissions required.")
                }
            }

            // Vehicle controls in center (only when connected)
            if (uiState.gattConnectionState == BleConnectionState.CONNECTED) {
                viewModel.startStatusNotifications()

                if (useJoystick) {
                    VehicleJoystickControl(viewModel)
                }
            } else {
                Text(
                    text = "Waiting for connection...",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Control mode toggle at bottom
            if (uiState.gattConnectionState == BleConnectionState.CONNECTED) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Button(onClick = { useJoystick = !useJoystick }) {
                        Text(if (useJoystick) "Switch to Buttons" else "Switch to Joystick")
                    }
                }
            }
        }

        // Permission dialog overlay
        if (uiState.showPermissionRationale) {
            PermissionRationaleDialog(
                isPermanentlyDenied = uiState.permissionState == BlePermissionState.PERMANENTLY_DENIED,
                onRequestPermissions = {
                    viewModel.dismissPermissionRationale()
                    onRequestPermissions()
                },
                onDismiss = { viewModel.dismissPermissionRationale() }
            )
        }
    }

    uiState.receivedMessage?.forEach { status ->
        Log.i(TAG, "Status: $status")
    }
}
