package com.a3.yearlyprogess.feature.settings.ui

import android.content.Intent
import android.icu.util.ULocale
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.a3.yearlyprogess.R
import com.a3.yearlyprogess.core.ui.components.SelectItemDialog
import com.a3.yearlyprogess.core.ui.components.SelectableItem
import com.a3.yearlyprogess.core.ui.components.Slider
import com.a3.yearlyprogess.core.ui.components.SwitchWithOptions
import com.a3.yearlyprogess.core.util.CalculationType
import com.a3.yearlyprogess.core.util.toSelectableItem
import java.util.Calendar

@Composable
fun getLocaleSelectableItems(): List<SelectableItem<ULocale>> {
    val entries = stringArrayResource(R.array.app_calendar_type_entries)
    val values = stringArrayResource(R.array.app_calendar_type_values)

    return entries.zip(values) { name, code ->
        val defaultULocale = ULocale.getDefault()
        val locale = if (code != "default") {
            ULocale(defaultULocale.toString() + "@calendar=${code}")
        } else {
            defaultULocale
        }
        SelectableItem(name, locale)
    }
}

@Composable
fun SettingsHomeScreen(
    viewModel: SettingsViewModel,
    onNavigateToLocation: () -> Unit,
    onNavigateToNotification: () -> Unit
) {
    val settings by viewModel.settings.collectAsState()

    LazyColumn {
        item {
            Text(
                text = stringResource(R.string.app_label),
                style = MaterialTheme.typography.labelLarge.copy(
                    color = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.padding(16.dp)
            )


                SwitchWithOptions(
                    title = stringResource(R.string.progress_notification),
                    summary = stringResource(R.string.shows_progress_in_the_notification),
                    checked = false,
                    onCheckedChange = { newValue ->
//                    if (newValue) {
//                        val notificationHelper = YearlyProgressNotification(context)
//                        if (!notificationHelper.hasAppNotificationPermission()) {
//                            notificationHelper.requestNotificationPermission(this@SettingsActivity)
//                        }
//                    }
//                    val widgetUpdateServiceIntent =
//                        Intent(context, WidgetUpdateBroadcastReceiver::class.java)
//                    context.sendBroadcast(widgetUpdateServiceIntent)
//                    viewModel.setProgressShowNotification(newValue)
                    },
                    onOptionClicked = {  onNavigateToNotification()
                    }
                )
            }
        item {

            SelectItemDialog(
                title = stringResource(R.string.calendar_system_label),
                items = getLocaleSelectableItems(),
                selectedItem = settings.progressSettings.uLocale.toSelectableItem(),
                onItemSelected = { _, item -> viewModel.setLocale(item.value) },
            )
        }

        item {
            SelectItemDialog(
                title = stringResource(R.string.calculation_mode_label),
                items = CalculationType.entries.map { it.toSelectableItem() },
                selectedItem = settings.progressSettings.calculationType.toSelectableItem(),
                onItemSelected = { _, item -> viewModel.setCalculationType(item.value) },
            )
        }

        item {
            val daysOfWeek = listOf(
                SelectableItem(name = "Sunday", value = Calendar.SUNDAY),
                SelectableItem(name = "Monday", value = Calendar.MONDAY),
                SelectableItem(name = "Tuesday", value = Calendar.TUESDAY),
                SelectableItem(name = "Wednesday", value = Calendar.WEDNESDAY),
                SelectableItem(name = "Thursday", value = Calendar.THURSDAY),
                SelectableItem(name = "Friday", value = Calendar.FRIDAY),
                SelectableItem(name = "Saturday", value = Calendar.SATURDAY)
            )

            val selectedDay = daysOfWeek.find { it.value == settings.progressSettings.weekStartDay }
                ?: daysOfWeek.first()

            SelectItemDialog(
                title = "First day of the week",
                items = daysOfWeek,
                selectedItem = selectedDay,
                onItemSelected = { _, item -> viewModel.setWeekStartDay(item.value) },
            )
        }

        item {
            Slider(
                title = stringResource(R.string.decimal_places_label),
                description = stringResource(R.string.decimal_places_description),
                value = settings.progressSettings.decimalDigits.toFloat(),
                onValueChange = { viewModel.setDecimalDigits(it.toInt()) },
                valueRange = 0f..13f,
                steps = 12,
            )
        }

        item {
            ListItem(
                headlineContent = { Text("Location") },
                supportingContent = { Text("Manage location for day/night light widgets.") },
                trailingContent = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Navigate to location settings"
                    )
                },
                modifier = Modifier.clickable { onNavigateToLocation() }
            )
        }
    }
}