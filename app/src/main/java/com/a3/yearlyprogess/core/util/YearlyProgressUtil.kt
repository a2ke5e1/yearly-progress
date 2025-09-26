package com.a3.yearlyprogess.core.util

import android.annotation.SuppressLint
import android.icu.text.DateFormat
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.icu.util.ULocale

enum class TimePeriod {
    DAY, WEEK, MONTH, YEAR,
}

enum class CalculationType {
    ELAPSED, // progress so far
    REMAINING, // progress left
}

data class ProgressSettings(
    val uLocale: ULocale = ULocale.getDefault(),
    val calculationType: CalculationType = CalculationType.ELAPSED,
    val weekStartDay: Int = Calendar.SUNDAY,
)

class YearlyProgressUtil(private val settings: ProgressSettings = ProgressSettings()) {
    private fun locale(): ULocale = settings.uLocale

    fun calculateProgress(startTime: Long, endTime: Long): Double {
        val currentTime = System.currentTimeMillis()
        if (currentTime < startTime) return 0.0
        if (currentTime > endTime) return 100.0

        return when (settings.calculationType) {
            CalculationType.REMAINING -> (endTime - currentTime).toDouble() / (endTime - startTime) * 100
            CalculationType.ELAPSED -> (currentTime - startTime).toDouble() / (endTime - startTime) * 100
        }
    }

    fun calculateProgress(timePeriod: TimePeriod): Double {
        val startTime = calculateStartTime(timePeriod)
        val endTime = calculateEndTime(timePeriod)
        return calculateProgress(startTime, endTime)
    }

    fun calculateTimeLeft(endTime: Long): Long {
        val cal = Calendar.getInstance(locale())
        return endTime - cal.timeInMillis
    }

    fun getCurrentPeriodValue(timePeriod: TimePeriod): Int {
        val cal = Calendar.getInstance(locale())
        return when (timePeriod) {
            TimePeriod.DAY -> cal.get(Calendar.DAY_OF_MONTH)
            TimePeriod.WEEK -> cal.get(Calendar.DAY_OF_WEEK)
            TimePeriod.MONTH -> cal.get(Calendar.MONTH) + 1
            TimePeriod.YEAR -> cal.get(Calendar.YEAR)
        }
    }

    fun getOrdinalSuffix(number: Int): String = when {
        number % 100 in 11..13 -> "th"
        number % 10 == 1 -> "st"
        number % 10 == 2 -> "nd"
        number % 10 == 3 -> "rd"
        else -> "th"
    }

    fun calculateStartTime(timePeriod: TimePeriod): Long {
        val cal = Calendar.getInstance(locale())
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
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.clear(Calendar.MINUTE)
                cal.clear(Calendar.SECOND)
                cal.clear(Calendar.MILLISECOND)

                cal.firstDayOfWeek = settings.weekStartDay
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
        val cal = Calendar.getInstance(locale())
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
                startTime + (7L * 24 * 60 * 60 * 1000) // exactly 1 week
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
        val cal = Calendar.getInstance(locale())
        cal.set(Calendar.MONTH, monthNumber - 1)
        return DateFormat.getPatternInstance(DateFormat.ABBR_MONTH, locale()).format(cal.time)
    }

    @SuppressLint("SimpleDateFormat")
    fun getYearName(yearNumber: Int): String {
        val cal = Calendar.getInstance(locale())
        cal.set(Calendar.YEAR, yearNumber)
        return SimpleDateFormat("yyyy", locale()).format(cal.time)
    }

    fun getDayName(dayNumber: Int): String {
        val cal = Calendar.getInstance(locale())
        cal.set(Calendar.DAY_OF_MONTH, dayNumber)
        return DateFormat.getPatternInstance(DateFormat.NUM_MONTH_DAY, locale()).format(cal.time)
    }

    fun getWeekDayName(dayOfWeek: Int): String {
        val cal = Calendar.getInstance(locale())
        cal.set(Calendar.DAY_OF_WEEK, dayOfWeek)
        return DateFormat.getPatternInstance(DateFormat.ABBR_WEEKDAY, locale()).format(cal.time)
    }
}
