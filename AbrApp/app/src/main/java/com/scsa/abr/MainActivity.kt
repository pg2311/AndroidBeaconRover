package com.scsa.abr

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import com.scsa.abr.ui.MainScreen
import com.scsa.abr.ui.theme.ABRTheme
import com.scsa.abr.ui.viewmodel.VehicleControlViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    // TODO: ?
    private val viewModel: VehicleControlViewModel by viewModels<VehicleControlViewModel>()

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val deniedPermissions = results.filterValues { !it }.keys
        val shouldShowRationale = deniedPermissions.any { permission ->
            shouldShowRequestPermissionRationale(permission)
        }

        viewModel.onPermissionResult(results, shouldShowRationale)
    }

    private fun requestBlePermissions() {
        val permissions = viewModel.getRequiredPermissions()
        if (permissions.isNotEmpty()) {
            permissionLauncher.launch(permissions)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ABRTheme {
                MainScreen(
                    onRequestPermissions = { requestBlePermissions() }
                )
            }
        }
    }
}
