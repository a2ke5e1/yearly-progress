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
import androidx.datastore.preferences.preferencesDataStore
import com.a3.yearlyprogess.core.domain.model.AppSettings
import com.a3.yearlyprogess.core.domain.repository.AppSettingsRepository
import com.a3.yearlyprogess.core.util.CalculationType
import com.a3.yearlyprogess.core.util.ProgressSettings
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton


private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "yearly_progress_settings")


@Singleton
class AppSettingsRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context
) : AppSettingsRepository {

    override val appSettings: Flow<AppSettings> = context.dataStore.data.map { preferences ->
        val uLocale = ULocale(preferences[PreferencesKeys.LOCALE] ?: ULocale.getDefault().toString())
        val calendar = Calendar.getInstance(uLocale)
        AppSettings(
            isFirstLaunch = preferences[PreferencesKeys.IS_FIRST_LAUNCH] ?: true,
            progressSettings = ProgressSettings(
                uLocale = uLocale,
                calculationType = CalculationType.valueOf(
                    preferences[PreferencesKeys.CALCULATION_TYPE] ?: CalculationType.ELAPSED.name
                ),
                weekStartDay = preferences[PreferencesKeys.WEEK_START_DAY] ?: calendar.firstDayOfWeek,
                decimalDigits = preferences[PreferencesKeys.DECIMAL_DIGITS] ?: 13
            ),
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

    private object PreferencesKeys {
        val IS_FIRST_LAUNCH = booleanPreferencesKey("is_first_launch")
        val LOCALE = stringPreferencesKey("locale")
        val CALCULATION_TYPE = stringPreferencesKey("calculation_type")
        val WEEK_START_DAY = intPreferencesKey("week_start_day")
        val DECIMAL_DIGITS = intPreferencesKey("decimal_digits")
    }
}