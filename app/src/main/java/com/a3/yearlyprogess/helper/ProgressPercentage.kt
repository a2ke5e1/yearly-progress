package com.a3.yearlyprogess.helper

import java.util.*

class ProgressPercentage {

    private val calendar: Calendar = Calendar.getInstance()

    private fun isLeapYear(year: Int): Boolean = (year % 4 == 0 && year % 100 != 0) || year % 400 == 0

    fun getMonth(str: Boolean = false) : String {
        val monthName = calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault())
        if (monthName == null || !str) {
            return  (calendar.get(Calendar.MONTH) + 1).toString()
        }
        return monthName.toString()
    }
    fun getYear(): String = calendar.get(Calendar.YEAR).toString()
    fun getDay(custom : Boolean = false) : String {
        if (custom) {
            val currentDay = getDay()
            val currentMonth = "%02d".format(getMonth().toInt())
            return "$currentMonth/$currentDay"
        }
        return calendar.get(Calendar.DAY_OF_MONTH).toString()
    }
    fun getWeek(str: Boolean = false): String {
        val weekName = calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault())
        if ( weekName == null || !str) {
            return calendar.get(Calendar.DAY_OF_WEEK).toString()
        }
        return weekName.toString()
    }

    fun getSeconds(field: Int): Long {
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
            else -> -1
        }.toLong()
    }

    fun getSecondsPassed(field: Int) : Long {
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
                val secondsPassed = (getWeek().toInt() - 1) * 24 * 60 * 60 + currentHour * 60 * 60 + currentMinute * 60 + currentSecond
                return secondsPassed.toLong()
            }
            DAY -> {
                val secondsPassed =
                    currentHour * 60 * 60 + currentMinute * 60 + currentSecond
                return secondsPassed.toLong()
            }
            else -> return -1
        }
    }

    fun getPercent(field: Int): Double {
        return (getSecondsPassed(field).toDouble() / getSeconds(field)) * 100
    }

    companion object {
        const val YEAR = 100
        const val MONTH = 101
        const val WEEK = 102
        const val DAY = 103
    }


}

fun Double.format(digits: Int): Double = "%.${digits}f".format(this).toDouble()