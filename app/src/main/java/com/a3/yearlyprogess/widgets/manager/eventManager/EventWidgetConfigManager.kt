package com.a3.yearlyprogess.widgets.manager.eventManager

import android.app.Application
import android.appwidget.AppWidgetManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import com.a3.yearlyprogess.R
import com.a3.yearlyprogess.ui.theme.YearlyProgressTheme
import com.a3.yearlyprogess.widgets.ui.EventWidgetOption
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update


class EventWidgetConfigManagerViewModel(private val application: Application) :
    AndroidViewModel(application) {

  private val _widgetConfig = MutableStateFlow(EventWidgetOption.load(application, -1))
  val widgetConfig
    get() = _widgetConfig

  fun loadWidgetConfig(widgetId: Int) {
    _widgetConfig.value = EventWidgetOption.load(application, widgetId)
  }


  fun updateDecimalPlaces(places: Int) {
    _widgetConfig.update { it.copy(decimalPlaces = places) }
  }

  fun saveConfig() {
    EventWidgetOption.save(application, widgetConfig.value)
  }

  fun updateBackgroundTransparency(backgroundTransparency: Float) {
    _widgetConfig.update { it.copy(backgroundTransparency = backgroundTransparency.toInt()) }
  }

  fun updateFontScale(fontScale: Float) {
    _widgetConfig.update { it.copy(fontScale = fontScale.coerceIn(0.1f, 2f)) }
  }

  fun updateTimeLeftCounter(checked: Boolean) {
    _widgetConfig.update { it.copy(timeLeftCounter = checked) }
  }

  fun updateDynamicTimeLeftCounter(checked: Boolean) {
    if (checked) {
      _widgetConfig.update { it.copy(timeLeftCounter = true, dynamicLeftCounter = true) }
    } else {
      _widgetConfig.update { it.copy(dynamicLeftCounter = false) }
    }
  }

  fun updateReplaceTimeLeftCounter(checked: Boolean) {
    if (checked) {
      _widgetConfig.update { it.copy(timeLeftCounter = true, replaceProgressWithDaysLeft = true) }
    } else {
      _widgetConfig.update { it.copy(replaceProgressWithDaysLeft = false) }
    }
  }
}

class EventWidgetConfigManager : ComponentActivity() {

  private val viewModel: EventWidgetConfigManagerViewModel by viewModels()

  @OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    val appWidgetId =
      intent
        ?.extras
        ?.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
        ?: AppWidgetManager.INVALID_APPWIDGET_ID

    viewModel.loadWidgetConfig(appWidgetId)

    setContent {


      val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
      val widgetConfig = viewModel.widgetConfig.collectAsState()
      YearlyProgressTheme {
        Scaffold(
            modifier = Modifier
              .nestedScroll(scrollBehavior.nestedScrollConnection)
              .fillMaxSize(),
            topBar = {
              CenterAlignedTopAppBar(
                  title = {
                    Text(
                        text = stringResource(R.string.customize_event_widget),
                    )
                  },
                  scrollBehavior = scrollBehavior,
                  navigationIcon = {
                    IconButton(onClick = { finish() }) {
                      Icon(
                        Icons.AutoMirrored.Default.ArrowBack,
                        contentDescription = stringResource(R.string.go_back))
                    }
                  }
              )
            },
            floatingActionButton = {
              Button(
                  onClick = {
                    viewModel.saveConfig()
                    setResult(RESULT_OK)
                    finish()
                  }) {
                    Text(stringResource(R.string.save))
                  }
            },
            floatingActionButtonPosition = FabPosition.Center,
            contentWindowInsets = WindowInsets.safeDrawing,
        ) { innerPadding ->
          LazyColumn(
              contentPadding = innerPadding,
              verticalArrangement = Arrangement.spacedBy(8.dp),
              modifier = Modifier.padding(horizontal = 16.dp)) {
                item {
                  Text(
                      text = stringResource(R.string.widget_settings),
                      style = MaterialTheme.typography.labelLarge,
                      modifier = Modifier.padding(vertical = 8.dp))
                }

                item {
                  Column(
                      verticalArrangement = Arrangement.spacedBy(8.dp),
                  ) {
                    val timeLeftInteractionSource = remember { MutableInteractionSource() }
                    val dynamicTimeLeftInteractionSource = remember { MutableInteractionSource() }
                    val replaceTimeLeftInteractionSource = remember { MutableInteractionSource() }

                    Row(
                        modifier =
                        Modifier
                          .fillMaxWidth()
                          .clickable(
                            interactionSource = timeLeftInteractionSource, indication = null
                          ) {
                            viewModel.updateTimeLeftCounter(
                              !widgetConfig.value.timeLeftCounter
                            )
                          },
                        verticalAlignment = Alignment.CenterVertically) {
                          Text(
                              stringResource(R.string.time_left_counter),
                              modifier = Modifier.weight(1f))

                          Switch(
                              checked = widgetConfig.value.timeLeftCounter,
                              onCheckedChange = { viewModel.updateTimeLeftCounter(it) },
                              interactionSource = timeLeftInteractionSource)
                        }

                    Row(
                        modifier =
                        Modifier
                          .fillMaxWidth()
                          .clickable(
                            interactionSource = dynamicTimeLeftInteractionSource,
                            indication = null
                          ) {
                            viewModel.updateDynamicTimeLeftCounter(
                              !widgetConfig.value.dynamicLeftCounter
                            )
                          },
                        verticalAlignment = Alignment.CenterVertically) {
                          Text(
                              stringResource(R.string.dynamic_time_left_counter),
                              modifier = Modifier.weight(1f))

                          Switch(
                              checked = widgetConfig.value.dynamicLeftCounter,
                              onCheckedChange = { viewModel.updateDynamicTimeLeftCounter(it) },
                              interactionSource = dynamicTimeLeftInteractionSource)
                        }

                    Row(
                        modifier =
                        Modifier
                          .fillMaxWidth()
                          .clickable(
                            interactionSource = replaceTimeLeftInteractionSource,
                            indication = null
                          ) {
                            viewModel.updateReplaceTimeLeftCounter(
                              !widgetConfig.value.replaceProgressWithDaysLeft
                            )
                          },
                        verticalAlignment = Alignment.CenterVertically) {
                          Text(
                              stringResource(R.string.replace_progress_with_days_left_counter),
                              modifier = Modifier.weight(1f))

                          Switch(
                              checked = widgetConfig.value.replaceProgressWithDaysLeft,
                              onCheckedChange = { viewModel.updateReplaceTimeLeftCounter(it) },
                              interactionSource = replaceTimeLeftInteractionSource)
                        }

                    Column {
                      var decimalPlaces by remember {
                        mutableFloatStateOf(widgetConfig.value.decimalPlaces.toFloat())
                      }
                      Text(stringResource(R.string.pref_title_widget_decimal_places))
                      Slider(
                          value = decimalPlaces,
                          onValueChange = { decimalPlaces = it },
                          valueRange = 0f..5f,
                          steps = 4,
                          onValueChangeFinished = {
                            viewModel.updateDecimalPlaces(decimalPlaces.toInt())
                          },
                      )
                    }
                    Column {
                      var backgroundTransparency by remember {
                        mutableFloatStateOf(widgetConfig.value.backgroundTransparency.toFloat())
                      }
                      Text(stringResource(R.string.widget_transparency))
                      Slider(
                          value = backgroundTransparency,
                          onValueChange = { backgroundTransparency = it },
                          valueRange = 0f..100f,
                          onValueChangeFinished = {
                            viewModel.updateBackgroundTransparency(backgroundTransparency)
                          },
                      )
                    }
                    Column {
                      var fontScale by remember {
                        mutableFloatStateOf(widgetConfig.value.fontScale)
                      }
                      Text(stringResource(R.string.font_size))
                      Slider(
                          value = fontScale,
                          onValueChange = { fontScale = it },
                          valueRange = 0.1f..2f,
                          onValueChangeFinished = { viewModel.updateFontScale(fontScale) },
                      )
                    }
                  }
                }

              }
        }
      }
    }
  }
}
