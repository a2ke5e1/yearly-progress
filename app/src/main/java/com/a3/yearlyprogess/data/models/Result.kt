package com.a3.yearlyprogess.data.models

import android.icu.text.SimpleDateFormat
import android.icu.util.TimeZone
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import java.util.Date
import java.util.Locale

data class Result(
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
) {
  fun getFirstLight(): Date {
    return convertToDateTime(first_light)
  }

  fun getSunrise(): Date {
    return convertToDateTime(sunrise)
  }

  fun getSunset(): Date {
    return convertToDateTime(sunset)
  }

  fun getGoldenHour(): Date {
    return convertToDateTime(golden_hour)
  }

  fun getDawn(): Date {
    return convertToDateTime(dawn)
  }

  fun getDusk(): Date {
    return convertToDateTime(dusk)
  }

  fun getSolarNoon(): Date {
    return convertToDateTime(solar_noon)
  }

  fun getDayLength(): Date {
    return convertToDateTime(day_length)
  }

  fun getLastLight(): Date {
    return convertToDateTime(last_light)
  }

  private fun convertToDateTime(time: String): Date {
    val debug = Firebase.crashlytics
    debug.log("convertToDateTime: $date $time")

    val dateTime = "$date $time"
    val formatter = SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.getDefault())
    val timeZone = TimeZone.getTimeZone(timezone) // Adjust as needed
    formatter.timeZone = timeZone

    val newDate = formatter.parse(dateTime)
    return newDate
  }
}
