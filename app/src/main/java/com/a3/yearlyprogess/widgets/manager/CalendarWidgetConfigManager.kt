package com.a3.yearlyprogess.widgets.manager

import android.Manifest
import android.app.Application
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.CalendarContract
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.FloatRange
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.database.getLongOrNull
import androidx.core.database.getStringOrNull
import androidx.lifecycle.AndroidViewModel
import com.a3.yearlyprogess.R
import com.a3.yearlyprogess.components.dialogbox.PermissionRationalDialog
import com.a3.yearlyprogess.ui.theme.YearlyProgressTheme
import com.a3.yearlyprogess.widgets.manager.CalendarEventInfo.getCalendarsDetails
import com.a3.yearlyprogess.widgets.manager.eventManager.model.Event
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.gson.Gson
import java.util.Calendar
import java.util.Date
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

object CalendarEventInfo {
  fun getTodayOrNearestEvents(
      contentResolver: ContentResolver,
      selectedCalendarId: Long
  ): List<Event> {
    // Calculate start and end of the day
    val now = System.currentTimeMillis()
    val calendar =
        Calendar.getInstance().apply {
          timeInMillis = now
          set(Calendar.HOUR_OF_DAY, 0)
          set(Calendar.MINUTE, 0)
          set(Calendar.SECOND, 0)
          set(Calendar.MILLISECOND, 0)
        }
    val startOfDay = calendar.timeInMillis
    calendar.add(Calendar.DAY_OF_MONTH, 1)
    val endOfDay = calendar.timeInMillis

    val events = mutableListOf<Event>()

    // Build the URI for the Instances table with the desired time range
    val uri =
        CalendarContract.Instances.CONTENT_URI.buildUpon()
            .appendPath(startOfDay.toString())
            .appendPath(endOfDay.toString())
            .build()

    val projection =
        arrayOf(
            CalendarContract.Instances.EVENT_ID,
            CalendarContract.Instances.TITLE,
            CalendarContract.Instances.DESCRIPTION,
            CalendarContract.Instances.BEGIN,
            CalendarContract.Instances.END,
            CalendarContract.Instances.CALENDAR_ID)

    // Query for today's events (including recurring ones)
    val selectionToday = "${CalendarContract.Instances.CALENDAR_ID} = ?"
    val selectionArgsToday = arrayOf(selectedCalendarId.toString())
    val sortOrder = "${CalendarContract.Instances.BEGIN} ASC"

    contentResolver.query(uri, projection, selectionToday, selectionArgsToday, sortOrder)?.use {
        cursor ->
      while (cursor.moveToNext()) {
        val id = cursor.getLong(cursor.getColumnIndexOrThrow(CalendarContract.Instances.EVENT_ID))
        val title =
            cursor.getStringOrNull(cursor.getColumnIndexOrThrow(CalendarContract.Instances.TITLE))
                ?: ""
        val description =
            cursor.getStringOrNull(
                cursor.getColumnIndexOrThrow(CalendarContract.Instances.DESCRIPTION)) ?: ""
        val startTimeUtc =
            cursor.getLongOrNull(cursor.getColumnIndexOrThrow(CalendarContract.Instances.BEGIN))
        val endTimeUtc =
            cursor.getLongOrNull(cursor.getColumnIndexOrThrow(CalendarContract.Instances.END))

        if (startTimeUtc != null && endTimeUtc != null) {
          events.add(
              Event(
                  id = 0,
                  eventTitle = title,
                  eventDescription = description,
                  eventStartTime = Date(startTimeUtc),
                  eventEndTime = Date(endTimeUtc)))
        }
      }
    }

    if (events.isNotEmpty()) {
      return events // Return events for today
    }

    // If no events today, query for the nearest upcoming events
    val upcomingUri =
        CalendarContract.Instances.CONTENT_URI.buildUpon()
            .appendPath(now.toString())
            .appendPath(
                (now + 30L * 24 * 60 * 60 * 1000)
                    .toString()) // Search for events within the next 30 days
            .build()

    contentResolver
        .query(upcomingUri, projection, selectionToday, selectionArgsToday, sortOrder)
        ?.use { cursor ->
          while (cursor.moveToNext()) {
            val id =
                cursor.getLong(cursor.getColumnIndexOrThrow(CalendarContract.Instances.EVENT_ID))
            val title =
                cursor.getStringOrNull(
                    cursor.getColumnIndexOrThrow(CalendarContract.Instances.TITLE)) ?: ""
            val description =
                cursor.getStringOrNull(
                    cursor.getColumnIndexOrThrow(CalendarContract.Instances.DESCRIPTION)) ?: ""
            val startTimeUtc =
                cursor.getLongOrNull(cursor.getColumnIndexOrThrow(CalendarContract.Instances.BEGIN))
            val endTimeUtc =
                cursor.getLongOrNull(cursor.getColumnIndexOrThrow(CalendarContract.Instances.END))

            if (startTimeUtc != null && endTimeUtc != null) {
              events.add(
                  Event(
                      id = 0,
                      eventTitle = title,
                      eventDescription = description,
                      eventStartTime = Date(startTimeUtc),
                      eventEndTime = Date(endTimeUtc)))
            }
          }
        }

    return events // Return the nearest upcoming event (if any) as a single-item list
  }

  data class CalendarInfo(val id: Long, val displayName: String, val accountName: String)

  fun getCalendarsDetails(contentResolver: ContentResolver): List<CalendarInfo> {
    val uri: Uri = CalendarContract.Calendars.CONTENT_URI
    val projection =
        arrayOf(
            CalendarContract.Calendars._ID,
            CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,
            CalendarContract.Calendars.ACCOUNT_NAME)

    val calendars = mutableListOf<CalendarInfo>()

    contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
      while (cursor.moveToNext()) {
        val id = cursor.getLong(cursor.getColumnIndexOrThrow(CalendarContract.Calendars._ID))
        val displayName =
            cursor.getStringOrNull(
                cursor.getColumnIndexOrThrow(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME))
        val accountName =
            cursor.getStringOrNull(
                cursor.getColumnIndexOrThrow(CalendarContract.Calendars.ACCOUNT_NAME))

        if (displayName != null && accountName != null) {
          calendars.add(CalendarInfo(id = id, displayName = displayName, accountName = accountName))
        }
      }
    }

    return calendars
  }

  fun getCurrentEventOrUpcomingEvent(contentResolver: ContentResolver, calendarId: Long): Event? {
    val events = getTodayOrNearestEvents(contentResolver, calendarId)
    return events
        .filter { event -> event.eventEndTime.time > System.currentTimeMillis() }
        .minByOrNull { event -> event.eventStartTime }
  }
}

data class CalendarWidgetConfig(
    val decimalPlaces: Int,
    val timeLeftCounter: Boolean,
    val dynamicLeftCounter: Boolean,
    val replaceProgressWithDaysLeft: Boolean,
    @FloatRange(from = 0.0, to = 1.0) val backgroundTransparency: Float,
    val selectedCalendarIds: List<Long>?
) {

  companion object {

    private const val SELECTED_CALENDAR_PREF = "selected_calendar_prefs"
    private const val SELECTED_CALENDAR_CONFIG = "selected_calendar_prefs"

    fun load(context: Context): CalendarWidgetConfig {
      val sharedPreferences =
          context.getSharedPreferences(SELECTED_CALENDAR_PREF, Context.MODE_PRIVATE)
      val jsonString =
          sharedPreferences.getString(SELECTED_CALENDAR_CONFIG, null)
              ?: return CalendarWidgetConfig(
                  decimalPlaces = 2,
                  timeLeftCounter = true,
                  dynamicLeftCounter = false,
                  replaceProgressWithDaysLeft = false,
                  backgroundTransparency = 1f,
                  selectedCalendarIds = null)
      return Gson().fromJson(jsonString, CalendarWidgetConfig::class.java)
    }

    fun save(context: Context, config: CalendarWidgetConfig) {
      val sharedPreferences =
          context.getSharedPreferences(SELECTED_CALENDAR_PREF, Context.MODE_PRIVATE)
      val edit = sharedPreferences.edit()
      val jsonString = Gson().toJson(config)
      edit.putString(SELECTED_CALENDAR_CONFIG, jsonString).apply()
    }
  }
}

class CalendarWidgetConfigManagerViewModel(private val application: Application) :
    AndroidViewModel(application) {
  private val _calendars = MutableStateFlow(listOf<CalendarEventInfo.CalendarInfo>())
  val calendar
    get() = _calendars

  private val _widgetConfig = MutableStateFlow(CalendarWidgetConfig.load(application))
  val widgetConfig
    get() = _widgetConfig

  fun loadCalendars() {
    _calendars.update { getCalendarsDetails(application.contentResolver) }
  }

  fun updateSelectedCalendars(id: Long, isSelected: Boolean) {
    _widgetConfig.update {
      val selectedCalendarIds: MutableList<Long> =
          it.selectedCalendarIds?.toMutableList() ?: calendar.value.map { it.id }.toMutableList()
      if (isSelected) {
        selectedCalendarIds.add(id)
      } else {
        selectedCalendarIds.remove(id)
      }
      it.copy(selectedCalendarIds = selectedCalendarIds)
    }
  }

  fun updateDecimalPlaces(places: Int) {
    _widgetConfig.update { it.copy(decimalPlaces = places) }
  }

  fun saveConfig() {
    CalendarWidgetConfig.save(application, widgetConfig.value)
  }

  fun updateBackgroundTransparency(backgroundTransparency: Float) {
    _widgetConfig.update { it.copy(backgroundTransparency = backgroundTransparency) }
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

class CalendarWidgetConfigManager : ComponentActivity() {

  private val viewModel: CalendarWidgetConfigManagerViewModel by viewModels()

  @OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      val calendarPermissionState =
          rememberPermissionState(permission = Manifest.permission.READ_CALENDAR)
      var showPermissionRationalMessage by rememberSaveable { mutableStateOf(false) }
      val context = LocalContext.current

      LaunchedEffect(Unit) {
        if (!calendarPermissionState.status.isGranted) {
          calendarPermissionState.launchPermissionRequest()
        }
      }

      val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
      val widgetConfig = viewModel.widgetConfig.collectAsState()
      val calendars = viewModel.calendar.collectAsState().value
      YearlyProgressTheme {
        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection).fillMaxSize(),
            topBar = {
              CenterAlignedTopAppBar(
                  title = {
                    Text(
                        text = stringResource(R.string.calendar_widget_options),
                    )
                  },
                  scrollBehavior = scrollBehavior,
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
                            Modifier.fillMaxWidth().clickable(
                                interactionSource = timeLeftInteractionSource, indication = null) {
                                  viewModel.updateTimeLeftCounter(
                                      !widgetConfig.value.timeLeftCounter)
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
                            Modifier.fillMaxWidth().clickable(
                                interactionSource = dynamicTimeLeftInteractionSource,
                                indication = null) {
                                  viewModel.updateDynamicTimeLeftCounter(
                                      !widgetConfig.value.dynamicLeftCounter)
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
                            Modifier.fillMaxWidth().clickable(
                                interactionSource = replaceTimeLeftInteractionSource,
                                indication = null) {
                                  viewModel.updateReplaceTimeLeftCounter(
                                      !widgetConfig.value.replaceProgressWithDaysLeft)
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
                        mutableFloatStateOf(widgetConfig.value.backgroundTransparency)
                      }
                      Text(stringResource(R.string.widget_transparency))
                      Slider(
                          value = backgroundTransparency,
                          onValueChange = { backgroundTransparency = it },
                          valueRange = 0f..1f,
                          onValueChangeFinished = {
                            viewModel.updateBackgroundTransparency(backgroundTransparency)
                          },
                      )
                    }
                  }
                }

                item {
                  Text(
                      text = stringResource(R.string.select_calendars),
                      style = MaterialTheme.typography.labelLarge,
                      modifier = Modifier.padding(vertical = 16.dp))
                }

                when (val status = calendarPermissionState.status) {
                  is PermissionStatus.Granted -> {
                    showPermissionRationalMessage = false
                    viewModel.loadCalendars()
                    if (calendars.isNotEmpty()) {
                      itemsIndexed(calendars) { _, item ->
                        val isItemChecked =
                            widgetConfig.value.selectedCalendarIds?.any { it == item.id } ?: true
                        ElevatedCard(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {
                              viewModel.updateSelectedCalendars(item.id, !isItemChecked)
                            }) {
                              Row(
                                  horizontalArrangement = Arrangement.SpaceBetween,
                                  modifier = Modifier.padding(8.dp).fillMaxWidth()) {
                                    Column(modifier = Modifier.weight(2f)) {
                                      Text(
                                          text = item.displayName,
                                          style =
                                              MaterialTheme.typography.titleLarge.copy(
                                                  color = MaterialTheme.colorScheme.primary))
                                      Text(
                                          text = item.accountName,
                                          style = MaterialTheme.typography.labelSmall)
                                    }
                                    Checkbox(
                                        checked = isItemChecked,
                                        onCheckedChange = { checked ->
                                          viewModel.updateSelectedCalendars(item.id, checked)
                                        })
                                  }
                            }
                      }
                    } else {
                      item { Text(stringResource(R.string.no_calendars_available)) }
                    }
                  }

                  is PermissionStatus.Denied -> {
                    showPermissionRationalMessage = true
                    item {
                      if (status.shouldShowRationale) {
                        if (showPermissionRationalMessage) {
                          PermissionRationalDialog(
                              onDismiss = { showPermissionRationalMessage = false },
                              onConfirm = { calendarPermissionState.launchPermissionRequest() },
                              iconPainter = painterResource(R.drawable.ic_outline_edit_calendar_24),
                              title = stringResource(R.string.calendar_permission_title),
                              body = stringResource(R.string.calendar_permission_message))
                        }
                      } else {
                        Card(
                            colors =
                                CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.tertiaryContainer),
                            modifier = Modifier.fillMaxWidth()) {
                              Column(
                                  modifier = Modifier.padding(32.dp).fillMaxWidth(),
                                  horizontalAlignment = Alignment.CenterHorizontally,
                                  verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                    Text(
                                        stringResource(R.string.calendar_permission_required),
                                        style =
                                            MaterialTheme.typography.bodyLarge.copy(
                                                color =
                                                    MaterialTheme.colorScheme.onTertiaryContainer))
                                    Button(
                                        onClick = {
                                          val intent =
                                              Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                                  .apply {
                                                    data =
                                                        Uri.fromParts(
                                                            "package", context.packageName, null)
                                                  }
                                          context.startActivity(intent)
                                        }) {
                                          Text(stringResource(R.string.open_settings))
                                        }
                                  }
                            }
                      }
                    }
                  }
                }
              }
        }
      }
    }
  }
}
