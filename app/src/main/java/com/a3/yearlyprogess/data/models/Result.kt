package com.a3.yearlyprogess.data.models

import android.icu.text.SimpleDateFormat
import android.icu.util.TimeZone
import java.util.Calendar
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
    val utc_offset: Int
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
        try {
            val dateTime = "$date $time"
            val formatter = SimpleDateFormat(
                "yyyy-MM-dd hh:mm:ss", Locale.getDefault()
            )
            val timeZone = TimeZone.getTimeZone(timezone) // Adjust as needed
            formatter.timeZone = timeZone

            val newDate = formatter.parse(dateTime)
            return newDate
        } catch (ex: Exception) {
            ex.printStackTrace()
        }

        val cal = Calendar.getInstance()
        cal.timeInMillis = System.currentTimeMillis()

        val year = date.split("-")[0].toInt()
        val month = date.split("-")[1].toInt()
        val day = date.split("-")[2].toInt()

        cal.set(Calendar.YEAR, year)
        cal.set(Calendar.MONTH, month - 1)
        cal.set(Calendar.DAY_OF_MONTH, day)


        val onlyTime = time.split(" ")[0]
        val hour = onlyTime.split(":")[0].toInt()
        val minute = onlyTime.split(":")[1].toInt()
        val second = onlyTime.split(":")[2].toInt()
        cal.set(Calendar.HOUR_OF_DAY, hour)
        cal.set(Calendar.MINUTE, minute)
        cal.set(Calendar.SECOND, second)

        return cal.time
    }
}