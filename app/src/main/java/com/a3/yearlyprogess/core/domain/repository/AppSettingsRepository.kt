package com.a3.yearlyprogess.core.domain.repository

import android.icu.util.ULocale
import com.a3.yearlyprogess.core.domain.model.AppSettings
import com.a3.yearlyprogess.core.util.CalculationType
import kotlinx.coroutines.flow.Flow

interface AppSettingsRepository {
    val appSettings: Flow<AppSettings>

    suspend fun setAppSettings(appSettings: AppSettings)

    suspend fun setFirstLaunch(isFirstLaunch: Boolean)
    suspend fun setLocale(locale: ULocale)
    suspend fun setCalculationType(calculationType: CalculationType)
    suspend fun setWeekStartDay(weekStartDay: Int)
    suspend fun setDecimalDigits(decimalDigits: Int)
    suspend fun setSelectedCalendarIds(calendarIds: Set<Long>)
    suspend fun setAutomaticallyDetectLocation(enabled: Boolean)

    // Notification settings
    suspend fun setProgressShowNotification(enabled: Boolean)
    suspend fun setProgressShowNotificationYear(enabled: Boolean)
    suspend fun setProgressShowNotificationMonth(enabled: Boolean)
    suspend fun setProgressShowNotificationWeek(enabled: Boolean)
    suspend fun setProgressShowNotificationDay(enabled: Boolean)
}