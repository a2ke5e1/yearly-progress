package com.a3.yearlyprogess.feature.events.presentation

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.CalendarContract
import android.provider.Settings
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.a3.yearlyprogess.core.domain.model.AppSettings
import com.a3.yearlyprogess.core.domain.model.CalendarInfo
import com.a3.yearlyprogess.core.domain.repository.AppSettingsRepository
import com.a3.yearlyprogess.core.domain.repository.CalendarRepository
import com.a3.yearlyprogess.core.util.Log
import com.a3.yearlyprogess.feature.events.domain.model.Event
import com.a3.yearlyprogess.feature.events.domain.repository.EventRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.Date
import javax.inject.Inject

sealed class CalendarUiState {
    object Initial : CalendarUiState()
    object Loading : CalendarUiState()
    data class Success(val events: List<Event>) : CalendarUiState()
    data class Error(val message: String) : CalendarUiState()
    object PermissionRequired : CalendarUiState()
}

sealed class CalendarPermissionState {
    object Unknown : CalendarPermissionState()
    object PermissionRequired : CalendarPermissionState()
    object Available : CalendarPermissionState()
}

@HiltViewModel
class ImportEventsViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val appSettingsRepository: AppSettingsRepository,
    private val repository: EventRepository,
    private val calendarRepository: CalendarRepository,
) : ViewModel() {

    private val _importedEvents = MutableStateFlow<List<Event>>(emptyList())
    val events: StateFlow<List<Event>> = _importedEvents
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val settings: StateFlow<AppSettings> = appSettingsRepository.appSettings
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AppSettings()
        )

    private val _uiState = MutableStateFlow<CalendarUiState>(CalendarUiState.Initial)
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    private val _permissionState = MutableStateFlow<CalendarPermissionState>(
        CalendarPermissionState.Unknown
    )
    val permissionState: StateFlow<CalendarPermissionState> = _permissionState.asStateFlow()

    private val _shouldShowPermissionDialog = MutableStateFlow(false)
    val shouldShowPermissionDialog: StateFlow<Boolean> = _shouldShowPermissionDialog.asStateFlow()

    private val _selectedIds = MutableStateFlow<Set<Int>>(emptySet())
    val selectedIds: StateFlow<Set<Int>> = _selectedIds.asStateFlow().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptySet()
    )

    private val _isAllSelected = MutableStateFlow(false)
    val isAllSelected: StateFlow<Boolean> = _isAllSelected.asStateFlow().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    private val _showDateFilter = MutableStateFlow(false)
    val showDateFilter: StateFlow<Boolean> = _showDateFilter.asStateFlow().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    private val _dateFilter = MutableStateFlow<Pair<Long?, Long?>>(Pair(
        LocalDate.now().atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
        LocalDate.now().plusMonths(1).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
    ))
    val dateFilter: StateFlow<Pair<Long?, Long?>> = _dateFilter.asStateFlow().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000), initialValue = Pair(
            LocalDate.now().atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
            LocalDate.now().plusMonths(1).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        )
    )

    private val _availableCalendars = MutableStateFlow<List<CalendarInfo>>(emptyList())
    val availableCalendars: StateFlow<List<CalendarInfo>> =
        _availableCalendars.asStateFlow().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000), initialValue = emptyList()
        )

    private val _selectedCalendars = MutableStateFlow<List<CalendarInfo>>(emptyList())
    val selectedCalendars: StateFlow<List<CalendarInfo>> =
        _selectedCalendars.asStateFlow().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000), initialValue = emptyList()
        )

    fun loadAvailableCalendars() {
        viewModelScope.launch {
            calendarRepository.getAvailableCalendars()
                .onSuccess { calendars ->
                    _availableCalendars.value = calendars
                }
                .onFailure { error ->
                    Log.e("ImportEventsViewModel", "Failed to load calendars", error)
                }
        }
    }

    fun loadSelectedCalendarDetails(selectedIds: Set<Long>) {
        viewModelScope.launch {
            Log.d("ViewModel", "Selected Ids $selectedIds")
            calendarRepository.getSelectedCalendarDetails(selectedIds)
                .onSuccess { calendars ->
                    if (calendars.isEmpty()) {
                        _selectedCalendars.value = _availableCalendars.value
                        Log.d("ViewModel", "No Calendar selected, so showing all")
                        return@onSuccess
                    }
                    _selectedCalendars.value = calendars
                    Log.d("ViewModel", "Selected calendars: ${calendars.map { it.displayName }}")
                }
                .onFailure { error ->
                    Log.e("ViewModel", "Failed to load selected calendars", error)
                }
        }
    }

    init {
        checkCalendarPermission()

        viewModelScope.launch {
            permissionState.collect { state ->
                if (state == CalendarPermissionState.Available) {
                    loadAvailableCalendars()
                }
            }
        }

        // Combine settings, dateFilter, permissionState AND availableCalendars
        viewModelScope.launch {
            combine(
                appSettingsRepository.appSettings,
                dateFilter,
                permissionState,
                _availableCalendars
            ) { settings, filter, permission, availableCalendars ->
                Tuple4(settings, filter, permission, availableCalendars)
            }.collect { (settings, filter, permission, availableCalendars) ->
                Log.d("ViewModel", "Combined trigger - Calendar IDs: ${settings.selectedCalendarIds}, Date: ${filter.first} to ${filter.second}, Permission: $permission, Available: ${availableCalendars.size}")
                // Only proceed if we have permission and calendars are loaded
                if (permission == CalendarPermissionState.Available && availableCalendars.isNotEmpty()) {
                    loadSelectedCalendarDetails(settings.selectedCalendarIds)
                    readEventsFromCalendar(
                        selectedCalendarIds = settings.selectedCalendarIds,
                        dateRange = filter
                    )
                }
            }
        }
    }

    // Helper data class for combine
    private data class Tuple4<A, B, C, D>(
        val first: A,
        val second: B,
        val third: C,
        val fourth: D
    )

    private fun checkCalendarPermission() {
        viewModelScope.launch {
            when {
                hasCalendarPermission() -> {
                    _permissionState.value = CalendarPermissionState.Available
                }

                else -> {
                    _permissionState.value = CalendarPermissionState.PermissionRequired
                    _shouldShowPermissionDialog.value = true
                    _uiState.value = CalendarUiState.PermissionRequired
                }
            }
        }
    }

    private fun hasCalendarPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_CALENDAR
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun onPermissionGranted() {
        viewModelScope.launch {
            _shouldShowPermissionDialog.value = false
            _permissionState.value = CalendarPermissionState.Available
        }
    }

    fun onPermissionDenied() {
        viewModelScope.launch {
            _shouldShowPermissionDialog.value = false
            _permissionState.value = CalendarPermissionState.PermissionRequired
            _uiState.value = CalendarUiState.PermissionRequired
        }
    }

    fun retryPermissionRequest() {
        viewModelScope.launch {
            if (hasCalendarPermission()) {
                _permissionState.value = CalendarPermissionState.Available
            } else {
                _permissionState.value = CalendarPermissionState.PermissionRequired
                _shouldShowPermissionDialog.value = true
            }
        }
    }

    fun onGoToSettings() {
        Log.d("ImportEventsViewModel", "Navigate to app settings for calendar permission")
        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", context.packageName, null)
        ).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    private fun readEventsFromCalendar(
        selectedCalendarIds: Set<Long>,
        dateRange: Pair<Long?, Long?>
    ) {
        val eventList = mutableListOf<Event>()
        viewModelScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) {
                _uiState.value = CalendarUiState.Loading
            }

            if (!hasCalendarPermission()) {
                withContext(Dispatchers.Main) {
                    _uiState.value = CalendarUiState.PermissionRequired
                }
                return@launch
            }

            // Get date filter range
            val (filterStart, filterEnd) = dateRange

            if (filterStart == null || filterEnd == null) {
                withContext(Dispatchers.Main) {
                    _uiState.value = CalendarUiState.Error("Date filter is required")
                }
                return@launch
            }

            Log.d("ReadEvents", "Reading events - Calendar IDs: $selectedCalendarIds, Date: $filterStart to $filterEnd")

            // Build URI for calendar instances within the date range
            val uri = CalendarContract.Instances.CONTENT_URI.buildUpon()
                .appendPath(filterStart.toString())
                .appendPath(filterEnd.toString())
                .build()

            val projection = arrayOf(
                CalendarContract.Instances.EVENT_ID,
                CalendarContract.Instances.TITLE,
                CalendarContract.Instances.DESCRIPTION,
                CalendarContract.Instances.BEGIN,
                CalendarContract.Instances.END,
                CalendarContract.Instances.ALL_DAY,
                CalendarContract.Instances.CALENDAR_ID
            )

            // Build selection to filter by selected calendars
            var selection: String? = null
            var selectionArgs: Array<String>? = null

            if (selectedCalendarIds.isNotEmpty()) {
                // Create placeholders for each calendar ID
                val placeholders = selectedCalendarIds.joinToString(",") { "?" }
                selection = "${CalendarContract.Instances.CALENDAR_ID} IN ($placeholders)"
                selectionArgs = selectedCalendarIds.map { it.toString() }.toTypedArray()
                Log.d("ReadEvents", "Selection: $selection with args: ${selectionArgs.joinToString()}")
            } else {
                Log.d("ReadEvents", "No calendar filter - showing all calendars")
            }

            var count = 0
            try {
                val cursor = context.contentResolver.query(
                    uri,
                    projection,
                    selection,
                    selectionArgs,
                    "${CalendarContract.Instances.BEGIN} ASC"
                )

                cursor?.use { cur ->
                    val eventIdCol = cur.getColumnIndex(CalendarContract.Instances.EVENT_ID)
                    val titleCol = cur.getColumnIndex(CalendarContract.Instances.TITLE)
                    val descCol = cur.getColumnIndex(CalendarContract.Instances.DESCRIPTION)
                    val startCol = cur.getColumnIndex(CalendarContract.Instances.BEGIN)
                    val endCol = cur.getColumnIndex(CalendarContract.Instances.END)
                    val allDayCol = cur.getColumnIndex(CalendarContract.Instances.ALL_DAY)
                    val calendarIdCol = cur.getColumnIndex(CalendarContract.Instances.CALENDAR_ID)

                    while (cur.moveToNext()) {
                        try {
                            val title = cur.getString(titleCol) ?: "Untitled Event"
                            val description = cur.getString(descCol).orEmpty()
                            val dtStart = cur.getLong(startCol)
                            val dtEnd = cur.getLong(endCol).takeIf { !cur.isNull(endCol) && it != 0L }
                            val isAllDay = cur.getInt(allDayCol) == 1
                            val calId = cur.getLong(calendarIdCol)

                            // Adjust all-day events to local timezone
                            val startDate = Date(dtStart).let {
                                if (isAllDay) adjustAllDayToLocal(it) else it
                            }
                            val endDate = dtEnd?.let {
                                Date(it).let { date -> if (isAllDay) adjustAllDayToLocal(date) else date }
                            }

                            if (endDate != null) {
                                val event = Event(
                                    id = count++,
                                    eventTitle = title,
                                    eventDescription = description,
                                    eventStartTime = startDate,
                                    eventEndTime = endDate,
                                    allDayEvent = isAllDay
                                )
                                eventList.add(event)
                            }
                        } catch (e: Exception) {
                            Log.e("ImportEventsViewModel", "Failed to parse event instance", e)
                        }
                    }
                }

                Log.d("ReadEvents", "Loaded ${eventList.size} events")
            } catch (e: SecurityException) {
                Log.e("ReadEvents", "Security exception", e)
                withContext(Dispatchers.Main) {
                    _uiState.value = CalendarUiState.PermissionRequired
                }
                return@launch
            } catch (e: Exception) {
                Log.e("ImportEventsViewModel", "Failed to read calendar instances", e)
                withContext(Dispatchers.Main) {
                    _uiState.value = CalendarUiState.Error("Failed to load events: ${e.message}")
                }
                return@launch
            }

            // Update UI on Main thread
            withContext(Dispatchers.Main) {
                _importedEvents.value = eventList
                _uiState.value = CalendarUiState.Success(eventList)
            }
        }
    }

    private fun adjustAllDayToLocal(utcDate: Date): Date {
        val calendar = java.util.Calendar.getInstance().apply {
            time = utcDate
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }
        return calendar.time
    }

    fun toggleSelection(id: Int) {
        _selectedIds.value =
            if (_selectedIds.value.contains(id))
                _selectedIds.value - id
            else
                _selectedIds.value + id
    }

    fun toggleAllSelections() {
        val allIds = events.value.map { it.id }.toSet()

        if (_selectedIds.value.size == allIds.size && allIds.isNotEmpty()) {
            // All are selected → Deselect all
            _selectedIds.value = emptySet()
            _isAllSelected.value = false
        } else {
            // Not all selected → Select all
            _selectedIds.value = allIds
            _isAllSelected.value = true
        }
    }

    fun clearSelection() {
        _selectedIds.value = emptySet()
        _isAllSelected.value = false
    }

    fun importSelectedEvents(
        onFinish: () -> Unit
    ) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val selectedEvents = _importedEvents.value.filter {
                    it.id in selectedIds.value
                }.map {
                    it.copy(
                        id = 0 // This ensures that new id is generated instead of using id passed in the events
                    )
                }
                repository.insertAllEvents(selectedEvents)
            }
            onFinish()
        }
    }

    fun showDateFilter(
        show: Boolean
    ) {
        _showDateFilter.value = show
    }

    fun setDateFilter(
        dateFilter: Pair<Long?, Long?>
    ) {
        Log.d("SetDateFilter", "Setting date filter: ${dateFilter.first} to ${dateFilter.second}")
        _dateFilter.value = dateFilter
    }

    fun setSelectedCalendars(selectedCalendars: Set<CalendarInfo>) {
        viewModelScope.launch {
            Log.d("SetSelectedCalendars", "Selected calendars: $selectedCalendars")
            val ids = selectedCalendars.map { it.id }.toSet()
            withContext(Dispatchers.IO) {
                appSettingsRepository.setSelectedCalendarIds(ids)
            }
        }
    }
}