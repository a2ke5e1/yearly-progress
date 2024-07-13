package com.a3.yearlyprogess

import android.content.Context
import androidx.preference.PreferenceManager
import java.util.Calendar


enum class TimePeriod {
    DAY, WEEK, MONTH, YEAR
}

fun calculateProgress(context: Context, startTime: Long, endTime: Long): Double {

    val settingPref = PreferenceManager.getDefaultSharedPreferences(context)
    val calculationMode =
        settingPref.getString(context.getString(R.string.app_calculation_type), "0") ?: "0"


    val currentTime = System.currentTimeMillis()
    return if (currentTime in startTime..endTime) {

        if (calculationMode == "1") {
            (endTime - currentTime).toDouble() / (endTime - startTime) * 100
        } else {
            (currentTime - startTime).toDouble() / (endTime - startTime) * 100
        }

    } else 0.0
}

fun calculateTimeLeft(endTime: Long): Long {
    return endTime - System.currentTimeMillis()
}


fun getCurrentPeriodValue(timePeriod: TimePeriod): Int {
    val cal = Calendar.getInstance()
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

fun calculateStartTime(context: Context, timePeriod: TimePeriod): Long {
    val cal = Calendar.getInstance()
    return when (timePeriod) {
        TimePeriod.DAY -> {
            cal.set(
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH),
                0,
                0,
                0
            )
            cal.timeInMillis
        }

        TimePeriod.WEEK -> {

            val settingPref = PreferenceManager.getDefaultSharedPreferences(context)
            val weekStartDay =
                settingPref.getString(context.getString(R.string.app_week_widget_start_day), "0")
                    ?: "0"

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

fun calculateEndTime(context: Context, timePeriod: TimePeriod): Long {
    val cal = Calendar.getInstance()
    return when (timePeriod) {
        TimePeriod.DAY -> {
            cal.set(
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH),
                23,
                59,
                59
            )
            cal.timeInMillis
        }

        TimePeriod.WEEK -> {
            val startTime = calculateStartTime(context, timePeriod)
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
    val names = mapOf(
        1 to "January",
        2 to "February",
        3 to "March",
        4 to "April",
        5 to "May",
        6 to "June",
        7 to "July",
        8 to "August",
        9 to "September",
        10 to "October",
        11 to "November",
        12 to "December"
    )

    return names[monthNumber]?.substring(0, 3) ?: "Unknown"
}

fun getWeekDayName(dayOfWeek: Int): String {
    val names = mapOf(
        1 to "Sunday",
        2 to "Monday",
        3 to "Tuesday",
        4 to "Wednesday",
        5 to "Thursday",
        6 to "Friday",
        7 to "Saturday"
    )
    return names[dayOfWeek]?.substring(0, 3) ?: "Unknown"
}