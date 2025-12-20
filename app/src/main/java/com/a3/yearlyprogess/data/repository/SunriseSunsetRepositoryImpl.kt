package com.a3.yearlyprogess.data.repository

import android.content.Context
import com.a3.yearlyprogess.core.util.Log
import com.a3.yearlyprogess.core.util.Resource
import com.a3.yearlyprogess.data.local.SunriseSunsetCache
import com.a3.yearlyprogess.data.mapper.toDomain
import com.a3.yearlyprogess.data.remote.SunriseSunsetApi
import com.a3.yearlyprogess.domain.model.SunriseSunset
import com.a3.yearlyprogess.domain.repository.SunriseSunsetRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

class SunriseSunsetRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val api: SunriseSunsetApi
) : SunriseSunsetRepository {

    override fun getSunriseSunset(
        lat: Double,
        lon: Double
    ): Flow<Resource<List<SunriseSunset>>> = flow {
        emit(Resource.Loading())

        // yyyy-MM-dd formatter
        val formatter = DateTimeFormatter.ISO_LOCAL_DATE
        val today = LocalDate.now()
        val startDate = today.minusDays(1).format(formatter) // yesterday
        val endDate = today.plusDays(1).format(formatter)    // tomorrow

        val cached = SunriseSunsetCache.get(context, lat, lon, startDate, endDate)
        if (cached != null && cached.size == 3) {
            Log.d("SunriseSunsetRepository", "Returning cached data for $lat,$lon")
            emit(Resource.Success(cached.map { it.toDomain() }))
            return@flow
        }

        try {
            Log.d("SunriseSunsetRepository", "Fetching data for $lat,$lon between $startDate and $endDate")
            val response = api.getSunriseSunset(lat, lon, startDate, endDate)
            if (response.status == "OK") {
                val results = response.results
                SunriseSunsetCache.set(context, lat, lon, startDate, endDate, results)
                emit(Resource.Success(results.map { it.toDomain() }))
            } else {
                emit(Resource.Error("API returned status ${response.status}"))
            }
        } catch (e: Exception) {
            Log.w("SunriseSunsetRepository", "Failed to fetch data", e)
            if (e is CancellationException) throw e
            emit(Resource.Error("Failed to fetch data", e))
        }
    }
}
