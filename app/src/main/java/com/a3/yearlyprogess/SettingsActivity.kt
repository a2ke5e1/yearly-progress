package com.a3.yearlyprogess

import android.app.Application
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.IntRange
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.a3.yearlyprogess.screens.LocationSelectionScreen
import com.a3.yearlyprogess.ui.theme.YearlyProgressTheme
import com.a3.yearlyprogess.widgets.manager.updateManager.services.WidgetUpdateBroadcastReceiver
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.time.DurationUnit
import kotlin.time.toDuration


class SettingsViewModel(private val application: Application) : AndroidViewModel(application) {

  private val prefs = PreferenceManager.getDefaultSharedPreferences(application)

  private val _timeLeftCounter = MutableStateFlow(
    prefs.getBoolean(application.getString(R.string.widget_widget_time_left), true)
  )
  val timeLeftCounter: StateFlow<Boolean> = _timeLeftCounter.asStateFlow()

  private val _dynamicTimeLeftCounter = MutableStateFlow(
    prefs.getBoolean(application.getString(R.string.widget_widget_use_dynamic_time_left), false)
  )
  val dynamicTimeLeftCounter: StateFlow<Boolean> = _dynamicTimeLeftCounter.asStateFlow()

  private val _widgetDecimalPlaces = MutableStateFlow(
    prefs.getInt(application.getString(R.string.widget_widget_decimal_point), 2)
  )
  val widgetDecimalPlaces = _widgetDecimalPlaces.asStateFlow()

  private val _eventWidgetDecimalPlaces = MutableStateFlow(
    prefs.getInt(application.getString(R.string.widget_event_widget_decimal_point), 2)
  )
  val eventWidgetDecimalPlaces = _eventWidgetDecimalPlaces.asStateFlow()

  private val _decimalProgressPage = MutableStateFlow(
    prefs.getInt(application.getString(R.string.app_widget_decimal_point), 13)
  )
  val decimalProgressPage = _decimalProgressPage.asStateFlow()


  private val _replaceTimeLeftCounter = MutableStateFlow(
    prefs.getBoolean(
      application.getString(R.string.widget_widget_event_replace_progress_with_days_counter), false
    )
  )
  val replaceTimeLeftCounter: StateFlow<Boolean> = _replaceTimeLeftCounter.asStateFlow()


  fun setTimeLeftCounter(enabled: Boolean) {
    _timeLeftCounter.value = enabled
    prefs.edit().putBoolean(application.getString(R.string.widget_widget_time_left), enabled)
      .apply()
  }

  fun setDynamicTimeLeftCounter(enabled: Boolean) {
    _dynamicTimeLeftCounter.value = enabled
    prefs.edit()
      .putBoolean(application.getString(R.string.widget_widget_use_dynamic_time_left), enabled)
      .apply()
  }

  fun setReplaceTimeLeftCounter(enabled: Boolean) {
    _replaceTimeLeftCounter.value = enabled
    prefs.edit().putBoolean(
      application.getString(R.string.widget_widget_event_replace_progress_with_days_counter),
      enabled
    ).apply()
  }

  fun setWidgetDecimalPlaces(digits: Int) {
    _widgetDecimalPlaces.value = digits
    prefs.edit().putInt(application.getString(R.string.widget_widget_decimal_point), digits).apply()
  }

  fun setEventWidgetDecimalPlaces(digits: Int) {
    _eventWidgetDecimalPlaces.value = digits
    prefs.edit().putInt(application.getString(R.string.widget_event_widget_decimal_point), digits).apply()
  }

  fun setDecimalProgressPage(digits: Int) {
    _decimalProgressPage.value = digits
    prefs.edit().putInt(application.getString(R.string.app_widget_decimal_point), digits).apply()
  }

}


class SettingsActivity : ComponentActivity() {

  private val settingsViewModel: SettingsViewModel by viewModels()

  @OptIn(ExperimentalMaterial3Api::class)
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()/*DynamicColors.applyToActivityIfAvailable(this)
    setContentView(R.layout.settings_activity)
    if (savedInstanceState == null) {
      supportFragmentManager.beginTransaction().replace(R.id.settings, SettingsFragment()).commit()
    }
    supportActionBar?.setDisplayHomeAsUpEnabled(true)
    val toolbar: MaterialToolbar = findViewById(R.id.toolbar)
    val appBarLayout: AppBarLayout = findViewById(R.id.appBarLayout)

    ViewCompat.setOnApplyWindowInsetsListener(appBarLayout) { view, windowInsets ->
      val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
      view.updatePadding(top = insets.top)
      WindowInsetsCompat.CONSUMED
    }

    toolbar.setNavigationOnClickListener { finish() }*/

    setContent {
      val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
      YearlyProgressTheme {
        Scaffold(
          modifier = Modifier
            .nestedScroll(scrollBehavior.nestedScrollConnection)
            .fillMaxSize(),
          topBar = {
            CenterAlignedTopAppBar(title = {
              Text(
                text = stringResource(R.string.settings),
              )
            }, scrollBehavior = scrollBehavior, navigationIcon = {
              IconButton(onClick = { finish() }) {
                Icon(
                  Icons.AutoMirrored.Default.ArrowBack,
                  contentDescription = stringResource(R.string.go_back)
                )
              }
            })
          },
          contentWindowInsets = WindowInsets.safeDrawing,
        ) { innerPadding ->

          SettingsScreen(
            contentPadding = innerPadding, viewModel = settingsViewModel
          )
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

    Row(modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp)
      .clickable(
        enabled = !disabled, interactionSource = interactionSource, indication = null
      ) { onCheckedChange(!checked) }
      .alpha(if (!disabled) 1f else 0.5f)
      .animateContentSize(),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(16.dp)) {
      Column(modifier = Modifier.weight(1f)) {
        Text(title, style = MaterialTheme.typography.bodyLarge)

        AnimatedVisibility(visible = true) {
          Text(
            summary, style = MaterialTheme.typography.bodyMedium.copy(
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          )
        }
      }

      Switch(
        checked = checked,
        onCheckedChange = { onCheckedChange(it) },
        interactionSource = interactionSource
      )
    }
  }

  @Composable
  fun SliderPreference(
    title: String,
    summary: String,
    disabled: Boolean = false,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    @IntRange(from = 0)
    steps: Int = 0,
    onValueChange: (Float) -> Unit,
  ) {

    Column(modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp)
      .alpha(if (!disabled) 1f else 0.5f)
      .animateContentSize(),
     ) {

        Text(title, style = MaterialTheme.typography.bodyLarge)

        AnimatedVisibility(visible = true) {
          Text(
            summary, style = MaterialTheme.typography.bodyMedium.copy(
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          )
        }


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
  fun SettingsScreen(contentPadding: PaddingValues, viewModel: SettingsViewModel) {
    val timeLeftCounter by viewModel.timeLeftCounter.collectAsState()
    val dynamicTimeLeftCounter by viewModel.dynamicTimeLeftCounter.collectAsState()
    val replaceTimeLeftCounter by viewModel.replaceTimeLeftCounter.collectAsState()
    val widgetDecimalPlaces by viewModel.widgetDecimalPlaces.collectAsState()
    val decimalProgressPage by viewModel.decimalProgressPage.collectAsState()
    val eventWidgetDecimalPlaces by viewModel.eventWidgetDecimalPlaces.collectAsState()

    LazyColumn(
      contentPadding = contentPadding, verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      item {
        SwitchPreference(title = stringResource(R.string.time_left_counter),
          summary = stringResource(R.string.shows_how_much_time_left_in_the_widget),
          checked = timeLeftCounter,
          onCheckedChange = { viewModel.setTimeLeftCounter(it) })
      }
      item {
        SwitchPreference(title = stringResource(R.string.dynamic_time_left_counter),
          summary = stringResource(R.string.dynamic_time_left_counter_will_automatically_switch_between_days_hours_minutes_based_on_the_time_left),
          checked = dynamicTimeLeftCounter,
          disabled = !timeLeftCounter,
          onCheckedChange = { viewModel.setDynamicTimeLeftCounter(it) })


      }

      item {
        SwitchPreference(title = stringResource(R.string.replace_progress_with_days_left_counter),
          summary = stringResource(R.string.this_will_only_work_if_the_time_left_counter_is_enabled),
          checked = replaceTimeLeftCounter,
          disabled = !timeLeftCounter,
          onCheckedChange = { viewModel.setReplaceTimeLeftCounter(it) })
      }

      item {
        SliderPreference(
          title = stringResource(R.string.pref_title_widget_decimal_places),
          summary = stringResource(R.string.pref_summary_widget_decimal_places),
          value = widgetDecimalPlaces.toFloat(),
          valueRange = 0f..5f,
          steps = 4,
          onValueChange = { viewModel.setWidgetDecimalPlaces(it.toInt())}
        )
      }

      item {
        SliderPreference(
          title = stringResource(R.string.pref_title_widget_event_decimal_place),
          summary = stringResource(R.string.pref_summary_widget_decimal_places),
          value = eventWidgetDecimalPlaces.toFloat(),
          valueRange = 0f..5f,
          steps = 4,
          onValueChange = { viewModel.setEventWidgetDecimalPlaces(it.toInt())}
        )
      }

      item {
        SliderPreference(
          title = stringResource(R.string.pref_title_app_decimal_places),
          summary = stringResource(R.string.pref_summary_app_decimal_places),
          value = decimalProgressPage.toFloat(),
          valueRange = 0f..13f,
          steps = 12,
          onValueChange = { viewModel.setDecimalProgressPage(it.toInt())}
        )
      }
    }
  }



  class SettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(
        savedInstanceState: Bundle?,
        rootKey: String?,
    ) {
      setPreferencesFromResource(R.xml.root_preferences, rootKey)

      val locationPreference = findPreference<Preference>(getString(R.string.app_location_settings))
      locationPreference?.setOnPreferenceClickListener {
        startActivity(Intent(requireContext(), LocationSelectionScreen::class.java))
        true
      }

      val updateFrequencyPreference =
          findPreference<Preference>(getString(R.string.widget_widget_update_frequency))
      val defaultUpdateFrequencyPreferenceSummary =
          getString(R.string.adjust_widget_frequency_summary)

      updatePreferenceSummary(updateFrequencyPreference, defaultUpdateFrequencyPreferenceSummary) {
          value ->
        (value as? Int ?: 5).toDuration(DurationUnit.SECONDS).toString()
      }

      val notificationPref =
          findPreference<Preference>(getString(R.string.progress_show_notification))
      notificationPref?.setOnPreferenceChangeListener { _, newValue ->
        if (newValue == true) {
          val notificationHelper = YearlyProgressNotification(requireContext())
          if (!notificationHelper.hasAppNotificationPermission()) {
            notificationHelper.requestNotificationPermission(requireActivity())
            return@setOnPreferenceChangeListener false
          }
        }
        val widgetUpdateServiceIntent = Intent(context, WidgetUpdateBroadcastReceiver::class.java)
        context?.sendBroadcast(widgetUpdateServiceIntent)
        true
      }
    }

    private fun updatePreferenceSummary(
        preference: Preference?,
        defaultSummary: String,
        formatValue: (Any?) -> String,
    ) {
      preference?.let {
        val currentValue = it.sharedPreferences?.all?.get(it.key) ?: return
        it.summary =
            "$defaultSummary\n" +
                getString(R.string.current_value_settings, formatValue(currentValue))
        it.setOnPreferenceChangeListener { pref, newValue ->
          pref.summary =
              "$defaultSummary\n" +
                  getString(R.string.current_value_settings, formatValue(newValue))
          true
        }
      }
    }
  }
}
