package com.a3.yearlyprogess.feature.widgets.domain.repository

import com.a3.yearlyprogess.feature.widgets.domain.model.WidgetTheme
import kotlinx.coroutines.flow.Flow

interface WidgetThemeRepository {
    fun getSelectedTheme(): Flow<WidgetTheme>
    fun useDynamicColors(): Flow<Boolean>
    suspend fun saveTheme(theme: WidgetTheme)
    suspend fun setUseDynamicColors(enabled: Boolean)
}
