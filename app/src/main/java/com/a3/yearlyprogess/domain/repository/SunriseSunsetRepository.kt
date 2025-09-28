package com.a3.yearlyprogess.domain.repository

import com.a3.yearlyprogess.core.util.Resource
import com.a3.yearlyprogess.domain.model.SunriseSunset
import kotlinx.coroutines.flow.Flow

interface SunriseSunsetRepository {
    fun getSunriseSunset(
        lat: Double,
        lon: Double
    ): Flow<Resource<List<SunriseSunset>>>
}
