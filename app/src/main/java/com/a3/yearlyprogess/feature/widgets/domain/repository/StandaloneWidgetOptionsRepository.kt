package com.a3.yearlyprogess.feature.widgets.domain.repository

import com.a3.yearlyprogess.feature.widgets.domain.model.StandaloneWidgetOptions
import com.a3.yearlyprogess.feature.widgets.domain.model.StandaloneWidgetOptions.Companion.WidgetShape
import com.a3.yearlyprogess.feature.widgets.domain.model.WidgetTheme
import com.a3.yearlyprogess.feature.widgets.ui.StandaloneWidgetType
import kotlinx.coroutines.flow.Flow

interface StandaloneWidgetOptionsRepository {
    fun getOptions(widgetId: Int): Flow<StandaloneWidgetOptions>
    suspend fun updateOptions(widgetId: Int, options: StandaloneWidgetOptions)
    suspend fun updateTheme(widgetId: Int, theme: WidgetTheme)
    suspend fun updateWidgetType(widgetId: Int, widgetType: StandaloneWidgetType?)
    suspend fun updateWidgetShape(widgetId: Int, shape: WidgetShape)
    suspend fun updateTimeLeftCounter(widgetId: Int, enabled: Boolean)
    suspend fun updateDynamicLeftCounter(widgetId: Int, enabled: Boolean)
    suspend fun updateReplaceProgressWithDaysLeft(widgetId: Int, enabled: Boolean)
    suspend fun updateDecimalPlaces(widgetId: Int, places: Int)
    suspend fun updateBackgroundTransparency(widgetId: Int, transparency: Int)
    suspend fun updateFontScale(widgetId: Int, scale: Float)
    suspend fun deleteWidgetOptions(widgetId: Int)
}