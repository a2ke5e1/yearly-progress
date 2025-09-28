package com.a3.yearlyprogess.core.util

import android.util.Log
import androidx.datastore.preferences.preferencesDataStore
import com.a3.yearlyprogess.data.remote.ResultDto
import java.time.LocalDate

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json

val Context.sunriseSunsetDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "sunrise_sunset_cache"
)
object SunriseSunsetCache {

    private val LAST_UPDATE_KEY = stringPreferencesKey("last_update_date")
    private fun cacheKey(lat: Double, lon: Double) = stringPreferencesKey("sunrise_sunset_${lat}_$lon")

    /**
     * Read cache for a location. Returns null if cache missing or date changed.
     */
    suspend fun get(context: Context, lat: Double, lon: Double, ): List<ResultDto>? {
        val prefs = context.sunriseSunsetDataStore.data.first()
        val lastUpdate = prefs[LAST_UPDATE_KEY]?.let { LocalDate.parse(it) }

        // Invalidate cache if last update is before today
        if (lastUpdate == null || lastUpdate.isBefore(LocalDate.now())) {
            Log.d("SunriseSunsetCache", "Cache expired or missing for $lat,$lon")
            clear(context)
            return null
        }

        val json = prefs[cacheKey(lat, lon)]
        return if (json != null) {
            Log.d("SunriseSunsetCache", "Cache hit for $lat,$lon")
            try {
                Json.decodeFromString<List<ResultDto>>(json)
            } catch (e: Exception) {
                Log.w("SunriseSunsetCache", "Failed to decode cached data", e)
                null
            }
        } else {
            Log.d("SunriseSunsetCache", "No cache found for $lat,$lon")
            null
        }
    }

    /**
     * Save cache for a location.
     */
    suspend fun set(context: Context, lat: Double, lon: Double, data: List<ResultDto>, ) {
        try {
            val json = Json.encodeToString(data)
            context.sunriseSunsetDataStore.edit { prefs ->
                prefs[cacheKey(lat, lon)] = json
                prefs[LAST_UPDATE_KEY] = LocalDate.now().toString()
            }
            Log.d("SunriseSunsetCache", "Cache saved for $lat,$lon with ${data.size} items")
        } catch (e: Exception) {
            Log.w("SunriseSunsetCache", "Failed to save cache", e)
        }
    }

    /**
     * Clear all cache
     */
    suspend fun clear(context: Context) {
        try {
            context.sunriseSunsetDataStore.edit { prefs ->
                prefs.clear()
            }
            Log.d("SunriseSunsetCache", "All cache cleared")
        } catch (e: Exception) {
            Log.w("SunriseSunsetCache", "Failed to clear cache", e)
        }
    }
}

