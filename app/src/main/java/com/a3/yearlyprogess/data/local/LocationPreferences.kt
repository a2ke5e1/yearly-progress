package com.a3.yearlyprogess.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.a3.yearlyprogess.domain.model.Location
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "location_preferences")

@Singleton
class LocationPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private val LATITUDE_KEY = doublePreferencesKey("latitude")
        private val LONGITUDE_KEY = doublePreferencesKey("longitude")
        private val IS_MANUAL_KEY = booleanPreferencesKey("is_manual")
        private val HAS_SAVED_LOCATION_KEY = booleanPreferencesKey("has_saved_location")
    }

    val savedLocation: Flow<Location?> = context.dataStore.data.map { preferences ->
        val hasSavedLocation = preferences[HAS_SAVED_LOCATION_KEY] ?: false
        if (hasSavedLocation) {
            Location(
                latitude = preferences[LATITUDE_KEY] ?: 0.0,
                longitude = preferences[LONGITUDE_KEY] ?: 0.0,
                isManual = preferences[IS_MANUAL_KEY] ?: false
            )
        } else {
            null
        }
    }

    suspend fun saveLocation(location: Location) {
        context.dataStore.edit { preferences ->
            preferences[LATITUDE_KEY] = location.latitude
            preferences[LONGITUDE_KEY] = location.longitude
            preferences[IS_MANUAL_KEY] = location.isManual
            preferences[HAS_SAVED_LOCATION_KEY] = true
        }
    }

    suspend fun clearLocation() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}