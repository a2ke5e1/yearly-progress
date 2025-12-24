package com.a3.yearlyprogess.feature.widgets.domain.repository

import com.a3.yearlyprogess.feature.widgets.domain.model.CalendarWidgetOptions
import kotlinx.coroutines.flow.Flow

interface CalendarWidgetOptionsRepository {
    fun getOptions(appWidgetId: Int): Flow<CalendarWidgetOptions>
    suspend fun updateOptions(appWidgetId: Int, options: CalendarWidgetOptions)
    suspend fun deleteOptions(appWidgetId: Int)
}