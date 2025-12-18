package com.scsa.abr.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.scsa.abr.domain.model.Beacon
import com.scsa.abr.domain.model.BeaconScanResult
import com.scsa.abr.ui.viewmodel.BeaconViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BeaconScreen(
    modifier: Modifier = Modifier,
    viewModel: BeaconViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(modifier = modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Beacon Scanner") },
            actions = {
                IconToggleButton(
                    checked = uiState.isScanning,
                    onCheckedChange = { viewModel.onToggleScan() }
                ) {
                    Icon(
                        imageVector = if (uiState.isScanning)
                            Icons.Default.Stop
                        else
                            Icons.Default.PlayArrow,
                        contentDescription = if (uiState.isScanning)
                            "Stop Scanning"
                        else
                            "Start Scanning",
                        tint = if (uiState.isScanning)
                            MaterialTheme.colorScheme.error
                        else
                            MaterialTheme.colorScheme.primary
                    )
                }
            }
        )

        uiState.errorMessage?.let { error ->
            Surface(
                color = MaterialTheme.colorScheme.errorContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }

        if (uiState.configuredBeacons.isEmpty()) {
            EmptyState(modifier = Modifier.fillMaxSize())
        } else {
            BeaconList(
                beacons = uiState.configuredBeacons,
                scanResults = uiState.scanResults,
                modifier = Modifier.fillMaxSize()
            )
        }
    }

}


@Composable
private fun BeaconList(
    beacons: List<Beacon>,
    scanResults: Map<String, BeaconScanResult>,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(
            items = beacons,
            key = { it.macAddress }
        ) { beacon ->
            BeaconCard(
                beacon = beacon,
                scanResult = scanResults[beacon.macAddress.uppercase()]
            )
        }
    }
}

@Composable
private fun BeaconCard(
    beacon: Beacon,
    scanResult: BeaconScanResult?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {

            // Beacon name and MAC address
            Text(
                text = beacon.name,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = beacon.macAddress,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (scanResult != null) {
                val dataSize = scanResult.data.size
                val startIndex = if (dataSize < 10) {
                    0
                } else {
                    dataSize - 10
                }

                Text(
                    text = "RSSI History:",
                    style = MaterialTheme.typography.labelMedium
                )
                Text(text = "${scanResult.data.size}")
                Text(
                    text = scanResult.data
                        .subList(startIndex, dataSize)
                        .joinToString("\n") { "$it" },
                    style = MaterialTheme.typography.bodySmall,
                )

                Spacer(modifier = Modifier.height(8.dp))
            } else {
                // Beacon not detected
                Text(
                    text = "Not detected",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }

        }

    }
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "No beacons configured",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}