package com.scsa.abr.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.scsa.abr.ui.viewmodel.MainViewModel

/**
 * Vehicle control using virtual joystick.
 * Provides smooth analog control of the ESP32 vehicle.
 */
@Composable
fun VehicleJoystickControl(viewModel: MainViewModel) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Joystick Control",
            style = MaterialTheme.typography.headlineSmall
        )

        VirtualJoystick(
            joystickSize = 250.dp,
            baseColor = MaterialTheme.colorScheme.surfaceVariant,
            stickColor = MaterialTheme.colorScheme.primary,
            deadZone = 0.15f, // 15% dead zone to prevent jitter
            onMove = { x, y ->
                // Convert from -1.0..1.0 to -100..100 for ESP32 protocol
                val xCmd = (x * 100).toInt()
                val yCmd = (y * 100).toInt()

                // Send command (throttled in ViewModel)
                viewModel.sendJoystickCommand(xCmd, yCmd)
            },
            onRelease = {
                // Stop the vehicle when joystick is released
                viewModel.stop()
            }
        )

        Text(
            text = "Pull up to move forward\nPull left/right to turn",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
