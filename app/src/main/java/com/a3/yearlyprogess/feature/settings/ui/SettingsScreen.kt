package com.a3.yearlyprogess.feature.settings.ui

import android.icu.util.ULocale
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.a3.yearlyprogess.R
import com.a3.yearlyprogess.app.ui.AppTopBar
import com.a3.yearlyprogess.core.ui.components.SelectItemDialog
import com.a3.yearlyprogess.core.ui.components.SelectableItem
import com.a3.yearlyprogess.core.ui.components.Slider
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavHostController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val settings by viewModel.settings.collectAsState()
    var showWeekStartDialog by remember { mutableStateOf(false) }
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()


    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            AppTopBar(
                title = stringResource(R.string.settings),
                scrollBehavior = scrollBehavior,
                onNavigateUp = {
                    navController.navigateUp()
                }
            )
        },
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->

    LazyColumn(
        modifier = Modifier.padding(innerPadding)
    ) {


        item {

            Text(
                text = stringResource(R.string.app_label),
                style =
                    MaterialTheme.typography.labelLarge.copy(color = MaterialTheme.colorScheme.primary),
                modifier = Modifier.padding(16.dp)
            )


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
            // 1. Define the list of days once and store it in a variable.
            val daysOfWeek = listOf(
                SelectableItem(name = "Sunday", value = Calendar.SUNDAY),
                SelectableItem(name = "Monday", value = Calendar.MONDAY),
                SelectableItem(name = "Tuesday", value = Calendar.TUESDAY),
                SelectableItem(name = "Wednesday", value = Calendar.WEDNESDAY),
                SelectableItem(name = "Thursday", value = Calendar.THURSDAY),
                SelectableItem(name = "Friday", value = Calendar.FRIDAY),
                SelectableItem(name = "Saturday", value = Calendar.SATURDAY)
            )

            // 2. Use this list to find the selected item.
            val selectedDay = daysOfWeek.find { it.value == settings.progressSettings.weekStartDay }
                ?: daysOfWeek.first() // Default to the first item (Sunday) if not found

            // 3. Now, your dialog call is much cleaner.
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
        }

    }
}
