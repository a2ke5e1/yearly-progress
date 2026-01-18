package com.a3.yearlyprogess.feature.widgets.ui.config_screens

import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.a3.yearlyprogess.core.domain.repository.AppSettingsRepository
import com.a3.yearlyprogess.core.util.Log
import com.a3.yearlyprogess.core.util.TimePeriod
import com.a3.yearlyprogess.feature.widgets.domain.model.StandaloneWidgetOptions
import com.a3.yearlyprogess.feature.widgets.domain.model.StandaloneWidgetOptions.Companion.WidgetShape
import com.a3.yearlyprogess.feature.widgets.domain.model.WidgetTheme
import com.a3.yearlyprogess.feature.widgets.domain.repository.StandaloneWidgetOptionsRepository
import com.a3.yearlyprogess.feature.widgets.ui.StandaloneWidgetType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StandaloneWidgetConfigViewModel @Inject constructor(
    private val repository: StandaloneWidgetOptionsRepository,
    private val appSettingsRepository: AppSettingsRepository
) : ViewModel() {

    private val _options = MutableStateFlow(
        StandaloneWidgetOptions(
            theme = null,
            widgetType = null,
            widgetShape = WidgetShape.RECTANGULAR,
            timeLeftCounter = true,
            dynamicLeftCounter = false,
            replaceProgressWithDaysLeft = false,
            decimalPlaces = 2,
            backgroundTransparency = 100,
            fontScale = 1.0f
        )
    )
    val options: StateFlow<StandaloneWidgetOptions> = _options.asStateFlow()

    // Channel to send one-time events to the UI (like "Save Complete")
    private val _uiEvent = Channel<UiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    private var currentWidgetId: Int? = null

    fun setWidgetId(widgetId: Int, widgetType: StandaloneWidgetType? = null) {
        this.currentWidgetId = widgetId
        loadOptions(widgetId, widgetType)
    }

    private fun loadOptions(appWidgetId: Int, widgetType: StandaloneWidgetType? = null) {
        viewModelScope.launch {
            // Collect the flow from repository to populate the UI with saved data
            repository.getOptions(appWidgetId).collect { savedOptions ->
                // If theme is null, get it from AppSettings
                val effectiveTheme = savedOptions.theme ?: run {
                    val appSettings = appSettingsRepository.appSettings.first()
                    appSettings.appTheme
                }

                // Set the widget type if provided, otherwise use saved value
                _options.value = savedOptions.copy(
                    theme = effectiveTheme,
                    widgetType = widgetType ?: savedOptions.widgetType
                )
            }
        }
    }

    fun saveOptions() {
        val widgetId = currentWidgetId ?: return
        viewModelScope.launch {
            Log.d("StandaloneWidgetConfigViewModel", "during saveOptions ${_options.value}")
            repository.updateOptions(widgetId, _options.value)
            _uiEvent.send(UiEvent.SaveSuccess)
        }
    }

    fun updateTheme(theme: WidgetTheme) {
        _options.update { it.copy(theme = theme) }
        Log.d("StandaloneWidgetConfigViewModel", "after theme selected ${_options.value}")
    }

    fun updateWidgetShape(shape: WidgetShape) {
        _options.update { it.copy(widgetShape = shape) }
        Log.d("StandaloneWidgetConfigViewModel", "after shape selected ${_options.value}")
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