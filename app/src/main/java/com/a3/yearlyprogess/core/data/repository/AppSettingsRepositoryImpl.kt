package com.a3.yearlyprogess.core.data.repository

import android.content.Context
import android.icu.util.Calendar
import android.icu.util.ULocale
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.a3.yearlyprogess.core.domain.model.AppSettings
import com.a3.yearlyprogess.core.domain.model.NotificationSettings
import com.a3.yearlyprogess.core.domain.repository.AppSettingsRepository
import com.a3.yearlyprogess.core.util.CalculationType
import com.a3.yearlyprogess.core.util.Log
import com.a3.yearlyprogess.core.util.ProgressSettings
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.collections.map
import kotlin.collections.toSet


private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "yearly_progress_settings")


@Singleton
class AppSettingsRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context
) : AppSettingsRepository {

    override val appSettings: Flow<AppSettings> = context.dataStore.data.map { preferences ->
        val uLocale = ULocale(preferences[PreferencesKeys.LOCALE] ?: ULocale.getDefault().toString())
        val calendar = Calendar.getInstance(uLocale)
        Log.d("appSettings", preferences.toString())
        AppSettings(
            isFirstLaunch = preferences[PreferencesKeys.IS_FIRST_LAUNCH] ?: true,
            progressSettings = ProgressSettings(
                localeTag = uLocale.toLanguageTag(),
                calculationType = CalculationType.valueOf(
                    preferences[PreferencesKeys.CALCULATION_TYPE] ?: CalculationType.ELAPSED.name
                ),
                weekStartDay = preferences[PreferencesKeys.WEEK_START_DAY] ?: calendar.firstDayOfWeek,
                decimalDigits = preferences[PreferencesKeys.DECIMAL_DIGITS] ?: 13
            ),
            notificationSettings = NotificationSettings(
                progressShowNotification = preferences[PreferencesKeys.PROGRESS_SHOW_NOTIFICATION] ?: false,
                progressShowNotificationYear = preferences[PreferencesKeys.PROGRESS_SHOW_NOTIFICATION_YEAR] ?: true,
                progressShowNotificationMonth = preferences[PreferencesKeys.PROGRESS_SHOW_NOTIFICATION_MONTH] ?: true,
                progressShowNotificationWeek = preferences[PreferencesKeys.PROGRESS_SHOW_NOTIFICATION_WEEK] ?: true,
                progressShowNotificationDay = preferences[PreferencesKeys.PROGRESS_SHOW_NOTIFICATION_DAY] ?: true
            ),
            selectedCalendarIds = preferences[PreferencesKeys.SELECTED_CALENDAR_IDS]
                ?.map { it.toLong() }
                ?.toSet() ?: emptySet(),
            automaticallyDetectLocation = preferences[PreferencesKeys.AUTOMATICALLY_DETECT_LOCATION] ?: false
        )
    }

    override suspend fun setFirstLaunch(isFirstLaunch: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_FIRST_LAUNCH] = isFirstLaunch
        }
    }

    override suspend fun setLocale(locale: ULocale) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.LOCALE] = locale.toString()
        }
    }

    override suspend fun setCalculationType(calculationType: CalculationType) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.CALCULATION_TYPE] = calculationType.name
        }
    }

    override suspend fun setWeekStartDay(weekStartDay: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.WEEK_START_DAY] = weekStartDay
        }
    }

    override suspend fun setDecimalDigits(decimalDigits: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DECIMAL_DIGITS] = decimalDigits
        }
    }

    override suspend fun setSelectedCalendarIds(calendarIds: Set<Long>) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SELECTED_CALENDAR_IDS] = calendarIds.map { it.toString() }.toSet()
        }
    }

    override suspend fun setAutomaticallyDetectLocation(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.AUTOMATICALLY_DETECT_LOCATION] = enabled
        }
    }

    override suspend fun setProgressShowNotification(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.PROGRESS_SHOW_NOTIFICATION] = enabled
        }
    }

    override suspend fun setProgressShowNotificationYear(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.PROGRESS_SHOW_NOTIFICATION_YEAR] = enabled
        }
    }

    override suspend fun setProgressShowNotificationMonth(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.PROGRESS_SHOW_NOTIFICATION_MONTH] = enabled
        }
    }

    override suspend fun setProgressShowNotificationWeek(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.PROGRESS_SHOW_NOTIFICATION_WEEK] = enabled
        }
    }

    override suspend fun setProgressShowNotificationDay(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.PROGRESS_SHOW_NOTIFICATION_DAY] = enabled
        }
    }

    override suspend fun setAppSettings(appSettings: AppSettings) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_FIRST_LAUNCH] = appSettings.isFirstLaunch
            preferences[PreferencesKeys.LOCALE] = appSettings.progressSettings.localeTag
            preferences[PreferencesKeys.CALCULATION_TYPE] = appSettings.progressSettings.calculationType.name
            preferences[PreferencesKeys.WEEK_START_DAY] = appSettings.progressSettings.weekStartDay
            preferences[PreferencesKeys.DECIMAL_DIGITS] = appSettings.progressSettings.decimalDigits
            preferences[PreferencesKeys.SELECTED_CALENDAR_IDS] = appSettings.selectedCalendarIds.map { it.toString() }.toSet()

            // Notification settings
            preferences[PreferencesKeys.PROGRESS_SHOW_NOTIFICATION] = appSettings.notificationSettings.progressShowNotification
            preferences[PreferencesKeys.PROGRESS_SHOW_NOTIFICATION_YEAR] = appSettings.notificationSettings.progressShowNotificationYear
            preferences[PreferencesKeys.PROGRESS_SHOW_NOTIFICATION_MONTH] = appSettings.notificationSettings.progressShowNotificationMonth
            preferences[PreferencesKeys.PROGRESS_SHOW_NOTIFICATION_WEEK] = appSettings.notificationSettings.progressShowNotificationWeek
            preferences[PreferencesKeys.PROGRESS_SHOW_NOTIFICATION_DAY] = appSettings.notificationSettings.progressShowNotificationDay
        }
    }

    private object PreferencesKeys {
        val IS_FIRST_LAUNCH = booleanPreferencesKey("is_first_launch")
        val LOCALE = stringPreferencesKey("locale")
        val CALCULATION_TYPE = stringPreferencesKey("calculation_type")
        val WEEK_START_DAY = intPreferencesKey("week_start_day")
        val DECIMAL_DIGITS = intPreferencesKey("decimal_digits")
        val SELECTED_CALENDAR_IDS = stringSetPreferencesKey("selected_calendar_ids")
        val AUTOMATICALLY_DETECT_LOCATION = booleanPreferencesKey("automatically_detect_location")

        // Notification preferences
        val PROGRESS_SHOW_NOTIFICATION = booleanPreferencesKey("progress_show_notification")
        val PROGRESS_SHOW_NOTIFICATION_YEAR = booleanPreferencesKey("progress_show_notification_year")
        val PROGRESS_SHOW_NOTIFICATION_MONTH = booleanPreferencesKey("progress_show_notification_month")
        val PROGRESS_SHOW_NOTIFICATION_WEEK = booleanPreferencesKey("progress_show_notification_week")
        val PROGRESS_SHOW_NOTIFICATION_DAY = booleanPreferencesKey("progress_show_notification_day")
    }
}