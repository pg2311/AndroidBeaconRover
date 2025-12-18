package com.scsa.abr.data

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.scsa.abr.domain.repository.BlePermissionRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BlePermissionRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : BlePermissionRepository {
    /**
     * Returns the list of BLE permissions required for the current Android version.
     */
    override fun getRequiredPermissions(): Array<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT
            )
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        } else {
            emptyArray()
        }
    }

    /**
     * Checks if all required BLE permissions are currently granted.
     */
    override fun hasAllPermissions(): Boolean {
        val requiredPermissions = getRequiredPermissions()
        if (requiredPermissions.isEmpty()) return true

        return requiredPermissions.all { permission ->
            ContextCompat.checkSelfPermission(
                context,
                permission
            ) == PackageManager.PERMISSION_GRANTED
        }
    }
}