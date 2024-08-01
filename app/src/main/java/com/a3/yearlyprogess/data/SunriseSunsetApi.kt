package com.a3.yearlyprogess.data

import com.a3.yearlyprogess.data.models.SunriseSunsetResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface SunriseSunsetApi {
    @GET("/json")
    suspend fun getSunriseSunset(
        @Query("lat") lat: Double,
        @Query("lng") lon: Double,
        @Query("date_start") startDate: String,
        @Query("date_end") endDate: String,
        @Query("time_format") timeFormat: String = "24"
    ): Response<SunriseSunsetResponse>
}