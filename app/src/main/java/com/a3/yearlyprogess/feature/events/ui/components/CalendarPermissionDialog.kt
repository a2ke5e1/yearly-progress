package com.a3.yearlyprogess.feature.events.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign

@Composable
fun CalendarPermissionDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.CalendarToday,
                contentDescription = null
            )
        },
        title = {
            Text("Calendar Permission Needed", style = TextStyle(
                textAlign = TextAlign.Center
            ))
        },
        text = {
            Text(
                "This app needs permission to read your calendar to import your events one time.\n\n" +
                        "• No continuous syncing is performed\n" +
                        "• Access is used only during the import process\n" +
                        "• Your calendar data stays on your device"
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
