package com.scsa.abr.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.scsa.abr.ui.viewmodel.VehicleControlViewModel

@Composable
fun MainScreen(
    viewModel: VehicleControlViewModel = hiltViewModel(),
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
