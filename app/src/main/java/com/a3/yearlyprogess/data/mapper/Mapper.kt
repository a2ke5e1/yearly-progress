package com.a3.yearlyprogess.data.mapper

import com.a3.yearlyprogess.data.remote.ResultDto
import com.a3.yearlyprogess.domain.model.SunriseSunset
import android.icu.text.SimpleDateFormat
import android.icu.util.TimeZone
import java.util.Locale
import java.util.Date

fun ResultDto.toDomain(): SunriseSunset {

     fun convertToDateTime(time: String): Date {
        val dateTime = "$date $time"
        val formatter = SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.getDefault())
        val timeZone = TimeZone.getTimeZone(timezone)
        formatter.timeZone = timeZone

        val newDate = formatter.parse(dateTime)
        return newDate
    }

    return SunriseSunset(
        sunrise = convertToDateTime(sunrise),
        sunset = convertToDateTime(sunset),
    )
}
