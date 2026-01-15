package com.a3.yearlyprogess.core.data.migration

import android.content.Context
import android.content.SharedPreferences
import android.icu.util.ULocale
import com.a3.yearlyprogess.core.domain.model.AppSettings
import com.a3.yearlyprogess.core.domain.model.NotificationSettings
import com.a3.yearlyprogess.core.domain.repository.AppSettingsRepository
import com.a3.yearlyprogess.core.util.CalculationType
import com.a3.yearlyprogess.core.util.ProgressSettings
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsMigrationManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val appSettingsRepository: AppSettingsRepository
) {
    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences("${context.packageName}_preferences", Context.MODE_PRIVATE)
    }

    fun hasLegacySettings(): Boolean {
        return prefs.contains("app_calculation_type") || 
               prefs.contains("progress_show_notification") ||
               prefs.contains("app_location_settings") ||
               prefs.contains("app_widget_decimal_point")
    }

    suspend fun migrate() {
        if (!hasLegacySettings()) return

        val currentSettings = appSettingsRepository.appSettings.first()

        // Notification settings
        val migratedNotificationSettings = NotificationSettings(
            progressShowNotification = prefs.getBoolean("progress_show_notification", currentSettings.notificationSettings.progressShowNotification),
            progressShowNotificationYear = prefs.getBoolean("progress_show_notification_year", currentSettings.notificationSettings.progressShowNotificationYear),
            progressShowNotificationMonth = prefs.getBoolean("progress_show_notification_month", currentSettings.notificationSettings.progressShowNotificationMonth),
            progressShowNotificationWeek = prefs.getBoolean("progress_show_notification_week", currentSettings.notificationSettings.progressShowNotificationWeek),
            progressShowNotificationDay = prefs.getBoolean("progress_show_notification_day", currentSettings.notificationSettings.progressShowNotificationDay)
        )

        // Calculation type
        val legacyCalculationType = prefs.getString("app_calculation_type", null)
        val calculationType = if (legacyCalculationType != null) {
            try { 
                if (legacyCalculationType == "1") CalculationType.ELAPSED else CalculationType.REMAINING
            } catch (e: Exception) { currentSettings.progressSettings.calculationType }
        } else {
            currentSettings.progressSettings.calculationType
        }

        // Calendar type
        val legacyCalendarType = prefs.getString("app_calendar_type", "default")
        val uLocale = if (legacyCalendarType != "default" && legacyCalendarType != null) {
            ULocale(ULocale.getDefault().toString() + "@calendar=${legacyCalendarType}")
        } else {
            ULocale.getDefault()
        }

        // Progress settings
        val migratedProgressSettings = ProgressSettings(
            localeTag = uLocale.toLanguageTag(),
            calculationType = calculationType,
            weekStartDay = prefs.getString("app_week_widget_start_day", "0")?.toInt() ?: currentSettings.progressSettings.weekStartDay,
            decimalDigits =
                prefs.getInt("app_widget_decimal_point", currentSettings.progressSettings.decimalDigits)
        )

        // Location settings
        val autoLocation = prefs.getBoolean("app_location_settings", currentSettings.automaticallyDetectLocation)

        val migratedSettings = currentSettings.copy(
            isFirstLaunch = false,
            notificationSettings = migratedNotificationSettings,
            progressSettings = migratedProgressSettings,
            automaticallyDetectLocation = autoLocation
        )

        // Save migrated settings
        appSettingsRepository.setAppSettings(migratedSettings)

        // Clear legacy settings
        clearLegacySettings()
    }

    private fun clearLegacySettings() {
        prefs.edit().clear().apply()
    }
}
