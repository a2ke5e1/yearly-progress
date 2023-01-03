package com.a3.yearlyprogess.helper

import java.util.*

class ProgressPercentageV2 {

    fun getYear(): Int = Calendar.getInstance().get(Calendar.YEAR)
    fun getMonth(): Int = Calendar.getInstance().get(Calendar.MONTH) + 1
    fun getWeek(): Int = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
    fun getDay(): Int = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)

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

                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.clear(Calendar.MINUTE)
                calendar.clear(Calendar.SECOND)
                calendar.clear(Calendar.MILLISECOND)

                calendar.set(
                    Calendar.DAY_OF_WEEK,
                    calendar.firstDayOfWeek + calendar.getActualMaximum(Calendar.DAY_OF_WEEK) - 1,
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

    fun getProgress(field: Int): Double {
        return (getCurrentTimeMillis() - getStartOfTimeMillis(field)) * 100.0 / (getEndOfTimeMillis(
            field
        ) - getStartOfTimeMillis(field))
    }

    companion object {
        const val YEAR = 100
        const val MONTH = 101
        const val WEEK = 102
        const val DAY = 103
        const val CUSTOM_EVENT = 104
    }
}

class InvalidProgressType(msg: String) : Exception(msg)

fun main(args: Array<String>) {
    println(ProgressPercentageV2().getProgress(ProgressPercentageV2.YEAR))
    println(ProgressPercentageV2().getProgress(ProgressPercentageV2.MONTH))
    println(ProgressPercentageV2().getProgress(ProgressPercentageV2.WEEK))
    println(ProgressPercentageV2().getProgress(ProgressPercentageV2.DAY))

    println(ProgressPercentageV2().getDay())
    println(ProgressPercentageV2().getWeek())
    println(ProgressPercentageV2().getMonth())
    println(ProgressPercentageV2().getYear())

    val c = Calendar.getInstance(Locale.getDefault())
    c.set(
        Calendar.DAY_OF_WEEK, c.firstDayOfWeek
    )
    println(
        c.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault()))

}