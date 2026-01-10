package com.a3.yearlyprogess.feature.widgets.domain.repository

import com.a3.yearlyprogess.feature.widgets.domain.model.AllInWidgetOptions
import com.a3.yearlyprogess.feature.widgets.domain.model.WidgetTheme
import kotlinx.coroutines.flow.Flow

interface AllInWidgetOptionsRepository {
    fun getOptions(widgetId: Int): Flow<AllInWidgetOptions>
    suspend fun updateOptions(widgetId: Int, options: AllInWidgetOptions)
    suspend fun updateTheme(widgetId: Int, theme: WidgetTheme)
    suspend fun updateShowDay(widgetId: Int, show: Boolean)
    suspend fun updateShowWeek(widgetId: Int, show: Boolean)
    suspend fun updateShowMonth(widgetId: Int, show: Boolean)
    suspend fun updateShowYear(widgetId: Int, show: Boolean)
    suspend fun updateTimeLeftCounter(widgetId: Int, enabled: Boolean)
    suspend fun updateDynamicLeftCounter(widgetId: Int, enabled: Boolean)
    suspend fun updateReplaceProgressWithDaysLeft(widgetId: Int, enabled: Boolean)
    suspend fun updateDecimalPlaces(widgetId: Int, places: Int)
    suspend fun updateBackgroundTransparency(widgetId: Int, transparency: Int)
    suspend fun updateFontScale(widgetId: Int, scale: Float)
    suspend fun deleteWidgetOptions(widgetId: Int)
}