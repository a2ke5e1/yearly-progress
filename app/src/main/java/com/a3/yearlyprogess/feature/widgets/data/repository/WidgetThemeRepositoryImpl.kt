package com.a3.yearlyprogess.feature.widgets.data.repository

import com.a3.yearlyprogess.feature.widgets.data.datastore.WidgetThemeDataStore
import com.a3.yearlyprogess.feature.widgets.domain.model.WidgetTheme
import com.a3.yearlyprogess.feature.widgets.domain.repository.WidgetThemeRepository
import kotlinx.coroutines.flow.Flow

class WidgetThemeRepositoryImpl(
    private val dataStore: WidgetThemeDataStore
) : WidgetThemeRepository {

    override fun getSelectedTheme(): Flow<WidgetTheme> = dataStore.themeFlow
    override fun useDynamicColors(): Flow<Boolean> = dataStore.useDynamicColorsFlow
    override suspend fun saveTheme(theme: WidgetTheme) = dataStore.saveTheme(theme)
    override suspend fun setUseDynamicColors(enabled: Boolean) = dataStore.setUseDynamicColors(enabled)
}
