package com.a3.yearlyprogess.feature.widgets.data.datastore

import android.content.Context
import android.os.Build
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.a3.yearlyprogess.core.util.TimePeriod
import com.a3.yearlyprogess.feature.widgets.domain.model.StandaloneWidgetOptions
import com.a3.yearlyprogess.feature.widgets.domain.model.StandaloneWidgetOptions.Companion.WidgetShape
import com.a3.yearlyprogess.feature.widgets.domain.model.WidgetTheme
import com.a3.yearlyprogess.feature.widgets.ui.StandaloneWidgetType
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Singleton

private const val PREFERENCES_NAME = "standalone_widget_options_preferences"

val Context.standaloneWidgetOptionsDataStore by preferencesDataStore(name = PREFERENCES_NAME)

@Singleton
class StandaloneWidgetOptionsDataStore(
    @param:ApplicationContext private val context: Context
) {

    private val defaultStandaloneWidgetOptions = StandaloneWidgetOptions(
        theme = if (Build.VERSION.SDK_INT > Build.VERSION_CODES.S) WidgetTheme.DYNAMIC else WidgetTheme.DEFAULT,
        widgetType = null,
        widgetShape = WidgetShape.RECTANGULAR,
        timeLeftCounter = true,
        dynamicLeftCounter = false,
        replaceProgressWithDaysLeft = false,
        decimalPlaces = 2,
        backgroundTransparency = 100,
        fontScale = 1.0f
    )

    // Create preference keys for specific widget ID
    private fun getThemeKey(widgetId: Int) = stringPreferencesKey("theme_$widgetId")
    private fun getWidgetTypeKey(widgetId: Int) = stringPreferencesKey("widget_type_$widgetId")
    private fun getWidgetShapeKey(widgetId: Int) = stringPreferencesKey("widget_shape_$widgetId")
    private fun getTimeLeftCounterKey(widgetId: Int) = booleanPreferencesKey("time_left_counter_$widgetId")
    private fun getDynamicLeftCounterKey(widgetId: Int) = booleanPreferencesKey("dynamic_left_counter_$widgetId")
    private fun getReplaceProgressWithDaysLeftKey(widgetId: Int) = booleanPreferencesKey("replace_progress_with_days_left_$widgetId")
    private fun getDecimalPlacesKey(widgetId: Int) = intPreferencesKey("decimal_places_$widgetId")
    private fun getBackgroundTransparencyKey(widgetId: Int) = intPreferencesKey("background_transparency_$widgetId")
    private fun getFontScaleKey(widgetId: Int) = floatPreferencesKey("font_scale_$widgetId")

    fun getOptionsFlow(widgetId: Int): Flow<StandaloneWidgetOptions> =
        context.standaloneWidgetOptionsDataStore.data.map { prefs ->
            StandaloneWidgetOptions(
                theme = prefs[getThemeKey(widgetId)]?.let { themeName ->
                    runCatching { WidgetTheme.valueOf(themeName) }.getOrDefault(defaultStandaloneWidgetOptions.theme)
                } ?: defaultStandaloneWidgetOptions.theme,
                widgetType = prefs[getWidgetTypeKey(widgetId)]?.let { typeName ->
                    runCatching { StandaloneWidgetType.valueOf(typeName) }.getOrNull()
                },
                widgetShape = prefs[getWidgetShapeKey(widgetId)]?.let { shapeName ->
                    runCatching { WidgetShape.valueOf(shapeName) }.getOrDefault(defaultStandaloneWidgetOptions.widgetShape)
                } ?: defaultStandaloneWidgetOptions.widgetShape,
                timeLeftCounter = prefs[getTimeLeftCounterKey(widgetId)] ?: defaultStandaloneWidgetOptions.timeLeftCounter,
                dynamicLeftCounter = prefs[getDynamicLeftCounterKey(widgetId)] ?: defaultStandaloneWidgetOptions.dynamicLeftCounter,
                replaceProgressWithDaysLeft = prefs[getReplaceProgressWithDaysLeftKey(widgetId)] ?: defaultStandaloneWidgetOptions.replaceProgressWithDaysLeft,
                decimalPlaces = prefs[getDecimalPlacesKey(widgetId)]?.coerceIn(0, 5) ?: defaultStandaloneWidgetOptions.decimalPlaces,
                backgroundTransparency = prefs[getBackgroundTransparencyKey(widgetId)]?.coerceIn(0, 100) ?: defaultStandaloneWidgetOptions.backgroundTransparency,
                fontScale = prefs[getFontScaleKey(widgetId)]?.coerceIn(0.5f, 2.0f) ?: defaultStandaloneWidgetOptions.fontScale
            )
        }

    suspend fun updateOptions(widgetId: Int, options: StandaloneWidgetOptions) {
        context.standaloneWidgetOptionsDataStore.edit { prefs ->
            prefs[getThemeKey(widgetId)] = options.theme.name
            options.widgetType?.let { prefs[getWidgetTypeKey(widgetId)] = it.name }
            prefs[getWidgetShapeKey(widgetId)] = options.widgetShape.name
            prefs[getTimeLeftCounterKey(widgetId)] = options.timeLeftCounter
            prefs[getDynamicLeftCounterKey(widgetId)] = options.dynamicLeftCounter
            prefs[getReplaceProgressWithDaysLeftKey(widgetId)] = options.replaceProgressWithDaysLeft
            prefs[getDecimalPlacesKey(widgetId)] = options.decimalPlaces
            prefs[getBackgroundTransparencyKey(widgetId)] = options.backgroundTransparency
            prefs[getFontScaleKey(widgetId)] = options.fontScale
        }
    }

    suspend fun updateTheme(widgetId: Int, theme: WidgetTheme) {
        context.standaloneWidgetOptionsDataStore.edit { prefs ->
            prefs[getThemeKey(widgetId)] = theme.name
        }
    }

    suspend fun updateWidgetType(widgetId: Int, widgetType: StandaloneWidgetType?) {
        context.standaloneWidgetOptionsDataStore.edit { prefs ->
            if (widgetType != null) {
                prefs[getWidgetTypeKey(widgetId)] = widgetType.name
            } else {
                prefs.remove(getWidgetTypeKey(widgetId))
            }
        }
    }

    suspend fun updateWidgetShape(widgetId: Int, shape: WidgetShape) {
        context.standaloneWidgetOptionsDataStore.edit { prefs ->
            prefs[getWidgetShapeKey(widgetId)] = shape.name
        }
    }

    suspend fun updateTimeLeftCounter(widgetId: Int, enabled: Boolean) {
        context.standaloneWidgetOptionsDataStore.edit { prefs ->
            prefs[getTimeLeftCounterKey(widgetId)] = enabled
        }
    }

    suspend fun updateDynamicLeftCounter(widgetId: Int, enabled: Boolean) {
        context.standaloneWidgetOptionsDataStore.edit { prefs ->
            prefs[getDynamicLeftCounterKey(widgetId)] = enabled
        }
    }

    suspend fun updateReplaceProgressWithDaysLeft(widgetId: Int, enabled: Boolean) {
        context.standaloneWidgetOptionsDataStore.edit { prefs ->
            prefs[getReplaceProgressWithDaysLeftKey(widgetId)] = enabled
        }
    }

    suspend fun updateDecimalPlaces(widgetId: Int, places: Int) {
        context.standaloneWidgetOptionsDataStore.edit { prefs ->
            prefs[getDecimalPlacesKey(widgetId)] = places.coerceIn(0, 5)
        }
    }

    suspend fun updateBackgroundTransparency(widgetId: Int, transparency: Int) {
        context.standaloneWidgetOptionsDataStore.edit { prefs ->
            prefs[getBackgroundTransparencyKey(widgetId)] = transparency.coerceIn(0, 100)
        }
    }

    suspend fun updateFontScale(widgetId: Int, scale: Float) {
        context.standaloneWidgetOptionsDataStore.edit { prefs ->
            prefs[getFontScaleKey(widgetId)] = scale.coerceIn(0.5f, 2.0f)
        }
    }

    suspend fun deleteWidgetOptions(widgetId: Int) {
        context.standaloneWidgetOptionsDataStore.edit { prefs ->
            prefs.remove(getThemeKey(widgetId))
            prefs.remove(getWidgetTypeKey(widgetId))
            prefs.remove(getWidgetShapeKey(widgetId))
            prefs.remove(getTimeLeftCounterKey(widgetId))
            prefs.remove(getDynamicLeftCounterKey(widgetId))
            prefs.remove(getReplaceProgressWithDaysLeftKey(widgetId))
            prefs.remove(getDecimalPlacesKey(widgetId))
            prefs.remove(getBackgroundTransparencyKey(widgetId))
            prefs.remove(getFontScaleKey(widgetId))
        }
    }
}