package com.a3.yearlyprogess.feature.widgets.data.repository

import com.a3.yearlyprogess.feature.widgets.data.datastore.EventWidgetOptionsDataStore
import com.a3.yearlyprogess.feature.widgets.domain.model.EventWidgetOptions
import com.a3.yearlyprogess.feature.widgets.domain.model.WidgetTheme
import com.a3.yearlyprogess.feature.widgets.domain.repository.EventWidgetOptionsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EventWidgetOptionsRepositoryImpl @Inject constructor(
    private val dataStore: EventWidgetOptionsDataStore
) : EventWidgetOptionsRepository {

    override fun getOptions(widgetId: Int): Flow<EventWidgetOptions> =
        dataStore.getOptionsFlow(widgetId)

    override suspend fun updateOptions(widgetId: Int, options: EventWidgetOptions) =
        dataStore.updateOptions(widgetId, options)

    override suspend fun updateTheme(widgetId: Int, theme: WidgetTheme) =
        dataStore.updateTheme(widgetId, theme)

    override suspend fun updateTimeStatusCounter(widgetId: Int, enabled: Boolean) =
        dataStore.updateTimeStatusCounter(widgetId, enabled)

    override suspend fun updateDynamicTimeStatusCounter(widgetId: Int, enabled: Boolean) =
        dataStore.updateDynamicTimeStatusCounter(widgetId, enabled)

    override suspend fun updateReplaceProgressWithTimeLeft(
        widgetId: Int,
        enabled: Boolean
    )  =  dataStore.updateReplaceProgressWithTimeLeft(widgetId, enabled)

    override suspend fun updateDecimalDigits(widgetId: Int, digits: Int) =
        dataStore.updateDecimalDigits(widgetId, digits)

    override suspend fun updateBackgroundTransparency(widgetId: Int, transparency: Int) =
        dataStore.updateBackgroundTransparency(widgetId, transparency)

    override suspend fun updateFontScale(widgetId: Int, scale: Float) =
        dataStore.updateFontScale(widgetId, scale)

    override suspend fun updateShowEventImage(widgetId: Int, enabled: Boolean) =
        dataStore.updateShowEventImage(widgetId, enabled)

    override suspend fun deleteWidgetOptions(widgetId: Int) =
        dataStore.deleteWidgetOptions(widgetId)

    override suspend fun updateSelectedEventIds(eventIds: Set<Int>, widgetId: Int) =
        dataStore.updateSelectedEventIds(eventIds, widgetId)

}