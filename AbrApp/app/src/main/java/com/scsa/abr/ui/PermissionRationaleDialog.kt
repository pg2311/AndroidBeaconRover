package com.scsa.abr.ui

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.scsa.abr.R

@Composable
fun PermissionRationaleDialog(
    isPermanentlyDenied: Boolean,
    onRequestPermissions: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(stringResource(if (isPermanentlyDenied) R.string.permission_required_title else R.string.permission_rationale_title))
        },
        text = {
            Text(stringResource(if (isPermanentlyDenied) R.string.permission_permanently_denied_message else R.string.permission_rationale_message))
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (isPermanentlyDenied) {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", context.packageName, null)
                        }
                        context.startActivity(intent)
                        onDismiss()
                    } else {
                        onRequestPermissions()
                    }
                }
            ) {
                Text(stringResource(if (isPermanentlyDenied) R.string.open_settings else R.string.grant_permission))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}