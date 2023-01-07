package com.a3.yearlyprogess.helper

import android.text.Spannable
import android.text.SpannableString
import android.text.style.RelativeSizeSpan
import android.text.style.SuperscriptSpan
import java.util.*

class ProgressPercentage {
    companion object {
        const val YEAR = 100
        const val MONTH = 101
        const val WEEK = 102
        const val DAY = 103
        const val CUSTOM_EVENT = 104

        fun getYear(): Int = Calendar.getInstance().get(Calendar.YEAR)
        fun getMonth(): Int = Calendar.getInstance().get(Calendar.MONTH) + 1
        fun getWeek(): Int = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
        fun getDay(): Int = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)

        fun getMonth(isLong: Boolean): String {
            return Calendar.getInstance().getDisplayName(
                Calendar.MONTH, if (isLong) {
                    Calendar.LONG
                } else {
                    Calendar.SHORT
                }, Locale.getDefault()
            ) ?: getMonth().toString()
        }

        fun getWeek(isLong: Boolean): String {
            return Calendar.getInstance().getDisplayName(
                Calendar.DAY_OF_WEEK, if (isLong) {
                    Calendar.LONG
                } else {
                    Calendar.SHORT
                }, Locale.getDefault()
            ) ?: getWeek().toString()
        }

        fun getDay(formatted: Boolean): SpannableString {
            val day = getDay().toString()
            if (!formatted) {
                return SpannableString(day)
            }
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

        fun getStartOfTimeMillis(
            field: Int, eventStartMilliSeconds: Long = 0
        ): Long {
            val calendar = Calendar.getInstance(Locale.getDefault())
            return when (field) {
                YEAR -> {
                    calendar.set(
                        calendar.get(Calendar.YEAR), 0, 1, 0, 0, 0
                    )
                    calendar.timeInMillis
                }
                MONTH -> {
                    calendar.set(
                        calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), 1, 0, 0, 0
                    )
                    calendar.timeInMillis
                }
                WEEK -> {

                    calendar.set(Calendar.HOUR_OF_DAY, 0)
                    calendar.clear(Calendar.MINUTE)
                    calendar.clear(Calendar.SECOND)
                    calendar.clear(Calendar.MILLISECOND)

                    calendar.set(
                        Calendar.DAY_OF_WEEK,
                        calendar.firstDayOfWeek,
                    )
                    calendar.timeInMillis
                }
                DAY -> {
                    calendar.set(
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH),
                        0,
                        0,
                        0
                    )
                    calendar.timeInMillis
                }
                CUSTOM_EVENT -> eventStartMilliSeconds
                else -> throw InvalidProgressType("Invalid Progress Type $field")
            }
        }

        fun getEndOfTimeMillis(
            field: Int, eventEndMilliSeconds: Long = 0
        ): Long {
            val calendar = Calendar.getInstance(Locale.getDefault())
            return when (field) {
                YEAR -> {
                    calendar.set(
                        calendar.get(Calendar.YEAR) + 1, 0, 1, 0, 0, 0
                    )
                    calendar.timeInMillis
                }
                MONTH -> {
                    calendar.set(
                        calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, 1, 0, 0, 0
                    )
                    calendar.timeInMillis
                }
                WEEK -> {
                    calendar.set(
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.firstDayOfWeek + calendar.getActualMaximum(Calendar.DAY_OF_WEEK),
                        0, 0, 0
                    )
                    calendar.timeInMillis
                }
                DAY -> {
                    calendar.set(
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH) + 1,
                        0,
                        0,
                        0
                    )
                    calendar.timeInMillis
                }
                CUSTOM_EVENT -> eventEndMilliSeconds
                else -> throw InvalidProgressType("Invalid Progress Type $field")
            }
        }

        fun getCurrentTimeMillis(): Long = System.currentTimeMillis()

        fun getProgress(
            field: Int, eventStartMilliSeconds: Long = 0, eventEndMilliSeconds: Long = 0
        ): Double {
            return (getCurrentTimeMillis() - getStartOfTimeMillis(
                field, eventStartMilliSeconds
            )) * 100.0 / (getEndOfTimeMillis(
                field, eventEndMilliSeconds
            ) - getStartOfTimeMillis(field, eventStartMilliSeconds))
        }

        fun formatProgressStyle(progress: Double): SpannableString {
            val widgetText = SpannableString("%,.2f".format(progress) + "%")
            return formatProgressStyle(widgetText)
        }

        fun formatProgressStyle(widgetText: SpannableString): SpannableString {

            var dotPos = widgetText.indexOf('.')
            if (dotPos == -1) {
                dotPos = widgetText.indexOf(',')
            }

            try {
                widgetText.setSpan(
                    RelativeSizeSpan(0.7f),
                    dotPos,
                    widgetText.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            } catch (ignored: IndexOutOfBoundsException) {}
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


    }
}

class InvalidProgressType(msg: String) : Exception(msg)
