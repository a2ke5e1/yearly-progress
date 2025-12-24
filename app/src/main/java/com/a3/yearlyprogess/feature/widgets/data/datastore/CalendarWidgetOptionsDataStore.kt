package com.a3.yearlyprogess.feature.widgets.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.a3.yearlyprogess.feature.widgets.domain.model.CalendarWidgetOptions
import com.a3.yearlyprogess.feature.widgets.domain.model.WidgetTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.calendarWidgetDataStore: DataStore<Preferences> by preferencesDataStore(name = "calendar_widget_options")

class CalendarWidgetOptionsDataStore(private val context: Context) {

    private fun getThemeKey(appWidgetId: Int) = stringPreferencesKey("calendar_widget_${appWidgetId}_theme")
    private fun getTimeStatusCounterKey(appWidgetId: Int) = booleanPreferencesKey("calendar_widget_${appWidgetId}_time_status_counter")
    private fun getDynamicTimeStatusCounterKey(appWidgetId: Int) = booleanPreferencesKey("calendar_widget_${appWidgetId}_dynamic_time_status_counter")
    private fun getReplaceProgressWithTimeLeftKey(appWidgetId: Int) = booleanPreferencesKey("calendar_widget_${appWidgetId}_replace_progress_with_time_left")
    private fun getDecimalDigitsKey(appWidgetId: Int) = intPreferencesKey("calendar_widget_${appWidgetId}_decimal_digits")
    private fun getBackgroundTransparencyKey(appWidgetId: Int) = intPreferencesKey("calendar_widget_${appWidgetId}_background_transparency")
    private fun getFontScaleKey(appWidgetId: Int) = floatPreferencesKey("calendar_widget_${appWidgetId}_font_scale")
    private fun getSelectedCalendarIdsKey(appWidgetId: Int) = stringPreferencesKey("calendar_widget_${appWidgetId}_selected_calendar_ids")

    fun getOptions(appWidgetId: Int): Flow<CalendarWidgetOptions> {
        return context.calendarWidgetDataStore.data.map { preferences ->
            val themeStr = preferences[getThemeKey(appWidgetId)] ?: WidgetTheme.DEFAULT.name
            val theme = try {
                WidgetTheme.valueOf(themeStr)
            } catch (e: IllegalArgumentException) {
                WidgetTheme.DEFAULT
            }

            val selectedCalendarIdsStr = preferences[getSelectedCalendarIdsKey(appWidgetId)] ?: ""
            val selectedCalendarIds = if (selectedCalendarIdsStr.isNotEmpty()) {
                selectedCalendarIdsStr.split(",").mapNotNull { it.toLongOrNull() }.toSet()
            } else {
                emptySet()
            }

            CalendarWidgetOptions(
                theme = theme,
                timeStatusCounter = preferences[getTimeStatusCounterKey(appWidgetId)] ?: true,
                dynamicTimeStatusCounter = preferences[getDynamicTimeStatusCounterKey(appWidgetId)] ?: false,
                replaceProgressWithTimeLeft = preferences[getReplaceProgressWithTimeLeftKey(appWidgetId)] ?: false,
                decimalDigits = preferences[getDecimalDigitsKey(appWidgetId)] ?: 2,
                backgroundTransparency = preferences[getBackgroundTransparencyKey(appWidgetId)] ?: 100,
                fontScale = preferences[getFontScaleKey(appWidgetId)] ?: 1.0f,
                selectedCalendarIds = selectedCalendarIds
            )
        }
    }

    suspend fun updateOptions(appWidgetId: Int, options: CalendarWidgetOptions) {
        context.calendarWidgetDataStore.edit { preferences ->
            preferences[getThemeKey(appWidgetId)] = options.theme.name
            preferences[getTimeStatusCounterKey(appWidgetId)] = options.timeStatusCounter
            preferences[getDynamicTimeStatusCounterKey(appWidgetId)] = options.dynamicTimeStatusCounter
            preferences[getReplaceProgressWithTimeLeftKey(appWidgetId)] = options.replaceProgressWithTimeLeft
            preferences[getDecimalDigitsKey(appWidgetId)] = options.decimalDigits
            preferences[getBackgroundTransparencyKey(appWidgetId)] = options.backgroundTransparency
            preferences[getFontScaleKey(appWidgetId)] = options.fontScale
            preferences[getSelectedCalendarIdsKey(appWidgetId)] = options.selectedCalendarIds.joinToString(",")
        }
    }

    suspend fun deleteOptions(appWidgetId: Int) {
        context.calendarWidgetDataStore.edit { preferences ->
            preferences.remove(getThemeKey(appWidgetId))
            preferences.remove(getTimeStatusCounterKey(appWidgetId))
            preferences.remove(getDynamicTimeStatusCounterKey(appWidgetId))
            preferences.remove(getReplaceProgressWithTimeLeftKey(appWidgetId))
            preferences.remove(getDecimalDigitsKey(appWidgetId))
            preferences.remove(getBackgroundTransparencyKey(appWidgetId))
            preferences.remove(getFontScaleKey(appWidgetId))
            preferences.remove(getSelectedCalendarIdsKey(appWidgetId))
        }
    }
}