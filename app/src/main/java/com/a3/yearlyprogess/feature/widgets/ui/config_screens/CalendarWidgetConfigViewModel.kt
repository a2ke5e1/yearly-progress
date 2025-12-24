package com.a3.yearlyprogess.feature.widgets.ui.config_screens

import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.a3.yearlyprogess.core.util.Log
import com.a3.yearlyprogess.feature.widgets.domain.model.CalendarWidgetOptions
import com.a3.yearlyprogess.feature.widgets.domain.model.WidgetTheme
import com.a3.yearlyprogess.feature.widgets.domain.repository.CalendarWidgetOptionsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CalendarWidgetConfigViewModel @Inject constructor(
    private val repository: CalendarWidgetOptionsRepository
) : ViewModel() {

    private val _options = MutableStateFlow(
        CalendarWidgetOptions(
            theme = if (Build.VERSION.SDK_INT > Build.VERSION_CODES.S) WidgetTheme.DYNAMIC else WidgetTheme.DEFAULT,
            timeStatusCounter = true,
            dynamicTimeStatusCounter = false,
            replaceProgressWithTimeLeft = false,
            decimalDigits = 2,
            backgroundTransparency = 100,
            fontScale = 1.0f,
            selectedCalendarIds = emptySet()
        )
    )
    val options: StateFlow<CalendarWidgetOptions> = _options.asStateFlow()

    // Channel to send one-time events to the UI (like "Save Complete")
    private val _uiEvent = Channel<UiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    private var currentWidgetId: Int? = null

    fun setWidgetId(widgetId: Int) {
        this.currentWidgetId = widgetId
        loadOptions(widgetId)
    }

    private fun loadOptions(appWidgetId: Int) {
        viewModelScope.launch {
            // Collect the flow from repository to populate the UI with saved data
            repository.getOptions(appWidgetId).collect { savedOptions ->
                _options.value = savedOptions
            }
        }
    }

    fun saveOptions() {
        val widgetId = currentWidgetId ?: return
        viewModelScope.launch {
            Log.d("CalendarWidgetConfigViewModel", "Saving options: ${_options.value}")
            repository.updateOptions(widgetId, _options.value)
            _uiEvent.send(UiEvent.SaveSuccess)
        }
    }

    fun updateTheme(theme: WidgetTheme) {
        _options.update { it.copy(theme = theme) }
        Log.d("CalendarWidgetConfigViewModel", "Updated theme: ${_options.value}")
    }

    fun updateTimeStatusCounter(enabled: Boolean) {
        _options.update { it.copy(timeStatusCounter = enabled) }
    }

    fun updateDynamicTimeStatusCounter(enabled: Boolean) {
        _options.update { it.copy(dynamicTimeStatusCounter = enabled) }
    }

    fun updateReplaceProgressWithTimeLeft(enabled: Boolean) {
        _options.update { it.copy(replaceProgressWithTimeLeft = enabled) }
    }

    fun updateDecimalDigits(digits: Int) {
        val validated = digits.coerceIn(0, 5)
        _options.update { it.copy(decimalDigits = validated) }
    }

    fun updateBackgroundTransparency(transparency: Int) {
        val validated = transparency.coerceIn(0, 100)
        _options.update { it.copy(backgroundTransparency = validated) }
    }

    fun updateFontScale(scale: Float) {
        val validated = scale.coerceIn(0.5f, 2.0f)
        _options.update { it.copy(fontScale = validated) }
    }

    fun updateSelectedCalendarIds(calendarIds: Set<Long>) {
        Log.d("CalendarWidgetConfigViewModel", "Updating calendar ids: $calendarIds")
        _options.update { it.copy(selectedCalendarIds = calendarIds) }
        Log.d("CalendarWidgetConfigViewModel", "Updated options: ${_options.value}")
    }

    fun toggleCalendarSelection(calendarId: Long) {
        _options.update { current ->
            val updated = if (calendarId in current.selectedCalendarIds) {
                current.selectedCalendarIds - calendarId
            } else {
                current.selectedCalendarIds + calendarId
            }
            current.copy(selectedCalendarIds = updated)
        }
        Log.d("CalendarWidgetConfigViewModel", "After toggle: ${_options.value}")
    }

    sealed class UiEvent {
        data object SaveSuccess : UiEvent()
    }
}