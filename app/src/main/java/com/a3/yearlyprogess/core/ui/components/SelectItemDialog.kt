package com.a3.yearlyprogess.core.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.a3.yearlyprogess.R

data class SelectableItem<T>(val name: String, val value: T)

@Composable
fun <T>SelectItemDialog(
    modifier: Modifier = Modifier,
    title: String,
    items: List<SelectableItem<T>>,
    selectedItem: SelectableItem<T>,
    onItemSelected: (index: Int, SelectableItem<T>) -> Unit,
    disabled: Boolean = false,
    renderSelectedItem: @Composable (SelectableItem<T>) -> Unit = {
        Text(
            it.name,
            style =
                MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
        )
    },
    renderItemInDialog: @Composable (SelectableItem<T>) -> Unit = { Text(it.name) }
) {
    var showDialog by remember { mutableStateOf(false) }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                enabled = !disabled,
            ) {
                showDialog = true
            }
            .alpha(if (!disabled) 1f else 0.5f)
            .animateContentSize(),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
            )
            AnimatedVisibility(visible = true) { renderSelectedItem(selectedItem) }
        }
    }

    if (showDialog) {
        ListSelectorDialogBox(
            title = title,
            items = items,
            selectedItem = selectedItem,
            onItemSelected = { i, it ->
                onItemSelected(i, it)
                showDialog = false
            },
            renderItem = renderItemInDialog,
            onDismiss = { showDialog = false })
    }
}


@Composable
private fun <T> ListSelectorDialogBox(
    title: String,
    items: List<T>,
    selectedItem: T?,
    onItemSelected: (index: Int, T) -> Unit,
    renderItem: @Composable (T) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = title, style = MaterialTheme.typography.bodyLarge) },
        text = {
            LazyColumn {
                itemsIndexed(items) { index, type ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onItemSelected(index, type) },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        RadioButton(
                            selected = type == selectedItem,
                            onClick = { onItemSelected(index, type) })
                        renderItem(type)
                    }
                }
            }
        },
        confirmButton = {
            FilledTonalButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        })
}