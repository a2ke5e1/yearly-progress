package com.a3.yearlyprogess.core.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun DateRangePickerModal(
    open: Boolean = false, onDateRangeSelected: (Pair<Long?, Long?>) -> Unit, onDismiss: () -> Unit,
    initialStartDateMillis: Long? = null,
    initialEndDateMillis: Long? = null,
) {
    val dateRangePickerState = rememberDateRangePickerState(
        initialSelectedStartDateMillis = initialStartDateMillis,
        initialSelectedEndDateMillis = initialEndDateMillis
    )
    if (!open) {
        return
    }
    DatePickerDialog(onDismissRequest = onDismiss, confirmButton = {
        TextButton(
            onClick = {
                onDateRangeSelected(
                    Pair(
                        dateRangePickerState.selectedStartDateMillis,
                        dateRangePickerState.selectedEndDateMillis
                    )
                )
                onDismiss()
            }) {
            Text("Okay")
        }
    }, dismissButton = {
        TextButton(onClick = onDismiss) {
            Text("Cancel")
        }
    }) {
        DateRangePicker(
            state = dateRangePickerState,
            title = {
                Text(
                    text = "Select date range"
                )
            },
            showModeToggle = false,
            modifier = Modifier
                .fillMaxWidth()
                .height(500.dp)
                .padding(16.dp)
        )
    }
}
