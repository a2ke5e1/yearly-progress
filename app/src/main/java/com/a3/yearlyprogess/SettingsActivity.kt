package com.a3.yearlyprogess

import android.app.Application
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.IntRange
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.preference.PreferenceManager
import com.a3.yearlyprogess.components.dialogbox.CalculationMode
import com.a3.yearlyprogess.components.dialogbox.CalendarType
import com.a3.yearlyprogess.components.dialogbox.ListSelectorDialogBox
import com.a3.yearlyprogess.components.dialogbox.WeekType
import com.a3.yearlyprogess.screens.LocationSelectionScreen
import com.a3.yearlyprogess.ui.theme.YearlyProgressTheme
import com.a3.yearlyprogess.widgets.manager.updateManager.services.WidgetUpdateBroadcastReceiver
import kotlin.time.DurationUnit
import kotlin.time.toDuration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsViewModel(private val application: Application) : AndroidViewModel(application) {

  private val prefs = PreferenceManager.getDefaultSharedPreferences(application)
  private fun getStringRes(@StringRes resId: Int) = application.getString(resId)

  private inline fun <reified T> prefFlow(key: String, defaultValue: T): MutableStateFlow<T> {
    val value: T = when (T::class) {
      Boolean::class -> prefs.getBoolean(key, defaultValue as Boolean) as T
      Int::class -> prefs.getInt(key, defaultValue as Int) as T
      String::class -> prefs.getString(key, defaultValue as String) as T
      else -> throw IllegalArgumentException("Unsupported type for key: $key")
    }
    return MutableStateFlow(value)
  }

  private fun <T> updatePref(flow: MutableStateFlow<T>, key: String, value: T) {
    flow.value = value
    with(prefs.edit()) {
      when (value) {
        is Boolean -> putBoolean(key, value)
        is Int -> putInt(key, value)
        is String -> putString(key, value)
        else -> throw IllegalArgumentException("Unsupported type")
      }
      apply()
    }
  }

  private val _timeLeftCounter = prefFlow(getStringRes(R.string.widget_widget_time_left), true)
  val timeLeftCounter = _timeLeftCounter.asStateFlow()
  fun setTimeLeftCounter(value: Boolean) =
    updatePref(_timeLeftCounter, getStringRes(R.string.widget_widget_time_left), value)

  private val _dynamicTimeLeftCounter = prefFlow(
    getStringRes(R.string.widget_widget_use_dynamic_time_left), false)
  val dynamicTimeLeftCounter = _dynamicTimeLeftCounter.asStateFlow()
  fun setDynamicTimeLeftCounter(value: Boolean) =
    updatePref(_dynamicTimeLeftCounter, getStringRes(R.string.widget_widget_use_dynamic_time_left), value)

  private val _replaceTimeLeftCounter = prefFlow(
    getStringRes(R.string.widget_widget_event_replace_progress_with_days_counter), false)
  val replaceTimeLeftCounter = _replaceTimeLeftCounter.asStateFlow()
  fun setReplaceTimeLeftCounter(value: Boolean) =
    updatePref(_replaceTimeLeftCounter, getStringRes(R.string.widget_widget_event_replace_progress_with_days_counter), value)

  private val _widgetDecimalPlaces = prefFlow(getStringRes(R.string.widget_widget_decimal_point), 2)
  val widgetDecimalPlaces = _widgetDecimalPlaces.asStateFlow()
  fun setWidgetDecimalPlaces(value: Int) =
    updatePref(_widgetDecimalPlaces, getStringRes(R.string.widget_widget_decimal_point), value)

  private val _eventWidgetDecimalPlaces = prefFlow(getStringRes(R.string.widget_event_widget_decimal_point), 2)
  val eventWidgetDecimalPlaces = _eventWidgetDecimalPlaces.asStateFlow()
  fun setEventWidgetDecimalPlaces(value: Int) =
    updatePref(_eventWidgetDecimalPlaces, getStringRes(R.string.widget_event_widget_decimal_point), value)

  private val _decimalProgressPage = prefFlow(getStringRes(R.string.app_widget_decimal_point), 13)
  val decimalProgressPage = _decimalProgressPage.asStateFlow()
  fun setDecimalProgressPage(value: Int) =
    updatePref(_decimalProgressPage, getStringRes(R.string.app_widget_decimal_point), value)

  private val _widgetUpdateFreqency = prefFlow(getStringRes(R.string.widget_widget_update_frequency), 5)
  val widgetUpdateFreqency = _widgetUpdateFreqency.asStateFlow()
  fun setWidgetUpdateFreqency(value: Int) =
    updatePref(_widgetUpdateFreqency, getStringRes(R.string.widget_widget_update_frequency), value)

  private val _widgetTransparency = prefFlow(getStringRes(R.string.widget_widget_background_transparency), 100)
  val widgetTransparency = _widgetTransparency.asStateFlow()
  fun setWidgetTransparency(value: Int) =
    updatePref(_widgetTransparency, getStringRes(R.string.widget_widget_background_transparency), value)

  private val _progressShowNotification = prefFlow(getStringRes(R.string.progress_show_notification), false)
  val progressShowNotification = _progressShowNotification.asStateFlow()
  fun setProgressShowNotification(value: Boolean) =
    updatePref(_progressShowNotification, getStringRes(R.string.progress_show_notification), value)

  private val calendarEntries = application.resources.getStringArray(R.array.app_calendar_type_entries)
  private val calendarValues = application.resources.getStringArray(R.array.app_calendar_type_values)
  private val _calendarTypes = calendarEntries.zip(calendarValues) { name, code -> CalendarType(name, code) }
  val calendarTypes get() = _calendarTypes

  private val _selectedCalendarType = prefFlow(
    getStringRes(R.string.app_calendar_type), calendarTypes.first().code)
  val selectedCalendarType = _selectedCalendarType.asStateFlow()
  fun setCalendarType(item: CalendarType) =
    updatePref(_selectedCalendarType, getStringRes(R.string.app_calendar_type), item.code)

  private val weekEntries = application.resources.getStringArray(R.array.week_start_entries)
  private val weekValues = application.resources.getStringArray(R.array.week_start_values)
  private val _weekTypes = weekEntries.zip(weekValues) { name, code -> WeekType(name, code) }
  val weekTypes get() = _weekTypes

  private val _selectedWeekType = prefFlow(
    getStringRes(R.string.app_week_widget_start_day), weekTypes.first().code)
  val selectedWeekType = _selectedWeekType.asStateFlow()
  fun setWeekType(item: WeekType) =
    updatePref(_selectedWeekType, getStringRes(R.string.app_week_widget_start_day), item.code)

  private val calculationEntries = application.resources.getStringArray(R.array.calc_entries)
  private val calculationValues = application.resources.getStringArray(R.array.calc_values)
  private val _calculationModes = calculationEntries.zip(calculationValues) { name, code ->
    CalculationMode(name, code)
  }
  val calculationModes get() = _calculationModes

  private val _selectedCalculationMode = prefFlow(
    getStringRes(R.string.app_calculation_type), calculationModes.first().code)
  val selectedCalculationMode = _selectedCalculationMode.asStateFlow()
  fun setCalculationMode(item: CalculationMode) =
    updatePref(_selectedCalculationMode, getStringRes(R.string.app_calculation_type), item.code)
}

class SettingsActivity : ComponentActivity() {

  private val settingsViewModel: SettingsViewModel by viewModels()

  @OptIn(ExperimentalMaterial3Api::class)
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
      YearlyProgressTheme {
        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection).fillMaxSize(),
            topBar = {
              CenterAlignedTopAppBar(
                  title = {
                    Text(
                        text = stringResource(R.string.settings),
                    )
                  },
                  scrollBehavior = scrollBehavior,
                  navigationIcon = {
                    IconButton(onClick = { finish() }) {
                      Icon(
                          Icons.AutoMirrored.Default.ArrowBack,
                          contentDescription = stringResource(R.string.go_back))
                    }
                  })
            },
            contentWindowInsets = WindowInsets.safeDrawing,
        ) { innerPadding ->
          SettingsScreen(contentPadding = innerPadding, viewModel = settingsViewModel)
        }
      }
    }
  }

  @Composable
  fun SwitchPreference(
      title: String,
      summary: String,
      checked: Boolean,
      disabled: Boolean = false,
      onCheckedChange: (Boolean) -> Unit
  ) {
    val interactionSource = remember { MutableInteractionSource() }

    Row(
        modifier =
            Modifier.fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clickable(
                    enabled = !disabled, interactionSource = interactionSource, indication = null) {
                      onCheckedChange(!checked)
                    }
                .alpha(if (!disabled) 1f else 0.5f)
                .animateContentSize(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)) {
          Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)

            AnimatedVisibility(visible = true) {
              Text(
                  summary,
                  style =
                      MaterialTheme.typography.bodyMedium.copy(
                          color = MaterialTheme.colorScheme.onSurfaceVariant))
            }
          }

          Switch(
              checked = checked,
              onCheckedChange = { onCheckedChange(it) },
              interactionSource = interactionSource)
        }
  }

  @Composable
  fun SliderPreference(
      title: String,
      summary: String,
      disabled: Boolean = false,
      value: Float,
      valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
      @IntRange(from = 0) steps: Int = 0,
      onValueChange: (Float) -> Unit,
  ) {

    Column(
        modifier =
            Modifier.fillMaxWidth()
                .padding(horizontal = 16.dp)
                .alpha(if (!disabled) 1f else 0.5f)
                .animateContentSize(),
    ) {
      Text(title, style = MaterialTheme.typography.bodyLarge)

      AnimatedVisibility(visible = true) {
        Text(
            summary,
            style =
                MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant))
      }

      Spacer(Modifier.height(4.dp))

      Slider(
          enabled = !disabled,
          value = value,
          onValueChange = onValueChange,
          valueRange = valueRange,
          steps = steps,
      )
    }
  }

  @Composable
  fun ManageLocation(disabled: Boolean = false) {
    val interactionSource = remember { MutableInteractionSource() }
    Column(
        modifier =
            Modifier.fillMaxWidth()
                .clickable(
                    enabled = !disabled,
                ) {
                  startActivity(Intent(this, LocationSelectionScreen::class.java))
                }
                .alpha(if (!disabled) 1f else 0.5f)
                .animateContentSize(),
    ) {
      Text(
          stringResource(R.string.manage_location_title),
          style = MaterialTheme.typography.bodyLarge,
          modifier = Modifier.padding(16.dp))
    }
  }

  @Composable
  fun SettingsScreen(contentPadding: PaddingValues, viewModel: SettingsViewModel) {
    val decimalProgressPage by viewModel.decimalProgressPage.collectAsState()
    val widgetUpdateFreqency by viewModel.widgetUpdateFreqency.collectAsState()
    val selectedCalendarTypeCode by viewModel.selectedCalendarType.collectAsState()
    val selectedWeekTypeCode by viewModel.selectedWeekType.collectAsState()
    val selectedCalculationMode by viewModel.selectedCalculationMode.collectAsState()
    val progressShowNotification by viewModel.progressShowNotification.collectAsState()
    val calendarTypes = viewModel.calendarTypes
    val weekTypes = viewModel.weekTypes
    val calculationModes = viewModel.calculationModes

    LazyColumn(contentPadding = contentPadding, verticalArrangement = Arrangement.spacedBy(16.dp)) {
      item {
        Text(
            text = stringResource(R.string.app),
            style =
                MaterialTheme.typography.labelLarge.copy(color = MaterialTheme.colorScheme.primary),
            modifier = Modifier.padding(16.dp))

        val context = LocalContext.current
        SwitchPreference(
            title = stringResource(R.string.progress_notification),
            summary = stringResource(R.string.shows_progress_in_the_notification),
            checked = progressShowNotification,
            onCheckedChange = { newValue ->
              if (newValue) {
                val notificationHelper = YearlyProgressNotification(context)
                if (!notificationHelper.hasAppNotificationPermission()) {
                  notificationHelper.requestNotificationPermission(this@SettingsActivity)
                }
              }
              val widgetUpdateServiceIntent =
                  Intent(context, WidgetUpdateBroadcastReceiver::class.java)
              context.sendBroadcast(widgetUpdateServiceIntent)
              viewModel.setProgressShowNotification(newValue)
            })

        ListPreference(
            title = stringResource(R.string.select_your_calendar_system),
            items = calendarTypes,
            selectedItem =
                calendarTypes.find { it.code == selectedCalendarTypeCode } ?: calendarTypes.first(),
            onItemSelected = { _, it -> viewModel.setCalendarType(it) },
            renderSelectedItem = {
              Text(
                  it.name,
                  style =
                      MaterialTheme.typography.bodyMedium.copy(
                          color = MaterialTheme.colorScheme.onSurfaceVariant))
            },
            renderItemInDialog = { Text(it.name) })

        ListPreference(
            title = stringResource(R.string.pref_title_week_day),
            items = weekTypes,
            selectedItem = weekTypes.find { it.code == selectedWeekTypeCode } ?: weekTypes.first(),
            onItemSelected = { _, it -> viewModel.setWeekType(it) },
            renderSelectedItem = {
              Text(
                  it.name,
                  style =
                      MaterialTheme.typography.bodyMedium.copy(
                          color = MaterialTheme.colorScheme.onSurfaceVariant))
            },
            renderItemInDialog = { Text(it.name) })

        ListPreference(
            title = stringResource(R.string.calculation_mode),
            items = calculationModes,
            selectedItem =
                calculationModes.find { it.code == selectedCalculationMode }
                    ?: calculationModes.first(),
            onItemSelected = { _, it -> viewModel.setCalculationMode(it) },
            renderSelectedItem = {
              Text(
                  it.name,
                  style =
                      MaterialTheme.typography.bodyMedium.copy(
                          color = MaterialTheme.colorScheme.onSurfaceVariant))
            },
            renderItemInDialog = { Text(it.name) })

        ManageLocation()
      }

      item {
        SliderPreference(
            title = stringResource(R.string.pref_title_app_decimal_places),
            summary = stringResource(R.string.pref_summary_app_decimal_places),
            value = decimalProgressPage.toFloat(),
            valueRange = 0f..13f,
            steps = 12,
            onValueChange = { viewModel.setDecimalProgressPage(it.toInt()) })
      }

      item {
        Text(
            text = stringResource(R.string.widget_customization),
            style =
                MaterialTheme.typography.labelLarge.copy(color = MaterialTheme.colorScheme.primary),
            modifier = Modifier.padding(16.dp))
        val widgetFreqSummary by remember(widgetUpdateFreqency) {
          derivedStateOf {
            getString(R.string.adjust_widget_frequency_summary) + "\n\n" +
              getString(
                R.string.current_value_settings,
                widgetUpdateFreqency.toDuration(DurationUnit.SECONDS).toString()
              )
          }
        }
        SliderPreference(
            title = stringResource(R.string.adjust_widget_frequency),
            summary = widgetFreqSummary,
            value = widgetUpdateFreqency.toFloat(),
            valueRange = 5f..900f,
            onValueChange = {
              viewModel.setWidgetUpdateFreqency(it.toInt())
            })
      }
    }
  }

  @Composable
  fun <T> ListPreference(
      title: String,
      items: List<T>,
      selectedItem: T,
      onItemSelected: (index: Int, T) -> Unit,
      disabled: Boolean = false,
      renderSelectedItem: @Composable (T) -> Unit,
      renderItemInDialog: @Composable (T) -> Unit
  ) {
    var showDialog by remember { mutableStateOf(false) }
    Column(
        modifier =
            Modifier.fillMaxWidth()
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
            style = MaterialTheme.typography.bodyLarge,
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
}
