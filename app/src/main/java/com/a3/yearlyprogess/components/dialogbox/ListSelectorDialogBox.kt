package com.a3.yearlyprogess.components.dialogbox

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.a3.yearlyprogess.R

data class WeekType(val name: String, val code: String)
data class CalculationMode(val name: String, val code: String)
data class CalendarType(val name: String, val code: String)

@Composable
fun <T>ListSelectorDialogBox(
  title: String,
  items: List<T>,
  selectedItem: T?,
  onItemSelected: (index: Int, T) -> Unit,
  renderItem: @Composable (T) -> Unit,
  onDismiss: () -> Unit
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(
        text = title,
        style = MaterialTheme.typography.bodyLarge
      )
    },
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
            RadioButton(selected = type == selectedItem, onClick = { onItemSelected(index, type) })
            renderItem(type)
          }
        }
      }
    },
    confirmButton = {
      FilledTonalButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
    })
}