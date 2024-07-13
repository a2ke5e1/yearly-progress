package com.a3.yearlyprogess

import java.util.Calendar


enum class TimePeriod {
    DAY, WEEK, MONTH, YEAR
}

fun calculateProgress(startTime: Long, endTime: Long): Double {
    val currentTime = System.currentTimeMillis()
    return if (currentTime in startTime..endTime) {
        (currentTime - startTime).toDouble() / (endTime - startTime) * 100
    } else 0.0
}

fun calculateTimeLeft(endTime: Long): Long {
    return endTime - System.currentTimeMillis()
}


fun getCurrentPeriodValue(timePeriod: TimePeriod): Int {
    val cal = Calendar.getInstance()
    return when (timePeriod) {
        TimePeriod.DAY -> cal.get(Calendar.DAY_OF_MONTH)
        TimePeriod.WEEK -> cal.get(Calendar.WEEK_OF_YEAR)
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

fun calculateStartTime(timePeriod: TimePeriod): Long {
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
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.clear(Calendar.MINUTE)
            cal.clear(Calendar.SECOND)
            cal.clear(Calendar.MILLISECOND)
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

fun calculateEndTime(timePeriod: TimePeriod): Long {
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

    return names[monthNumber] ?: "Unknown"
}

