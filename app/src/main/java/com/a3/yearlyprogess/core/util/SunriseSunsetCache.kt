package com.a3.yearlyprogess.core.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.a3.yearlyprogess.data.remote.ResultDto
import com.a3.yearlyprogess.domain.model.SunriseSunset
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json

val Context.sunriseSunsetDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "sunrise_sunset_cache"
)

fun getStartAndEndTime(dayLight: Boolean, results: List<SunriseSunset>): Pair<Long, Long> {
    return if (dayLight) {
        results[1].sunrise.time to results[1].sunset.time
    } else {
        if (System.currentTimeMillis() < results[1].sunset.time) {
            results[0].sunset.time to results[1].sunrise.time
        } else {
            results[1].sunset.time to results[2].sunrise.time
        }
    }
}


object SunriseSunsetCache {

    private fun cacheKey(lat: Double, lon: Double, startDate: String, endDate: String) =
        stringPreferencesKey("sunrise_sunset_${lat}_${lon}_${startDate}_${endDate}")

    /**
     * Read cache for a location+date range.
     */
    suspend fun get(
        context: Context,
        lat: Double,
        lon: Double,
        startDate: String,
        endDate: String
    ): List<ResultDto>? {
        val prefs = context.sunriseSunsetDataStore.data.first()
        val json = prefs[cacheKey(lat, lon, startDate, endDate)]
        return if (json != null) {
            Log.d("SunriseSunsetCache", "Cache hit for $lat,$lon [$startDate → $endDate]")
            try {
                Json.decodeFromString<List<ResultDto>>(json)
            } catch (e: Exception) {
                Log.w("SunriseSunsetCache", "Failed to decode cached data", e)
                null
            }
        } else {
            Log.d("SunriseSunsetCache", "No cache found for $lat,$lon [$startDate → $endDate]")
            null
        }
    }

    /**
     * Save cache for a location+date range.
     * Clears old data before saving.
     */
    suspend fun set(
        context: Context,
        lat: Double,
        lon: Double,
        startDate: String,
        endDate: String,
        data: List<ResultDto>
    ) {
        try {
            val json = Json.encodeToString(data)
            context.sunriseSunsetDataStore.edit { prefs ->
                prefs.clear() // clear old cache completely
                prefs[cacheKey(lat, lon, startDate, endDate)] = json
            }
            Log.d(
                "SunriseSunsetCache",
                "Cache saved for $lat,$lon [$startDate → $endDate] with ${data.size} items"
            )
        } catch (e: Exception) {
            Log.w("SunriseSunsetCache", "Failed to save cache", e)
        }
    }

    /**
     * Clear all cache.
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
