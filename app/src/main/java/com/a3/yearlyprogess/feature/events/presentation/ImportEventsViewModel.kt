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
import com.a3.yearlyprogess.core.domain.repository.AppSettingsRepository
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

    init {
        checkCalendarPermission()

        viewModelScope.launch {
            permissionState.collect { state ->
                if (state == CalendarPermissionState.Available) {
                    readEventsFromCalendar()
                }
            }
        }

        viewModelScope.launch {
            dateFilter.collect {
                if (permissionState.value == CalendarPermissionState.Available) {
                    readEventsFromCalendar()
                }
            }
        }
    }

    private fun checkCalendarPermission() {
        viewModelScope.launch {
            when {
                hasCalendarPermission() -> {
                    _permissionState.value = CalendarPermissionState.Available
                    readEventsFromCalendar()
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
            readEventsFromCalendar()
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
                readEventsFromCalendar()
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

    private fun readEventsFromCalendar() {
        val eventList = mutableListOf<Event>()
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = CalendarUiState.Loading

            if (!hasCalendarPermission()) {
                _uiState.value = CalendarUiState.PermissionRequired
                return@launch
            }

            val uri = CalendarContract.Events.CONTENT_URI
            val projection = arrayOf(
                CalendarContract.Events._ID,
                CalendarContract.Events.TITLE,
                CalendarContract.Events.DESCRIPTION,
                CalendarContract.Events.DTSTART,
                CalendarContract.Events.DTEND,
                CalendarContract.Events.ALL_DAY
            )

            // Build selection and selectionArgs based on date filter
            val (filterStart, filterEnd) = dateFilter.value

            var selection: String? = null
            var selectionArgs: Array<String>? = null

            if (filterStart != null && filterEnd != null) {
                // Overlap condition: event starts before filterEnd AND ends after filterStart
                selection = """
                (${CalendarContract.Events.DTSTART} <= ? AND ${CalendarContract.Events.DTEND} >= ?)
                OR (${CalendarContract.Events.DTSTART} >= ? AND ${CalendarContract.Events.DTSTART} <= ?)
                OR (${CalendarContract.Events.DTEND} >= ? AND ${CalendarContract.Events.DTEND} <= ?)
                OR (${CalendarContract.Events.DTSTART} <= ? AND (${CalendarContract.Events.DTEND} IS NULL OR ${CalendarContract.Events.DTEND} >= ?))
            """.trimIndent()

                selectionArgs = arrayOf(
                    filterEnd.toString(), filterStart.toString(),  // start before end AND end after start
                    filterStart.toString(), filterEnd.toString(),   // event starts within range
                    filterStart.toString(), filterEnd.toString(),   // event ends within range
                    filterStart.toString(), filterStart.toString()  // ongoing events at start time
                )
            } else if (filterStart != null) {
                // Only start date: show events starting on or after this date
                selection = "${CalendarContract.Events.DTSTART} >= ?"
                selectionArgs = arrayOf(filterStart.toString())
            } else if (filterEnd != null) {
                // Only end date: show events ending on or before this date
                selection = "${CalendarContract.Events.DTEND} <= ? OR ${CalendarContract.Events.DTEND} IS NULL"
                selectionArgs = arrayOf(filterEnd.toString())
            }

            var count = 0
            try {
                val cursor = context.contentResolver.query(
                    uri,
                    projection,
                    selection,
                    selectionArgs,
                    "${CalendarContract.Events.DTSTART} DESC"
                )

                cursor?.use { cur ->
                    val idCol = cur.getColumnIndex(CalendarContract.Events._ID)
                    val titleCol = cur.getColumnIndex(CalendarContract.Events.TITLE)
                    val descCol = cur.getColumnIndex(CalendarContract.Events.DESCRIPTION)
                    val startCol = cur.getColumnIndex(CalendarContract.Events.DTSTART)
                    val endCol = cur.getColumnIndex(CalendarContract.Events.DTEND)
                    val allDayCol = cur.getColumnIndex(CalendarContract.Events.ALL_DAY)

                    while (cur.moveToNext()) {
                        try {
                            val title = cur.getString(titleCol) ?: "Untitled Event"
                            val description = cur.getString(descCol).orEmpty()
                            val dtStart = cur.getLong(startCol)
                            val dtEnd = cur.getLong(endCol).takeIf { !cur.isNull(endCol) && it != 0L }
                            val isAllDay = cur.getInt(allDayCol) == 1

                            // Optional: Adjust all-day events to local timezone (they are usually in UTC midnight)
                            val startDate = Date(dtStart).let {
                                if (isAllDay) adjustAllDayToLocal(it) else it
                            }
                            val endDate = dtEnd?.let {
                                Date(it).let { date -> if (isAllDay) adjustAllDayToLocal(date) else date }
                            }

                            val event = Event(
                                id = count++,
                                eventTitle = title,
                                eventDescription = description,
                                eventStartTime = startDate,
                                eventEndTime = endDate!!,
                                allDayEvent = isAllDay
                            )
                            eventList.add(event)
                        } catch (e: Exception) {
                            Log.e("ImportEventsViewModel", "Failed to parse event", e)
                        }
                    }
                }
            } catch (e: SecurityException) {
                withContext(Dispatchers.Main) {
                    _uiState.value = CalendarUiState.PermissionRequired
                }
                return@launch
            } catch (e: Exception) {
                Log.e("ImportEventsViewModel", "Failed to read calendar", e)
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
            onFinish()  // now always on Main
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
        _dateFilter.value = dateFilter
    }

}