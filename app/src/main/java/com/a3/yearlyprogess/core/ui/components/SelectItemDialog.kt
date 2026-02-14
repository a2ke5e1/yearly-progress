package com.a3.yearlyprogess.core.ui.components

import android.view.SoundEffectConstants
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
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
    val haptic = LocalHapticFeedback.current
    val view = LocalView.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                enabled = !disabled,
            ) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                view.playSoundEffect(SoundEffectConstants.CLICK)
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
            onDismiss = { 
                view.playSoundEffect(SoundEffectConstants.CLICK)
                showDialog = false 
            })
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
    val haptic = LocalHapticFeedback.current
    val view = LocalView.current
    val listState = rememberLazyListState()


    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = title, style = MaterialTheme.typography.bodyLarge) },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 350.dp)
            ) {
                itemsIndexed(items) { index, type ->
                    val isSelected = type == selectedItem
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { 
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                view.playSoundEffect(SoundEffectConstants.CLICK)
                                onItemSelected(index, type) 
                            },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = { 
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                view.playSoundEffect(SoundEffectConstants.CLICK)
                                onItemSelected(index, type) 
                            })
                        renderItem(type)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        })
}