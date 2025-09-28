package com.a3.yearlyprogess.data.repository

import android.util.Log
import com.a3.yearlyprogess.core.util.Resource
import com.a3.yearlyprogess.data.mapper.toDomain
import com.a3.yearlyprogess.data.remote.SunriseSunsetApi
import com.a3.yearlyprogess.domain.model.SunriseSunset
import com.a3.yearlyprogess.domain.repository.SunriseSunsetRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class SunriseSunsetRepositoryImpl @Inject constructor(
    private val api: SunriseSunsetApi
) : SunriseSunsetRepository {

    override fun getSunriseSunset(
        lat: Double,
        lon: Double,
        startDate: String,
        endDate: String
    ): Flow<Resource<List<SunriseSunset>>> = flow {
        emit(Resource.Loading())
        try {
            val response = api.getSunriseSunset(lat, lon, startDate, endDate)
            if (response.status == "OK") {
                emit(Resource.Success(response.results.map { it.toDomain() }))
            } else {
                emit(Resource.Error("API returned status ${response.status}"))
            }
        } catch (e: Exception) {
            Log.w("SunriseSunsetRepository", "Failed to fetch data", e)
            emit(Resource.Error("Failed to fetch data", e))
        }
    }
}
