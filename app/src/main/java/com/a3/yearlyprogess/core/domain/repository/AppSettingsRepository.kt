package com.a3.yearlyprogess.core.domain.repository

import android.icu.util.ULocale
import com.a3.yearlyprogess.core.domain.model.AppSettings
import com.a3.yearlyprogess.core.util.CalculationType
import kotlinx.coroutines.flow.Flow

interface AppSettingsRepository {
    val appSettings: Flow<AppSettings>
    suspend fun setFirstLaunch(isFirstLaunch: Boolean)
    suspend fun setLocale(locale: ULocale)
    suspend fun setCalculationType(calculationType: CalculationType)
    suspend fun setWeekStartDay(weekStartDay: Int)
    suspend fun setDecimalDigits(decimalDigits: Int)
}