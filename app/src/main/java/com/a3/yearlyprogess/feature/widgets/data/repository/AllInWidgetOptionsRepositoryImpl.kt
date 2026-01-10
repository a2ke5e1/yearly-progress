package com.a3.yearlyprogess.feature.widgets.data.repository

import com.a3.yearlyprogess.feature.widgets.data.datastore.AllInWidgetOptionsDataStore
import com.a3.yearlyprogess.feature.widgets.domain.model.AllInWidgetOptions
import com.a3.yearlyprogess.feature.widgets.domain.model.WidgetTheme
import com.a3.yearlyprogess.feature.widgets.domain.repository.AllInWidgetOptionsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AllInWidgetOptionsRepositoryImpl @Inject constructor(
    private val dataStore: AllInWidgetOptionsDataStore
) : AllInWidgetOptionsRepository {

    override fun getOptions(widgetId: Int): Flow<AllInWidgetOptions> =
        dataStore.getOptionsFlow(widgetId)

    override suspend fun updateOptions(widgetId: Int, options: AllInWidgetOptions) =
        dataStore.updateOptions(widgetId, options)

    override suspend fun updateTheme(widgetId: Int, theme: WidgetTheme) =
        dataStore.updateTheme(widgetId, theme)

    override suspend fun updateShowDay(widgetId: Int, show: Boolean) =
        dataStore.updateShowDay(widgetId, show)

    override suspend fun updateShowWeek(widgetId: Int, show: Boolean) =
        dataStore.updateShowWeek(widgetId, show)

    override suspend fun updateShowMonth(widgetId: Int, show: Boolean) =
        dataStore.updateShowMonth(widgetId, show)

    override suspend fun updateShowYear(widgetId: Int, show: Boolean) =
        dataStore.updateShowYear(widgetId, show)

    override suspend fun updateTimeLeftCounter(widgetId: Int, enabled: Boolean) =
        dataStore.updateTimeLeftCounter(widgetId, enabled)

    override suspend fun updateDynamicLeftCounter(widgetId: Int, enabled: Boolean) =
        dataStore.updateDynamicLeftCounter(widgetId, enabled)

    override suspend fun updateReplaceProgressWithDaysLeft(widgetId: Int, enabled: Boolean) =
        dataStore.updateReplaceProgressWithDaysLeft(widgetId, enabled)

    override suspend fun updateDecimalPlaces(widgetId: Int, places: Int) =
        dataStore.updateDecimalPlaces(widgetId, places)

    override suspend fun updateBackgroundTransparency(widgetId: Int, transparency: Int) =
        dataStore.updateBackgroundTransparency(widgetId, transparency)

    override suspend fun updateFontScale(widgetId: Int, scale: Float) =
        dataStore.updateFontScale(widgetId, scale)

    override suspend fun deleteWidgetOptions(widgetId: Int) =
        dataStore.deleteWidgetOptions(widgetId)
}