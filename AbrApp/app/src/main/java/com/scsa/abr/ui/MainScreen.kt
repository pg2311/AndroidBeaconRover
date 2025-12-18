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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.scsa.abr.domain.model.BleConnectionState
import com.scsa.abr.ui.components.PermissionRationaleDialog
import com.scsa.abr.ui.components.VehicleButtonControl
import com.scsa.abr.ui.components.VehicleJoystickControl
import com.scsa.abr.ui.state.BlePermissionState
import com.scsa.abr.ui.viewmodel.MainViewModel

@Composable
fun MainScreen(
    viewModel: MainViewModel = hiltViewModel(),
    onRequestPermissions: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        modifier = Modifier.padding(32.dp),
        topBar = {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = {selectedTab = 0},
                    text = {Text("Control")}
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = {selectedTab = 1},
                    text = {Text("Beacons")}
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = {selectedTab = 2},
                    text = {Text("Navigation")}
                )
            }
        }
    ) { padding ->
        when (selectedTab) {
            0 -> VehicleControlScreen(
                viewModel = viewModel,
                modifier = Modifier.padding(padding),
                onRequestPermissions = onRequestPermissions
            )
            1 -> BeaconScreen(
                modifier = Modifier.padding(padding)
            )
            2 -> NavigationMonitorScreen(
                modifier = Modifier.padding(padding)
            )
        }
    }
}
