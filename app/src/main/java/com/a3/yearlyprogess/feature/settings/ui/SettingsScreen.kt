package com.a3.yearlyprogess.feature.settings.ui

import android.content.Intent
import android.icu.text.DateFormatSymbols
import android.icu.util.ULocale
import android.view.SoundEffectConstants
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
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
import com.a3.yearlyprogess.core.ui.components.Switch
import com.a3.yearlyprogess.core.ui.components.SwitchWithOptions
import com.a3.yearlyprogess.core.ui.components.ThemeSelector
import com.a3.yearlyprogess.core.util.CalculationType
import com.a3.yearlyprogess.core.util.YearlyProgressNotification
import com.a3.yearlyprogess.core.util.toSelectableItem
import com.a3.yearlyprogess.feature.widgets.update.WidgetUpdateBroadcastReceiver
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

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
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
            val TOTAL_SECTION_COUNT = 5
            Column(
                verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap),
            ) {
                ThemeSelector(
                    selectedTheme = settings.appTheme,
                    onThemeSelected = { viewModel.setAppTheme(it) },
                    modifier = Modifier.padding(16.dp),
                )
                SelectItemDialog(
                    title = stringResource(R.string.calendar_system_label),
                    items = getLocaleSelectableItems(),
                    selectedItem = settings.progressSettings.uLocale.toSelectableItem(),
                    onItemSelected = { _, item -> viewModel.setLocale(item.value) },
                    shape = segmentedShapes(0, TOTAL_SECTION_COUNT)
                )

                SelectItemDialog(
                    title = stringResource(R.string.calculation_mode_label),
                    items = CalculationType.entries.map { it.toSelectableItem() },
                    selectedItem = settings.progressSettings.calculationType.toSelectableItem(),
                    onItemSelected = { _, item -> viewModel.setCalculationType(item.value) },
                    shape = segmentedShapes(1, TOTAL_SECTION_COUNT)

                )
                val daysOfWeek = remember {
                    val dayNames = DateFormatSymbols.getInstance().weekdays
                    listOf(
                        SelectableItem(name = dayNames[Calendar.SUNDAY], value = Calendar.SUNDAY),
                        SelectableItem(name = dayNames[Calendar.MONDAY], value = Calendar.MONDAY),
                        SelectableItem(name = dayNames[Calendar.TUESDAY], value = Calendar.TUESDAY),
                        SelectableItem(
                            name = dayNames[Calendar.WEDNESDAY],
                            value = Calendar.WEDNESDAY
                        ),
                        SelectableItem(
                            name = dayNames[Calendar.THURSDAY],
                            value = Calendar.THURSDAY
                        ),
                        SelectableItem(name = dayNames[Calendar.FRIDAY], value = Calendar.FRIDAY),
                        SelectableItem(
                            name = dayNames[Calendar.SATURDAY],
                            value = Calendar.SATURDAY
                        )
                    )
                }

                val selectedDay =
                    daysOfWeek.find { it.value == settings.progressSettings.weekStartDay }
                        ?: daysOfWeek.first()

                SelectItemDialog(
                    title = stringResource(R.string.first_day_of_the_week),
                    items = daysOfWeek,
                    selectedItem = selectedDay,
                    onItemSelected = { _, item -> viewModel.setWeekStartDay(item.value) },
                    shape = segmentedShapes(2, TOTAL_SECTION_COUNT)
                )
                Slider(
                    title = stringResource(R.string.decimal_places_label),
                    description = stringResource(R.string.decimal_places_description),
                    value = settings.progressSettings.decimalDigits.toFloat(),
                    onValueChange = { viewModel.setDecimalDigits(it.toInt()) },
                    valueRange = 0f..13f,
                    steps = 12,
                    shape = segmentedShapes(3, TOTAL_SECTION_COUNT)
                )
                ListItem(
                    colors = ListItemDefaults.colors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    ),
                    headlineContent = { Text(stringResource(R.string.location)) },
                    supportingContent = { Text(stringResource(R.string.location_supporting_description)) },
                    trailingContent = {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null
                        )
                    },
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .clip(segmentedShapes(4, TOTAL_SECTION_COUNT))
                        .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        view.playSoundEffect(SoundEffectConstants.CLICK)
                        onNavigateToLocation()
                    }
                )
            }
        }

        item {
            Text(
                text = stringResource(R.string.events),
                style = MaterialTheme.typography.labelLarge.copy(
                    color = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.padding(16.dp)
            )
        }

        item {
            Column(
                verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap),
            ) {
                Slider(
                    title = stringResource(R.string.decimal_places_label),
                    description = stringResource(R.string.decimal_places_description),
                    value = settings.eventProgressDecimalDigits.toFloat(),
                    onValueChange = { viewModel.setEventProgressDecimalDigits(it.toInt()) },
                    valueRange = 0f..6f,
                    steps = 5,
                    shape = segmentedShapes(0, 2)
                )
                Switch(
                    title = stringResource(R.string.classic_event_cards),
                    description = stringResource(R.string.classic_event_cards_desc),
                    checked = settings.useClassicEventCards,
                    onCheckedChange = { viewModel.setUseClassicEventCards(it) },
                    shape = segmentedShapes(1, 2)
                )
            }
        }

        item {
            Text(
                text = stringResource(R.string.widgets),
                style = MaterialTheme.typography.labelLarge.copy(
                    color = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.padding(16.dp)
            )
        }

        item {
            Switch(
                title = stringResource(R.string.disable_widget_click_to_app),
                description = stringResource(R.string.disable_widget_click_to_app_desc),
                checked = settings.disableWidgetClickToApp,
                onCheckedChange = { viewModel.setDisableWidgetClickToApp(it) }
            )
        }
    }
}


@Composable
fun segmentedShapes(
    index: Int,
    count: Int,
): Shape {
    val defaultShapes = MaterialTheme.shapes.extraSmall
    val overrideShape = MaterialTheme.shapes.largeIncreased
    return remember(index, count, defaultShapes, overrideShape) {
        when {
            count == 1 -> defaultShapes
            index == 0 -> {
                defaultShapes.copy(
                    topStart = overrideShape.topStart,
                    topEnd = overrideShape.topEnd
                )
            }

            index == count - 1 -> {
                defaultShapes.copy(
                    bottomStart = overrideShape.bottomStart,
                    bottomEnd = overrideShape.bottomEnd,
                )
            }
            else -> defaultShapes
        }
    }
}