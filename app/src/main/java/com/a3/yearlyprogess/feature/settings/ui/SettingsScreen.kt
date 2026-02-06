package com.a3.yearlyprogess.feature.settings.ui

import android.content.Intent
import android.icu.text.DateFormatSymbols
import android.icu.util.ULocale
import android.view.SoundEffectConstants
import androidx.activity.ComponentActivity
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.a3.yearlyprogess.R
import com.a3.yearlyprogess.core.ui.components.SelectItemDialog
import com.a3.yearlyprogess.core.ui.components.SelectableItem
import com.a3.yearlyprogess.core.ui.components.Slider
import com.a3.yearlyprogess.core.ui.components.SwitchWithOptions
import com.a3.yearlyprogess.core.ui.components.ThemeSelector
import com.a3.yearlyprogess.core.util.CalculationType
import com.a3.yearlyprogess.core.util.YearlyProgressNotification
import com.a3.yearlyprogess.core.util.toSelectableItem
import com.a3.yearlyprogess.feature.widgets.update.WidgetUpdateBroadcastReceiver
import kotlinx.coroutines.launch
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
    yearlyProgressNotification: YearlyProgressNotification,
    onNavigateToLocation: () -> Unit,
    onNavigateToNotification: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val view = LocalView.current
    val scope = rememberCoroutineScope()
    val settings by viewModel.settings.collectAsState()

    LazyColumn(modifier = modifier) {
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
                checked = settings.notificationSettings.progressShowNotification,
                onCheckedChange = { newValue ->
                    if (newValue && !yearlyProgressNotification.hasAppNotificationPermission()) {
                        (context as ComponentActivity).let { activity ->
                            yearlyProgressNotification.requestNotificationPermission(activity)
                        }
                        return@SwitchWithOptions
                    }

                    viewModel.setProgressShowNotification(newValue)

                    val widgetUpdateServiceIntent = Intent(context, WidgetUpdateBroadcastReceiver::class.java).apply {
                        // Pass the new boolean value here
                        putExtra(WidgetUpdateBroadcastReceiver.EXTRA_FORCE_NOTIFICATION, newValue)
                    }
                    context.sendBroadcast(widgetUpdateServiceIntent)
                },
                onOptionClicked = { onNavigateToNotification() }
            )
        }

        item {
            ThemeSelector(
                selectedTheme = settings.appTheme,
                onThemeSelected = { viewModel.setAppTheme(it) },
                modifier = Modifier.padding(16.dp)
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
            val daysOfWeek = remember {
                val dayNames = DateFormatSymbols.getInstance().weekdays
                listOf(
                    SelectableItem(name = dayNames[Calendar.SUNDAY], value = Calendar.SUNDAY),
                    SelectableItem(name = dayNames[Calendar.MONDAY], value = Calendar.MONDAY),
                    SelectableItem(name = dayNames[Calendar.TUESDAY], value = Calendar.TUESDAY),
                    SelectableItem(name = dayNames[Calendar.WEDNESDAY], value = Calendar.WEDNESDAY),
                    SelectableItem(name = dayNames[Calendar.THURSDAY], value = Calendar.THURSDAY),
                    SelectableItem(name = dayNames[Calendar.FRIDAY], value = Calendar.FRIDAY),
                    SelectableItem(name = dayNames[Calendar.SATURDAY], value = Calendar.SATURDAY)
                )
            }

            val selectedDay = daysOfWeek.find { it.value == settings.progressSettings.weekStartDay }
                ?: daysOfWeek.first()

            SelectItemDialog(
                title = stringResource(R.string.first_day_of_the_week),
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
                headlineContent = { Text(stringResource(R.string.location)) },
                supportingContent = { Text(stringResource(R.string.location_supporting_description)) },
                trailingContent = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null
                    )
                },
                modifier = Modifier.clickable {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    view.playSoundEffect(SoundEffectConstants.CLICK)
                    onNavigateToLocation()
                }
            )
        }
    }
}
