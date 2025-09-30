package com.a3.yearlyprogess.feature.home.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

@Composable
fun LocationPermissionDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null
            )
        },
        title = {
            Text("Location Permission Needed")
        },
        text = {
            Text(
                "This app needs your approximate location to show accurate sunrise and sunset times for your area.\n\n" +
                        "• Only city-level accuracy is used\n" +
                        "• Your location data stays on your device\n" +
                        "• You can manually enter coordinates in settings later"
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Allow")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Not Now")
            }
        }
    )
}