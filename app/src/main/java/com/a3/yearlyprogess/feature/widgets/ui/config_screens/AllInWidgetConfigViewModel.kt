package com.a3.yearlyprogess.feature.widgets.ui.config_screens

import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.a3.yearlyprogess.core.util.Log
import com.a3.yearlyprogess.feature.widgets.domain.model.AllInWidgetOptions
import com.a3.yearlyprogess.feature.widgets.domain.model.WidgetTheme
import com.a3.yearlyprogess.feature.widgets.domain.repository.AllInWidgetOptionsRepository
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
class AllInWidgetConfigViewModel @Inject constructor(
    private val repository: AllInWidgetOptionsRepository
) : ViewModel() {

    private val _options = MutableStateFlow(
        AllInWidgetOptions(
            theme = if (Build.VERSION.SDK_INT > Build.VERSION_CODES.S) WidgetTheme.DYNAMIC else WidgetTheme.DEFAULT,
            showDay = true,
            showWeek = true,
            showMonth = true,
            showYear = true,
            timeLeftCounter = true,
            dynamicLeftCounter = false,
            replaceProgressWithDaysLeft = false,
            decimalPlaces = 2,
            backgroundTransparency = 100,
            fontScale = 1.0f
        )
    )
    val options: StateFlow<AllInWidgetOptions> = _options.asStateFlow()

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
            Log.d("AllInWidgetConfigViewModel", "Saving options: ${_options.value}")
            repository.updateOptions(widgetId, _options.value)
            _uiEvent.send(UiEvent.SaveSuccess)
        }
    }

    fun updateTheme(theme: WidgetTheme) {
        _options.update { it.copy(theme = theme) }
        Log.d("AllInWidgetConfigViewModel", "Updated theme: ${_options.value}")
    }

    fun updateShowDay(show: Boolean) {
        _options.update { it.copy(showDay = show) }
    }

    fun updateShowWeek(show: Boolean) {
        _options.update { it.copy(showWeek = show) }
    }

    fun updateShowMonth(show: Boolean) {
        _options.update { it.copy(showMonth = show) }
    }

    fun updateShowYear(show: Boolean) {
        _options.update { it.copy(showYear = show) }
    }

    fun updateTimeLeftCounter(enabled: Boolean) {
        _options.update { it.copy(timeLeftCounter = enabled) }
    }

    fun updateDynamicLeftCounter(enabled: Boolean) {
        _options.update { it.copy(dynamicLeftCounter = enabled) }
    }

    fun updateReplaceProgressWithDaysLeft(enabled: Boolean) {
        _options.update { it.copy(replaceProgressWithDaysLeft = enabled) }
    }

    fun updateDecimalPlaces(places: Int) {
        val validated = places.coerceIn(0, 5)
        _options.update { it.copy(decimalPlaces = validated) }
    }

    fun updateBackgroundTransparency(transparency: Int) {
        val validated = transparency.coerceIn(0, 100)
        _options.update { it.copy(backgroundTransparency = validated) }
    }

    fun updateFontScale(scale: Float) {
        val validated = scale.coerceIn(0.5f, 2.0f)
        _options.update { it.copy(fontScale = validated) }
    }

    sealed class UiEvent {
        data object SaveSuccess : UiEvent()
    }
}