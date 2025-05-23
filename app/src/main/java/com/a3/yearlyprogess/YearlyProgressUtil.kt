package com.a3.yearlyprogess

import android.content.Context
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.icu.util.ULocale
import android.location.Location
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import com.a3.yearlyprogess.data.SunriseSunsetApi
import com.a3.yearlyprogess.data.models.SunriseSunsetResponse
import com.google.gson.Gson
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

enum class TimePeriod {
  DAY,
  WEEK,
  MONTH,
  YEAR,
}

class YearlyProgressUtil(val context: Context) {
  private val settingPref = PreferenceManager.getDefaultSharedPreferences(context)

  fun getULocale(): ULocale {
    val defaultULocale = ULocale.getDefault()
    val userCalendarType =
        settingPref.getString(context.getString(R.string.app_calendar_type), null)
    if (userCalendarType != null && userCalendarType != "default") {
      return ULocale(defaultULocale.toString() + "@calendar=${userCalendarType}")
    }
    return defaultULocale
  }

  fun calculateProgress(
      startTime: Long,
      endTime: Long,
  ): Double {
    val calculationMode =
        settingPref.getString(context.getString(R.string.app_calculation_type), "0") ?: "0"
    val currentTime = System.currentTimeMillis()
    if (currentTime < startTime) {
      return 0.0
    }
    if (currentTime > endTime) {
      return 100.0
    }
    return if (calculationMode == "1") {
      (endTime - currentTime).toDouble() / (endTime - startTime) * 100
    } else {
      (currentTime - startTime).toDouble() / (endTime - startTime) * 100
    }
  }

  fun calculateProgress(
      timePeriod: TimePeriod,
  ): Double {
    val startTime = calculateStartTime(timePeriod)
    val endTime = calculateEndTime(timePeriod)
    return calculateProgress(startTime, endTime)
  }

  fun calculateTimeLeft(endTime: Long): Long {
    val cal = Calendar.getInstance(getULocale())
    return endTime - cal.timeInMillis
  }

  fun getCurrentPeriodValue(timePeriod: TimePeriod): Int {
    val cal = Calendar.getInstance(getULocale())
    return when (timePeriod) {
      TimePeriod.DAY -> cal.get(Calendar.DAY_OF_MONTH)
      TimePeriod.WEEK -> cal.get(Calendar.DAY_OF_WEEK)
      TimePeriod.MONTH -> cal.get(Calendar.MONTH) + 1 // Calendar.MONTH is zero-based
      TimePeriod.YEAR -> cal.get(Calendar.YEAR)
    }
  }

  fun getOrdinalSuffix(number: Int): String {
    return when {
      number % 100 in 11..13 -> "th"
      number % 10 == 1 -> "st"
      number % 10 == 2 -> "nd"
      number % 10 == 3 -> "rd"
      else -> "th"
    }
  }

  fun calculateStartTime(
      timePeriod: TimePeriod,
  ): Long {
    val cal = Calendar.getInstance(getULocale())
    return when (timePeriod) {
      TimePeriod.DAY -> {
        cal.set(
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH),
            0,
            0,
            0,
        )
        cal.timeInMillis
      }

      TimePeriod.WEEK -> {
        val weekStartDay =
            settingPref.getString(context.getString(R.string.app_week_widget_start_day), "0") ?: "0"

        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.clear(Calendar.MINUTE)
        cal.clear(Calendar.SECOND)
        cal.clear(Calendar.MILLISECOND)

        // This make sures if week prefs is not 0
        // then it loads the user prefs
        // otherwise it load default
        if (weekStartDay.toInt() > 0) {
          cal.firstDayOfWeek = weekStartDay.toInt()
        }

        cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
        cal.timeInMillis
      }

      TimePeriod.MONTH -> {
        cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), 1, 0, 0, 0)
        cal.timeInMillis
      }

      TimePeriod.YEAR -> {
        cal.set(cal.get(Calendar.YEAR), 0, 1, 0, 0, 0)
        cal.timeInMillis
      }
    }
  }

  fun calculateEndTime(
      timePeriod: TimePeriod,
  ): Long {
    val cal = Calendar.getInstance(getULocale())
    return when (timePeriod) {
      TimePeriod.DAY -> {
        cal.set(
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH) + 1,
            0,
            0,
            0,
        )
        cal.timeInMillis
      }

      TimePeriod.WEEK -> {
        val startTime = calculateStartTime(timePeriod)
        cal.timeInMillis =
            startTime + (cal.getActualMaximum(Calendar.DAY_OF_WEEK) * 24 * 60 * 60 * 1000)
        cal.timeInMillis
      }

      TimePeriod.MONTH -> {
        cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, 1, 0, 0, 0)
        cal.timeInMillis
      }

      TimePeriod.YEAR -> {
        cal.set(cal.get(Calendar.YEAR) + 1, 0, 1, 0, 0, 0)
        cal.timeInMillis
      }
    }
  }

  fun getMonthName(monthNumber: Int): String {
    val uLocale = getULocale()
    val cal = Calendar.getInstance(getULocale())
    cal.set(Calendar.MONTH, monthNumber - 1)

    val formatter = SimpleDateFormat("MMM", uLocale) // "MMMM" gives full month name
    val monthName = formatter.format(cal.time)

    return monthName ?: "Unknown"
  }

  fun getYearName(yearNumber: Int): String {
    val uLocale = getULocale()
    val cal = Calendar.getInstance(getULocale())
    cal.set(Calendar.YEAR, yearNumber)

    val formatter = SimpleDateFormat("yyyy", uLocale)
    val yearName = formatter.format(cal.time)

    return yearName ?: "Unknown"
  }

  fun getDayName(dayNumber: Int): String {
    val uLocale = getULocale()
    val cal = Calendar.getInstance(getULocale())
    cal.set(Calendar.DAY_OF_MONTH, dayNumber)

    val formatter = SimpleDateFormat("dd", uLocale)
    val dayName = formatter.format(cal.time)

    return dayName ?: "Unknown"
  }

  fun getWeekDayName(dayOfWeek: Int): String {
    val uLocale = getULocale()
    val cal = Calendar.getInstance(uLocale)
    cal.set(Calendar.DAY_OF_WEEK, dayOfWeek)

    val formatter = SimpleDateFormat("EEE", uLocale) // "EEEE" gives full WEEK name
    val dayName = formatter.format(cal.time)

    return dayName ?: "Unknown"
  }
}

private const val SUNRISE_SUNSET_BASE_URL = "https://api.sunrisesunset.io/"

fun provideSunriseSunsetApi(): SunriseSunsetApi =
    Retrofit.Builder()
        .baseUrl(SUNRISE_SUNSET_BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        // .client(okHttpClient)
        .build()
        .create(SunriseSunsetApi::class.java)

fun cacheSunriseSunset(
    context: Context,
    sunriseSunset: SunriseSunsetResponse,
) {
  val key = ContextCompat.getString(context, R.string.sunrise_sunset_data)
  val pref = context.getSharedPreferences(key, Context.MODE_PRIVATE)
  val editor = pref.edit()
  val gson = Gson()
  val json = gson.toJson(sunriseSunset)
  editor.putString(key, json)

  editor.apply()
}

fun loadCachedSunriseSunset(context: Context): SunriseSunsetResponse? {
  val key = ContextCompat.getString(context, R.string.sunrise_sunset_data)
  val pref = context.getSharedPreferences(key, Context.MODE_PRIVATE)
  val gson = Gson()
  val json = pref.getString(key, null) ?: return null
  return try {
    gson.fromJson(json, SunriseSunsetResponse::class.java)
  } catch (ex: Exception) {
    null
  }
}

fun cacheLocation(
    context: Context,
    location: Location,
) {
  val key = ContextCompat.getString(context, R.string.location_data_pref)
  val pref = context.getSharedPreferences(key, Context.MODE_PRIVATE)
  val editor = pref.edit()

  editor.putString("lat", location.latitude.toString())
  editor.putString("lon", location.longitude.toString())

  editor.apply()
}

fun loadCachedLocation(context: Context): Location? {
  val key = ContextCompat.getString(context, R.string.location_data_pref)
  val pref = context.getSharedPreferences(key, Context.MODE_PRIVATE)

  val lat = pref.getString("lat", null)
  val lon = pref.getString("lon", null)

  if (lat == null || lon == null) {
    return null
  }

  return Location("").apply {
    latitude = lat.toDouble()
    longitude = lon.toDouble()
  }
}

fun invalidateCachedSunriseSunset(
    context: Context,
) {
  val key = ContextCompat.getString(context, R.string.sunrise_sunset_data)
  val pref = context.getSharedPreferences(key, Context.MODE_PRIVATE)
  val editor = pref.edit()
  editor.putString(key, null)
  editor.apply()
}

fun getCurrentDate(): String {
  val cal = java.util.Calendar.getInstance()
  cal.timeInMillis = System.currentTimeMillis()

  return StringBuilder("")
      .append(cal.get(Calendar.YEAR))
      .append("-")
      .append(if (cal.get(Calendar.MONTH) + 1 < 10) "0" else "")
      .append(cal.get(Calendar.MONTH) + 1)
      .append("-")
      .append(cal.get(Calendar.DATE))
      .toString()
}

fun getDateRange(daysToAdd: Int): String {
  val cal = java.util.Calendar.getInstance()
  cal.timeInMillis = System.currentTimeMillis()
  cal.add(Calendar.DATE, daysToAdd)

  return "${cal.get(Calendar.YEAR)}-${cal.get(Calendar.MONTH) + 1}-${cal.get(Calendar.DATE)}"
}
