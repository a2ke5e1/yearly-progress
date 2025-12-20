package com.a3.yearlyprogess.feature.widgets.data.repository

import com.a3.yearlyprogess.core.util.TimePeriod
import com.a3.yearlyprogess.feature.widgets.data.datastore.StandaloneWidgetOptionsDataStore
import com.a3.yearlyprogess.feature.widgets.domain.model.StandaloneWidgetOptions
import com.a3.yearlyprogess.feature.widgets.domain.model.StandaloneWidgetOptions.Companion.WidgetShape
import com.a3.yearlyprogess.feature.widgets.domain.model.WidgetTheme
import com.a3.yearlyprogess.feature.widgets.domain.repository.StandaloneWidgetOptionsRepository
import com.a3.yearlyprogess.feature.widgets.ui.StandaloneWidgetType
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StandaloneWidgetOptionsRepositoryImpl @Inject constructor(
    private val dataStore: StandaloneWidgetOptionsDataStore
) : StandaloneWidgetOptionsRepository {

    override fun getOptions(widgetId: Int): Flow<StandaloneWidgetOptions> =
        dataStore.getOptionsFlow(widgetId)

    override suspend fun updateOptions(widgetId: Int, options: StandaloneWidgetOptions) =
        dataStore.updateOptions(widgetId, options)

    override suspend fun updateTheme(widgetId: Int, theme: WidgetTheme) =
        dataStore.updateTheme(widgetId, theme)

    override suspend fun updateWidgetType(widgetId: Int, widgetType: StandaloneWidgetType?) =
        dataStore.updateWidgetType(widgetId, widgetType)

    override suspend fun updateWidgetShape(widgetId: Int, shape: WidgetShape) =
        dataStore.updateWidgetShape(widgetId, shape)

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