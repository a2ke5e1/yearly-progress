package com.a3.yearlyprogess.core.util

import android.annotation.SuppressLint
import android.content.Context
import android.icu.text.DateFormat
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.icu.util.ULocale
import android.text.Spannable
import android.text.SpannableString
import android.text.style.RelativeSizeSpan
import android.text.style.SuperscriptSpan
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.Locale

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
    val decimalDigits: Int = 2,
)

class YearlyProgressUtil(val settings: ProgressSettings = ProgressSettings()) {
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
                    0, 0, 0
                )
                cal.set(Calendar.MILLISECOND, 0) // reset ms
                cal.timeInMillis
            }

            TimePeriod.WEEK -> {
                // This logic correctly finds the beginning of the current week
                cal.firstDayOfWeek = settings.weekStartDay
                cal.set(Calendar.DAY_OF_WEEK, settings.weekStartDay)

                // Reset time to the beginning of the day (midnight)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)

                // If today is before the weekStartDay (e.g., today is Mon, start is Wed),
                // the calendar will have jumped to the *next* week. We need to go back one week.
                if (cal.timeInMillis > System.currentTimeMillis()) {
                    cal.add(Calendar.WEEK_OF_YEAR, -1)
                }

                cal.timeInMillis
            }

            TimePeriod.MONTH -> {
                cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), 1, 0, 0, 0)
                cal.set(Calendar.MILLISECOND, 0)
                cal.timeInMillis
            }

            TimePeriod.YEAR -> {
                cal.set(cal.get(Calendar.YEAR), 0, 1, 0, 0, 0)
                cal.set(Calendar.MILLISECOND, 0)
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
                    0, 0, 0
                )
                cal.set(Calendar.MILLISECOND, 0)
                cal.timeInMillis
            }

            TimePeriod.WEEK -> {
                // Get the corrected start time
                val startTime = calculateStartTime(timePeriod)

                // Create a new calendar instance from the start time
                val endCal = Calendar.getInstance(locale())
                endCal.timeInMillis = startTime

                // Safely add exactly one week
                endCal.add(Calendar.WEEK_OF_YEAR, 1)

                endCal.timeInMillis
            }

            TimePeriod.MONTH -> {
                cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, 1, 0, 0, 0)
                cal.set(Calendar.MILLISECOND, 0)
                cal.timeInMillis
            }

            TimePeriod.YEAR -> {
                cal.set(cal.get(Calendar.YEAR) + 1, 0, 1, 0, 0, 0)
                cal.set(Calendar.MILLISECOND, 0)
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

    companion object {
        fun Int.formattedDay(yp: YearlyProgressUtil): SpannableString {
            val ordinalSuffix = yp.getOrdinalSuffix(this)

            val numberFormat = NumberFormat.getNumberInstance(Locale.getDefault()) as DecimalFormat
            numberFormat.maximumFractionDigits = 0
            val formattedNumber = numberFormat.format(this)

            val stringBuilder = StringBuilder()
            stringBuilder.append(formattedNumber)
            stringBuilder.append(ordinalSuffix)

            val spannable = SpannableString(stringBuilder.toString())
            spannable.setSpan(
                SuperscriptSpan(),
                spannable.length - 2,
                spannable.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE,
            )

            spannable.setSpan(
                RelativeSizeSpan(0.5f),
                spannable.length - 2,
                spannable.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE,
            )

            return spannable
        }

        fun Int.toFormattedTimePeriod(yp: YearlyProgressUtil, timePeriod: TimePeriod): SpannableString {
            return when (timePeriod) {
                TimePeriod.DAY -> this.formattedDay(yp)
                TimePeriod.MONTH -> SpannableString(yp.getMonthName(this))
                TimePeriod.WEEK -> SpannableString(yp.getWeekDayName(this))
                else -> {
                    val numberFormat = NumberFormat.getNumberInstance(Locale.getDefault()) as DecimalFormat
                    numberFormat.maximumFractionDigits = 0

                    // Don't add commas to the number
                    numberFormat.isGroupingUsed = false

                    val formattedNumber = numberFormat.format(this)
                    SpannableString(formattedNumber)
                }
            }
        }
    }
}
