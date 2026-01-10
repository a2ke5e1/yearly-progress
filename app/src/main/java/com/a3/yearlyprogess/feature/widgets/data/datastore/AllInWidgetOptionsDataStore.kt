package com.a3.yearlyprogess.feature.widgets.data.datastore

import android.content.Context
import android.os.Build
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.a3.yearlyprogess.feature.widgets.domain.model.AllInWidgetOptions
import com.a3.yearlyprogess.feature.widgets.domain.model.WidgetTheme
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Singleton

private const val PREFERENCES_NAME = "all_in_widget_options_preferences"

val Context.allInWidgetOptionsDataStore by preferencesDataStore(name = PREFERENCES_NAME)

@Singleton
class AllInWidgetOptionsDataStore(
    @param:ApplicationContext private val context: Context
) {

    private val defaultAllInWidgetOptions = AllInWidgetOptions(
        theme = if (Build.VERSION.SDK_INT > Build.VERSION_CODES.S) WidgetTheme.DYNAMIC else WidgetTheme.DEFAULT,
        showDay = true,
        showWeek = true,
        showMonth = true,
        showYear = true,
        timeLeftCounter = true,
        dynamicLeftCounter = false,
        replaceProgressWithDaysLeft = false,
        decimalPlaces = 2,
        backgroundTransparency = 100,
        fontScale = 1.0f
    )

    // Create preference keys for specific widget ID
    private fun getThemeKey(widgetId: Int) = stringPreferencesKey("theme_$widgetId")
    private fun getShowDayKey(widgetId: Int) = booleanPreferencesKey("show_day_$widgetId")
    private fun getShowWeekKey(widgetId: Int) = booleanPreferencesKey("show_week_$widgetId")
    private fun getShowMonthKey(widgetId: Int) = booleanPreferencesKey("show_month_$widgetId")
    private fun getShowYearKey(widgetId: Int) = booleanPreferencesKey("show_year_$widgetId")
    private fun getTimeLeftCounterKey(widgetId: Int) = booleanPreferencesKey("time_left_counter_$widgetId")
    private fun getDynamicLeftCounterKey(widgetId: Int) = booleanPreferencesKey("dynamic_left_counter_$widgetId")
    private fun getReplaceProgressWithDaysLeftKey(widgetId: Int) = booleanPreferencesKey("replace_progress_with_days_left_$widgetId")
    private fun getDecimalPlacesKey(widgetId: Int) = intPreferencesKey("decimal_places_$widgetId")
    private fun getBackgroundTransparencyKey(widgetId: Int) = intPreferencesKey("background_transparency_$widgetId")
    private fun getFontScaleKey(widgetId: Int) = floatPreferencesKey("font_scale_$widgetId")

    fun getOptionsFlow(widgetId: Int): Flow<AllInWidgetOptions> =
        context.allInWidgetOptionsDataStore.data.map { prefs ->
            AllInWidgetOptions(
                theme = prefs[getThemeKey(widgetId)]?.let { themeName ->
                    runCatching { WidgetTheme.valueOf(themeName) }.getOrDefault(defaultAllInWidgetOptions.theme)
                } ?: defaultAllInWidgetOptions.theme,
                showDay = prefs[getShowDayKey(widgetId)] ?: defaultAllInWidgetOptions.showDay,
                showWeek = prefs[getShowWeekKey(widgetId)] ?: defaultAllInWidgetOptions.showWeek,
                showMonth = prefs[getShowMonthKey(widgetId)] ?: defaultAllInWidgetOptions.showMonth,
                showYear = prefs[getShowYearKey(widgetId)] ?: defaultAllInWidgetOptions.showYear,
                timeLeftCounter = prefs[getTimeLeftCounterKey(widgetId)] ?: defaultAllInWidgetOptions.timeLeftCounter,
                dynamicLeftCounter = prefs[getDynamicLeftCounterKey(widgetId)] ?: defaultAllInWidgetOptions.dynamicLeftCounter,
                replaceProgressWithDaysLeft = prefs[getReplaceProgressWithDaysLeftKey(widgetId)] ?: defaultAllInWidgetOptions.replaceProgressWithDaysLeft,
                decimalPlaces = prefs[getDecimalPlacesKey(widgetId)]?.coerceIn(0, 5) ?: defaultAllInWidgetOptions.decimalPlaces,
                backgroundTransparency = prefs[getBackgroundTransparencyKey(widgetId)]?.coerceIn(0, 100) ?: defaultAllInWidgetOptions.backgroundTransparency,
                fontScale = prefs[getFontScaleKey(widgetId)]?.coerceIn(0.5f, 2.0f) ?: defaultAllInWidgetOptions.fontScale
            )
        }

    suspend fun updateOptions(widgetId: Int, options: AllInWidgetOptions) {
        context.allInWidgetOptionsDataStore.edit { prefs ->
            prefs[getThemeKey(widgetId)] = options.theme.name
            prefs[getShowDayKey(widgetId)] = options.showDay
            prefs[getShowWeekKey(widgetId)] = options.showWeek
            prefs[getShowMonthKey(widgetId)] = options.showMonth
            prefs[getShowYearKey(widgetId)] = options.showYear
            prefs[getTimeLeftCounterKey(widgetId)] = options.timeLeftCounter
            prefs[getDynamicLeftCounterKey(widgetId)] = options.dynamicLeftCounter
            prefs[getReplaceProgressWithDaysLeftKey(widgetId)] = options.replaceProgressWithDaysLeft
            prefs[getDecimalPlacesKey(widgetId)] = options.decimalPlaces
            prefs[getBackgroundTransparencyKey(widgetId)] = options.backgroundTransparency
            prefs[getFontScaleKey(widgetId)] = options.fontScale
        }
    }

    suspend fun updateTheme(widgetId: Int, theme: WidgetTheme) {
        context.allInWidgetOptionsDataStore.edit { prefs ->
            prefs[getThemeKey(widgetId)] = theme.name
        }
    }

    suspend fun updateShowDay(widgetId: Int, show: Boolean) {
        context.allInWidgetOptionsDataStore.edit { prefs ->
            prefs[getShowDayKey(widgetId)] = show
        }
    }

    suspend fun updateShowWeek(widgetId: Int, show: Boolean) {
        context.allInWidgetOptionsDataStore.edit { prefs ->
            prefs[getShowWeekKey(widgetId)] = show
        }
    }

    suspend fun updateShowMonth(widgetId: Int, show: Boolean) {
        context.allInWidgetOptionsDataStore.edit { prefs ->
            prefs[getShowMonthKey(widgetId)] = show
        }
    }

    suspend fun updateShowYear(widgetId: Int, show: Boolean) {
        context.allInWidgetOptionsDataStore.edit { prefs ->
            prefs[getShowYearKey(widgetId)] = show
        }
    }

    suspend fun updateTimeLeftCounter(widgetId: Int, enabled: Boolean) {
        context.allInWidgetOptionsDataStore.edit { prefs ->
            prefs[getTimeLeftCounterKey(widgetId)] = enabled
        }
    }

    suspend fun updateDynamicLeftCounter(widgetId: Int, enabled: Boolean) {
        context.allInWidgetOptionsDataStore.edit { prefs ->
            prefs[getDynamicLeftCounterKey(widgetId)] = enabled
        }
    }

    suspend fun updateReplaceProgressWithDaysLeft(widgetId: Int, enabled: Boolean) {
        context.allInWidgetOptionsDataStore.edit { prefs ->
            prefs[getReplaceProgressWithDaysLeftKey(widgetId)] = enabled
        }
    }

    suspend fun updateDecimalPlaces(widgetId: Int, places: Int) {
        context.allInWidgetOptionsDataStore.edit { prefs ->
            prefs[getDecimalPlacesKey(widgetId)] = places.coerceIn(0, 5)
        }
    }

    suspend fun updateBackgroundTransparency(widgetId: Int, transparency: Int) {
        context.allInWidgetOptionsDataStore.edit { prefs ->
            prefs[getBackgroundTransparencyKey(widgetId)] = transparency.coerceIn(0, 100)
        }
    }

    suspend fun updateFontScale(widgetId: Int, scale: Float) {
        context.allInWidgetOptionsDataStore.edit { prefs ->
            prefs[getFontScaleKey(widgetId)] = scale.coerceIn(0.5f, 2.0f)
        }
    }

    suspend fun deleteWidgetOptions(widgetId: Int) {
        context.allInWidgetOptionsDataStore.edit { prefs ->
            prefs.remove(getThemeKey(widgetId))
            prefs.remove(getShowDayKey(widgetId))
            prefs.remove(getShowWeekKey(widgetId))
            prefs.remove(getShowMonthKey(widgetId))
            prefs.remove(getShowYearKey(widgetId))
            prefs.remove(getTimeLeftCounterKey(widgetId))
            prefs.remove(getDynamicLeftCounterKey(widgetId))
            prefs.remove(getReplaceProgressWithDaysLeftKey(widgetId))
            prefs.remove(getDecimalPlacesKey(widgetId))
            prefs.remove(getBackgroundTransparencyKey(widgetId))
            prefs.remove(getFontScaleKey(widgetId))
        }
    }
}