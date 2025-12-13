package com.a3.yearlyprogess.feature.widgets.data.datastore

import android.content.Context
import android.os.Build
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.a3.yearlyprogess.feature.widgets.domain.model.EventWidgetOptions
import com.a3.yearlyprogess.feature.widgets.domain.model.WidgetTheme
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Singleton

private const val PREFERENCES_NAME = "event_widget_options_preferences"

val Context.eventWidgetOptionsDataStore by preferencesDataStore(name = PREFERENCES_NAME)

@Singleton
class EventWidgetOptionsDataStore(
    @param:ApplicationContext private val context: Context
) {

    private val defaultEventWidgetOptions = EventWidgetOptions(
        theme = if (Build.VERSION.SDK_INT > Build.VERSION_CODES.S) WidgetTheme.DYNAMIC else WidgetTheme.DEFAULT,
        timeStatusCounter = true,
        dynamicTimeStatusCounter = false,
        decimalDigits = 2,
        backgroundTransparency = 100,
        fontScale = 1.0f,
        showEventImage = false
    )

    // Create preference keys for specific widget ID
    private fun getThemeKey(widgetId: Int) = stringPreferencesKey("theme_$widgetId")
    private fun getTimeStatusCounterKey(widgetId: Int) = booleanPreferencesKey("time_status_counter_$widgetId")
    private fun getDynamicTimeStatusCounterKey(widgetId: Int) = booleanPreferencesKey("dynamic_time_status_counter_$widgetId")
    private fun getDecimalDigitsKey(widgetId: Int) = intPreferencesKey("decimal_digits_$widgetId")
    private fun getBackgroundTransparencyKey(widgetId: Int) = intPreferencesKey("background_transparency_$widgetId")
    private fun getFontScaleKey(widgetId: Int) = floatPreferencesKey("font_scale_$widgetId")
    private fun getShowEventImageKey(widgetId: Int) = booleanPreferencesKey("show_event_image_$widgetId")

    private fun getSelectedEventIdsKey(widgetId: Int) = stringPreferencesKey("selected_event_ids_$widgetId")


    fun getOptionsFlow(widgetId: Int): Flow<EventWidgetOptions> =
        context.eventWidgetOptionsDataStore.data.map { prefs ->
            EventWidgetOptions(
                theme = prefs[getThemeKey(widgetId)]?.let { themeName ->
                    runCatching { WidgetTheme.valueOf(themeName) }.getOrDefault(defaultEventWidgetOptions.theme)
                } ?: defaultEventWidgetOptions.theme,
                timeStatusCounter = prefs[getTimeStatusCounterKey(widgetId)] ?: defaultEventWidgetOptions.timeStatusCounter,
                dynamicTimeStatusCounter = prefs[getDynamicTimeStatusCounterKey(widgetId)] ?: defaultEventWidgetOptions.dynamicTimeStatusCounter,
                decimalDigits = prefs[getDecimalDigitsKey(widgetId)]?.coerceIn(0, 5) ?: defaultEventWidgetOptions.decimalDigits,
                backgroundTransparency = prefs[getBackgroundTransparencyKey(widgetId)]?.coerceIn(0, 100) ?: defaultEventWidgetOptions.backgroundTransparency,
                fontScale = prefs[getFontScaleKey(widgetId)]?.coerceIn(0.1f, 2.0f) ?: defaultEventWidgetOptions.fontScale,
                showEventImage = prefs[getShowEventImageKey(widgetId)] ?: defaultEventWidgetOptions.showEventImage,
                selectedEventIds = prefs[getSelectedEventIdsKey(widgetId)]?.split(",")?.mapNotNull { it.toIntOrNull() }?.toSet() ?: emptySet()
            )
        }

    suspend fun updateOptions(widgetId: Int, options: EventWidgetOptions) {
        context.eventWidgetOptionsDataStore.edit { prefs ->
            prefs[getThemeKey(widgetId)] = options.theme.name
            prefs[getTimeStatusCounterKey(widgetId)] = options.timeStatusCounter
            prefs[getDynamicTimeStatusCounterKey(widgetId)] = options.dynamicTimeStatusCounter
            prefs[getDecimalDigitsKey(widgetId)] = options.decimalDigits
            prefs[getBackgroundTransparencyKey(widgetId)] = options.backgroundTransparency
            prefs[getFontScaleKey(widgetId)] = options.fontScale
            prefs[getShowEventImageKey(widgetId)] = options.showEventImage
            prefs[getSelectedEventIdsKey(widgetId)] = options.selectedEventIds.joinToString(",")
        }
    }

    suspend fun updateTheme(widgetId: Int, theme: WidgetTheme) {
        context.eventWidgetOptionsDataStore.edit { prefs ->
            prefs[getThemeKey(widgetId)] = theme.name
        }
    }

    suspend fun updateTimeStatusCounter(widgetId: Int, enabled: Boolean) {
        context.eventWidgetOptionsDataStore.edit { prefs ->
            prefs[getTimeStatusCounterKey(widgetId)] = enabled
        }
    }

    suspend fun updateDynamicTimeStatusCounter(widgetId: Int, enabled: Boolean) {
        context.eventWidgetOptionsDataStore.edit { prefs ->
            prefs[getDynamicTimeStatusCounterKey(widgetId)] = enabled
        }
    }

    suspend fun updateDecimalDigits(widgetId: Int, digits: Int) {
        context.eventWidgetOptionsDataStore.edit { prefs ->
            prefs[getDecimalDigitsKey(widgetId)] = digits.coerceIn(0, 5)
        }
    }

    suspend fun updateBackgroundTransparency(widgetId: Int, transparency: Int) {
        context.eventWidgetOptionsDataStore.edit { prefs ->
            prefs[getBackgroundTransparencyKey(widgetId)] = transparency.coerceIn(0, 100)
        }
    }

    suspend fun updateFontScale(widgetId: Int, scale: Float) {
        context.eventWidgetOptionsDataStore.edit { prefs ->
            prefs[getFontScaleKey(widgetId)] = scale.coerceIn(0.1f, 2.0f)
        }
    }

    suspend fun updateShowEventImage(widgetId: Int, enabled: Boolean) {
        context.eventWidgetOptionsDataStore.edit { prefs ->
            prefs[getShowEventImageKey(widgetId)] = enabled
        }
    }


    suspend fun updateSelectedEventIds(ids : Set<Int>, widgetId: Int) {
        context.eventWidgetOptionsDataStore.edit { prefs ->
            prefs[getSelectedEventIdsKey(widgetId)] = ids.joinToString(",")
        }
    }

    suspend fun deleteWidgetOptions(widgetId: Int) {
        context.eventWidgetOptionsDataStore.edit { prefs ->
            prefs.remove(getThemeKey(widgetId))
            prefs.remove(getTimeStatusCounterKey(widgetId))
            prefs.remove(getDynamicTimeStatusCounterKey(widgetId))
            prefs.remove(getDecimalDigitsKey(widgetId))
            prefs.remove(getBackgroundTransparencyKey(widgetId))
            prefs.remove(getFontScaleKey(widgetId))
            prefs.remove(getShowEventImageKey(widgetId))
            prefs.remove(getSelectedEventIdsKey(widgetId))
        }
    }
}