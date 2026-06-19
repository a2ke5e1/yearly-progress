package com.a3.yearlyprogess.feature.widgets.data.datastore

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.a3.yearlyprogess.core.util.Log
import com.a3.yearlyprogess.feature.widgets.domain.model.WidgetTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private const val PREFERENCES_NAME = "widget_theme_preferences"

val Context.widgetThemeDataStore by preferencesDataStore(name = PREFERENCES_NAME)

object WidgetThemePreferences {
    val KEY_THEME = stringPreferencesKey("selected_theme")
    val KEY_USE_DYNAMIC = booleanPreferencesKey("use_dynamic_colors")
}

class WidgetThemeDataStore(private val context: Context) {

    val themeFlow: Flow<WidgetTheme> = context.widgetThemeDataStore.data
        .catch { exception ->
            if (exception is IOException) {
                Log.e("WidgetThemeDataStore", "Error reading theme", exception)
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { prefs ->
            val name = prefs[WidgetThemePreferences.KEY_THEME] ?: WidgetTheme.DEFAULT.name
            runCatching { WidgetTheme.valueOf(name) }.getOrDefault(WidgetTheme.DEFAULT)
        }

    val useDynamicColorsFlow: Flow<Boolean> = context.widgetThemeDataStore.data
        .catch { exception ->
            if (exception is IOException) {
                Log.e("WidgetThemeDataStore", "Error reading dynamic colors", exception)
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { prefs ->
            prefs[WidgetThemePreferences.KEY_USE_DYNAMIC] ?: true
        }

    suspend fun saveTheme(theme: WidgetTheme) {
        context.widgetThemeDataStore.edit { prefs ->
            prefs[WidgetThemePreferences.KEY_THEME] = theme.name
        }
    }

    suspend fun setUseDynamicColors(enabled: Boolean) {
        context.widgetThemeDataStore.edit { prefs ->
            prefs[WidgetThemePreferences.KEY_USE_DYNAMIC] = enabled
        }
    }
}
