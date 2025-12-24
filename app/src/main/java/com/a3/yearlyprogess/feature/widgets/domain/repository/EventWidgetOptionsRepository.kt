package com.a3.yearlyprogess.feature.widgets.domain.repository

import com.a3.yearlyprogess.feature.widgets.domain.model.EventWidgetOptions
import com.a3.yearlyprogess.feature.widgets.domain.model.WidgetTheme
import kotlinx.coroutines.flow.Flow

interface EventWidgetOptionsRepository {
    fun getOptions(widgetId: Int): Flow<EventWidgetOptions>
    suspend fun updateOptions(widgetId: Int, options: EventWidgetOptions)
    suspend fun updateTheme(widgetId: Int, theme: WidgetTheme)
    suspend fun updateTimeStatusCounter(widgetId: Int, enabled: Boolean)
    suspend fun updateDynamicTimeStatusCounter(widgetId: Int, enabled: Boolean)
    suspend fun updateReplaceProgressWithTimeLeft(widgetId: Int, enabled: Boolean)
    suspend fun updateDecimalDigits(widgetId: Int, digits: Int)
    suspend fun updateBackgroundTransparency(widgetId: Int, transparency: Int)
    suspend fun updateFontScale(widgetId: Int, scale: Float)
    suspend fun updateShowEventImage(widgetId: Int, enabled: Boolean)
    suspend fun deleteWidgetOptions(widgetId: Int)

    suspend fun updateSelectedEventIds(eventIds: Set<Int>, widgetId: Int)
}