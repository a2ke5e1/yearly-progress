package com.a3.yearlyprogess.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class SunriseSunsetResponseDto(
    val results: List<ResultDto>,
    val status: String
)
@Serializable
data class ResultDto(
    val date: String,
    val dawn: String,
    val day_length: String,
    val dusk: String,
    val first_light: String,
    val golden_hour: String,
    val last_light: String,
    val solar_noon: String,
    val sunrise: String,
    val sunset: String,
    val timezone: String,
    val utc_offset: Int,
)
