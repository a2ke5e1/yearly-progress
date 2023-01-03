package com.a3.yearlyprogess.helper

import android.text.Spannable
import android.text.SpannableString
import android.text.style.RelativeSizeSpan
import android.text.style.SuperscriptSpan
import androidx.annotation.IntDef
import androidx.annotation.RestrictTo
import java.util.*
import kotlin.math.pow

class ProgressPercentage {


    private val calendar: Calendar = Calendar.getInstance()


    private fun isLeapYear(year: Int): Boolean =
        (year % 4 == 0 && year % 100 != 0) || year % 400 == 0


    fun getMonth(str: Boolean = false, isLong: Boolean = false): String {
        val monthName = calendar.getDisplayName(
            Calendar.MONTH, if (isLong) {
                Calendar.LONG
            } else {
                Calendar.SHORT
            }, Locale.getDefault()
        )
        if (monthName == null || !str) {
            return (calendar.get(Calendar.MONTH) + 1).toString()
        }
        return monthName.toString()
    }

    fun getYear(): String = calendar.get(Calendar.YEAR).toString()
    fun getDay(): String = calendar.get(Calendar.DAY_OF_MONTH).toString()
    fun getDay(custom: Boolean = false): SpannableString {
        if (custom) {
            return formatCurrentDay(this)
        }
        return SpannableString(calendar.get(Calendar.DAY_OF_MONTH).toString())
    }

    fun getWeek(str: Boolean = false): String {
        val weekName =
            calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.getDefault())
        if (weekName == null || !str) {
            return calendar.get(Calendar.DAY_OF_WEEK).toString()
        }
        return weekName.toString()
    }

    fun getSeconds(
        @Field field: Int,
        eventStartTimeInMills: Long = 0,
        eventEndDateTimeInMillis: Long = 0
    ): Long {
        return when (field) {
            YEAR -> {
                if (isLeapYear(getYear().toInt())) {
                    366 * 24 * 60 * 60
                } else {
                    365 * 24 * 60 * 60
                }
            }
            MONTH -> calendar.getActualMaximum(Calendar.DAY_OF_MONTH) * 24 * 60 * 60
            WEEK -> 7 * 24 * 60 * 60
            DAY -> 24 * 60 * 60
            CUSTOM_EVENT -> {
                (eventEndDateTimeInMillis - eventStartTimeInMills).div(1000).toLong()
            }
            else -> -1
        }.toLong()
    }

    fun getSecondsPassed(@Field field: Int, eventStartTimeInMills: Long = 0): Long {
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        val currentMinute = calendar.get(Calendar.MINUTE)
        val currentSecond = calendar.get(Calendar.SECOND)
        val currentDay = calendar.get(Calendar.DAY_OF_MONTH) - 1

        when (field) {
            YEAR -> {
                // Need to have its own instance to set the date without interfering other calculation
                val localCalendar = Calendar.getInstance()
                localCalendar.set(getYear().toInt(), 0, 1, 0, 0, 0)
                val firstJan = localCalendar.timeInMillis
                val today = System.currentTimeMillis()
                return (today - firstJan) / 1000
            }
            MONTH -> {
                val secondsPassed =
                    currentDay * 24 * 60 * 60 + currentHour * 60 * 60 + currentMinute * 60 + currentSecond
                return secondsPassed.toLong()
            }
            WEEK -> {
                val secondsPassed =
                    (getWeek().toInt() - 1) * 24 * 60 * 60 + currentHour * 60 * 60 + currentMinute * 60 + currentSecond
                return secondsPassed.toLong()
            }
            DAY -> {
                val secondsPassed =
                    currentHour * 60 * 60 + currentMinute * 60 + currentSecond
                return secondsPassed.toLong()
            }
            CUSTOM_EVENT -> {
                return (System.currentTimeMillis() - eventStartTimeInMills).div(1000).toLong()
            }
            else -> return -1
        }
    }

    fun getPercent(
        @Field field: Int,
        eventStartTimeInMills: Long = 0,
        eventEndDateTimeInMillis: Long = 0
    ): Double {
        return when (field) {
            CUSTOM_EVENT -> (getSecondsPassed(field, eventStartTimeInMills).toDouble() / getSeconds(
                field,
                eventStartTimeInMills,
                eventEndDateTimeInMillis
            )) * 100
            else -> (getSecondsPassed(field).toDouble() / getSeconds(field)) * 100
        }
    }

    companion object {
        @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
        @IntDef(YEAR, MONTH, WEEK, DAY, CUSTOM_EVENT)
        @Retention(AnnotationRetention.SOURCE)
        annotation class Field

        const val YEAR = 100
        const val MONTH = 101
        const val WEEK = 102
        const val DAY = 103
        const val CUSTOM_EVENT = 104


        fun Double.format( digits: Int): Double {
            val p = 10.0.pow(digits.toDouble())
            return (this * p).toLong() / p
        }

        fun formatProgressStyle(progress: Double): SpannableString {
            val widgetText = SpannableString("%,.2f".format(progress) +"%")
            return formatProgressStyle(widgetText)
        }

        fun formatProgressStyle(widgetText: SpannableString): SpannableString {

            var dotPos = widgetText.indexOf('.')
            if (dotPos == -1) {
                dotPos = widgetText.indexOf(',')
            }

            widgetText.setSpan(
                RelativeSizeSpan(0.7f),
                dotPos,
                widgetText.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            return widgetText
        }

        fun formatProgress(progress: Int): SpannableString {
            val spannable = SpannableString("${progress}%")
            spannable.setSpan(
                RelativeSizeSpan(0.7f),
                spannable.length - 1,
                spannable.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            return spannable
        }

        fun formatCurrentDay(progressPercentage: ProgressPercentage): SpannableString {
            val day = progressPercentage.getDay()
            val spannable = SpannableString(
                "${day}${
                    when (day.last()) {
                        '1' -> "st"
                        '2' -> "nd"
                        '3' -> "rd"
                        else -> "th"
                    }
                }"
            )
            spannable.setSpan(
                SuperscriptSpan(),
                spannable.length - 2,
                spannable.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE

            )
            spannable.setSpan(
                RelativeSizeSpan(0.5f),
                spannable.length - 2,
                spannable.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE

            )
            return spannable
        }

    }


}
