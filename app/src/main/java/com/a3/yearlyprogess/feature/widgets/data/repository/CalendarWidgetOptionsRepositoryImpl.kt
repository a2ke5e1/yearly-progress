package com.a3.yearlyprogess.feature.widgets.data.repository

import com.a3.yearlyprogess.feature.widgets.data.datastore.CalendarWidgetOptionsDataStore
import com.a3.yearlyprogess.feature.widgets.domain.model.CalendarWidgetOptions
import com.a3.yearlyprogess.feature.widgets.domain.repository.CalendarWidgetOptionsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class CalendarWidgetOptionsRepositoryImpl @Inject constructor(
    private val dataStore: CalendarWidgetOptionsDataStore
) : CalendarWidgetOptionsRepository {

    override fun getOptions(appWidgetId: Int): Flow<CalendarWidgetOptions> {
        return dataStore.getOptions(appWidgetId)
    }

    override suspend fun updateOptions(appWidgetId: Int, options: CalendarWidgetOptions) {
        dataStore.updateOptions(appWidgetId, options)
    }

    override suspend fun deleteOptions(appWidgetId: Int) {
        dataStore.deleteOptions(appWidgetId)
    }
}