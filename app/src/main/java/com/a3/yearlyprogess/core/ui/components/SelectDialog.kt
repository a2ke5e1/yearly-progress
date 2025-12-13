package com.a3.yearlyprogess.core.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.a3.yearlyprogess.R

@Composable
fun <T> SelectDialog(
    isOpen: Boolean,
    title: String,
    items: List<T>,
    multiSelect: Boolean = true,
    selectedItems: Set<T> = emptySet(),
    renderItem: @Composable (T, Int) -> Unit,
    itemKey: (Int, T) -> Any = { index, _ -> index },
    onDismiss: () -> Unit,
    onConfirm: (Set<T>) -> Unit
) {
    if (!isOpen) return

    var selected by remember { mutableStateOf(selectedItems) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            LazyColumn {
                itemsIndexed(items, key = itemKey) { index, item ->
                    val isChecked = selected.contains(item)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selected =
                                    if (multiSelect) {
                                        if (isChecked) selected - item else selected + item
                                    } else {
                                        setOf(item)
                                    }
                            },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isChecked,
                            onCheckedChange = {
                                selected =
                                    if (multiSelect) {
                                        if (isChecked) selected - item else selected + item
                                    } else {
                                        setOf(item)
                                    }
                            }
                        )
                        renderItem(item, index)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = selected.isNotEmpty(),
                onClick = { onConfirm(selected) }
            ) {
               Text(stringResource(R.string.okay))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}
