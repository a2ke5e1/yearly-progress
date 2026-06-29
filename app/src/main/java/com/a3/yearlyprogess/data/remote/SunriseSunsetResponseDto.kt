package com.a3.yearlyprogess.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class SunriseSunsetResponseDto(
    val results: List<ResultDto> = emptyList(),
    val status: String = ""
)
@Serializable
data class ResultDto(
    val date: String = "",
    val dawn: String? = null,
    val day_length: String = "",
    val dusk: String? = null,
    val first_light: String? = null,
    val golden_hour: String? = null,
    val last_light: String? = null,
    val solar_noon: String = "",
    val sunrise: String = "",
    val sunset: String = "",
    val timezone: String = "UTC",
    val utc_offset: Int = 0,
)
