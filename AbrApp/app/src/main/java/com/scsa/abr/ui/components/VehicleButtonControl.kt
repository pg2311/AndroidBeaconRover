package com.scsa.abr.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.scsa.abr.ui.MainViewModel

@Composable
fun VehicleButtonControl(viewModel: MainViewModel) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Button Control",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Forward button
        Row(horizontalArrangement = Arrangement.Center) {
            Button(
                onClick = { viewModel.moveForward(200) },
                modifier = Modifier.width(120.dp)
            ) {
                Text("↑ Forward")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Left, Stop, Right buttons
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = { viewModel.turnLeft(180) },
                modifier = Modifier.width(100.dp)
            ) {
                Text("← Left")
            }
            Button(
                onClick = { viewModel.stop() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                ),
                modifier = Modifier.width(100.dp)
            ) {
                Text("STOP")
            }
            Button(
                onClick = { viewModel.turnRight(180) },
                modifier = Modifier.width(100.dp)
            ) {
                Text("Right →")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Backward button
        Row(horizontalArrangement = Arrangement.Center) {
            Button(
                onClick = { viewModel.moveBackward(200) },
                modifier = Modifier.width(120.dp)
            ) {
                Text("↓ Backward")
            }
        }
    }
}