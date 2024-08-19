package com.a3.yearlyprogess

import android.content.Context
import android.text.Spannable
import android.text.SpannableString
import android.text.style.RelativeSizeSpan
import android.text.style.SuperscriptSpan
import androidx.preference.PreferenceManager
import com.a3.yearlyprogess.widgets.manager.eventManager.model.RepeatDays
import java.util.*
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class YearlyProgressManager(private val context: Context) {

  private val settingPref = PreferenceManager.getDefaultSharedPreferences(context)

  fun setDefaultWeek() {
    val weekStartDay =
        settingPref.getString(context.getString(R.string.app_week_widget_start_day), "0") ?: "0"
    // Log.d("week_set", weekStartDay.toString())
    DEFAULT_WEEK_PREF = weekStartDay.toInt()
  }

  fun setDefaultCalculationMode() {
    val calculationMode =
        settingPref.getString(context.getString(R.string.app_calculation_type), "0") ?: "0"
    // Log.d("calculation_set", calculationMode.toString())
    DEFAULT_CALCULATION_MODE = calculationMode.toInt()
  }

  companion object {
    const val YEAR = 100
    const val MONTH = 101
    const val WEEK = 102
    const val DAY = 103
    const val CUSTOM_EVENT = 104

    private var DEFAULT_WEEK_PREF = 0
    private var DEFAULT_CALCULATION_MODE = 0

    fun getYear(): Int = Calendar.getInstance().get(Calendar.YEAR)

    fun getMonth(): Int = Calendar.getInstance().get(Calendar.MONTH) + 1

    fun getWeek(): Int = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)

    fun getDay(): Int = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)

    fun getMonth(isLong: Boolean): String {
      return Calendar.getInstance()
          .getDisplayName(
              Calendar.MONTH,
              if (isLong) {
                Calendar.LONG
              } else {
                Calendar.SHORT
              },
              Locale.getDefault()) ?: getMonth().toString()
    }

    fun getWeek(isLong: Boolean): String {
      return Calendar.getInstance()
          .getDisplayName(
              Calendar.DAY_OF_WEEK,
              if (isLong) {
                Calendar.LONG
              } else {
                Calendar.SHORT
              },
              Locale.getDefault()) ?: getWeek().toString()
    }

    fun getDay(formatted: Boolean): SpannableString {
      val day = getDay().toString()
      if (!formatted) {
        return SpannableString(day)
      }
      val spannable =
          SpannableString(
              "${day}${
                    when (day) {
                        "11", "12", "13" -> "th"
                        else -> when (day.last()) {
                            '1' -> "st"
                            '2' -> "nd"
                            '3' -> "rd"
                            else -> "th"
                        }
                    }
                }")
      spannable.setSpan(
          SuperscriptSpan(),
          spannable.length - 2,
          spannable.length,
          Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

      spannable.setSpan(
          RelativeSizeSpan(0.5f),
          spannable.length - 2,
          spannable.length,
          Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

      return spannable
    }

    fun getStartOfTimeMillis(field: Int, eventStartMilliSeconds: Long = 0): Long {
      val calendar = Calendar.getInstance(Locale.getDefault())
      return when (field) {
        YEAR -> {
          calendar.set(calendar.get(Calendar.YEAR), 0, 1, 0, 0, 0)
          calendar.timeInMillis
        }

        MONTH -> {
          calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), 1, 0, 0, 0)
          calendar.timeInMillis
        }

        WEEK -> {

          calendar.set(Calendar.HOUR_OF_DAY, 0)
          calendar.clear(Calendar.MINUTE)
          calendar.clear(Calendar.SECOND)
          calendar.clear(Calendar.MILLISECOND)

          if (DEFAULT_WEEK_PREF > 0) {
            calendar.firstDayOfWeek = DEFAULT_WEEK_PREF
          }

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
              0)
          calendar.timeInMillis
        }

        CUSTOM_EVENT -> eventStartMilliSeconds
        else -> throw InvalidProgressType("Invalid Progress Type $field")
      }
    }

    fun getEndOfTimeMillis(field: Int, eventEndMilliSeconds: Long = 0): Long {
      val calendar = Calendar.getInstance(Locale.getDefault())
      return when (field) {
        YEAR -> {
          calendar.set(calendar.get(Calendar.YEAR) + 1, 0, 1, 0, 0, 0)
          calendar.timeInMillis
        }

        MONTH -> {
          calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, 1, 0, 0, 0)
          calendar.timeInMillis
        }

        WEEK -> {
          calendar.timeInMillis =
              getStartOfTimeMillis(WEEK) +
                  (calendar.getActualMaximum(Calendar.DAY_OF_WEEK) * 24 * 60 * 60 * 1000)
          calendar.timeInMillis
        }

        DAY -> {
          calendar.set(
              calendar.get(Calendar.YEAR),
              calendar.get(Calendar.MONTH),
              calendar.get(Calendar.DAY_OF_MONTH) + 1,
              0,
              0,
              0)
          calendar.timeInMillis
        }

        CUSTOM_EVENT -> eventEndMilliSeconds
        else -> throw InvalidProgressType("Invalid Progress Type $field")
      }
    }

    fun getDaysLeft(field: Int, eventEndMilliSeconds: Long = 0): String {
      return (getEndOfTimeMillis(field, eventEndMilliSeconds) - getCurrentTimeMillis())
          .toDuration(DurationUnit.MILLISECONDS)
          .toString()
    }

    fun getCurrentTimeMillis(): Long = System.currentTimeMillis()

    fun getProgress(
        field: Int,
        eventStartMilliSeconds: Long = 0,
        eventEndMilliSeconds: Long = 0
    ): Double {
      return when (DEFAULT_CALCULATION_MODE) {
        1 -> {
          (getEndOfTimeMillis(field, eventEndMilliSeconds) - getCurrentTimeMillis()) * 100.0 /
              (getEndOfTimeMillis(field, eventEndMilliSeconds) -
                  getStartOfTimeMillis(field, eventStartMilliSeconds))
        }

        0 -> {
          (getCurrentTimeMillis() - getStartOfTimeMillis(field, eventStartMilliSeconds)) * 100.0 /
              (getEndOfTimeMillis(field, eventEndMilliSeconds) -
                  getStartOfTimeMillis(field, eventStartMilliSeconds))
        }

        else -> throw InvalidProgressType("Invalid Calculation mode $DEFAULT_CALCULATION_MODE")
      }
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
            RelativeSizeSpan(0.7f), dotPos, widgetText.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
      } catch (ignored: IndexOutOfBoundsException) {}
      return widgetText
    }

    fun formatProgress(progress: Int): SpannableString {
      val spannable = SpannableString("${progress}%")
      spannable.setSpan(
          RelativeSizeSpan(0.7f),
          spannable.length - 1,
          spannable.length,
          Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
      return spannable
    }

    fun getEventProgress(
        eventStartMilliSeconds: Long,
        eventEndMilliSeconds: Long,
        repeatDays: List<RepeatDays>
    ): Triple<Long, Long, Double> {

      var eventStartTimeInMills = eventStartMilliSeconds
      var eventEndDateTimeInMillis = eventEndMilliSeconds

      if (repeatDays.contains(RepeatDays.EVERY_YEAR)) {
        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)

        // change the year of the eventStartTimeInMills and eventEndDateTimeInMillis
        // to the current year

        val eventStartCalendar = Calendar.getInstance()
        eventStartCalendar.timeInMillis = eventStartTimeInMills

        val eventEndCalendar = Calendar.getInstance()
        eventEndCalendar.timeInMillis = eventEndDateTimeInMillis

        val diffYear = currentYear - eventStartCalendar.get(Calendar.YEAR)

        eventStartCalendar.add(Calendar.YEAR, diffYear)
        eventEndCalendar.add(Calendar.YEAR, diffYear)

        eventStartTimeInMills = eventStartCalendar.timeInMillis
        eventEndDateTimeInMillis = eventEndCalendar.timeInMillis
      }

      if (repeatDays.contains(RepeatDays.EVERY_MONTH)) {

        val calendar = Calendar.getInstance()
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentYear = calendar.get(Calendar.YEAR)

        // change the month of the eventStartTimeInMills and eventEndDateTimeInMillis
        // to the current month
        val eventStartCalendar = Calendar.getInstance()
        eventStartCalendar.timeInMillis = eventStartTimeInMills

        val eventEndCalendar = Calendar.getInstance()
        eventEndCalendar.timeInMillis = eventEndDateTimeInMillis

        var diffMonth = currentMonth - eventStartCalendar.get(Calendar.MONTH)
        val diffYear = currentYear - eventStartCalendar.get(Calendar.YEAR)

        diffMonth += diffYear * 12

        eventStartCalendar.add(Calendar.MONTH, diffMonth)
        eventEndCalendar.add(Calendar.MONTH, diffMonth)

        eventStartTimeInMills = eventStartCalendar.timeInMillis
        eventEndDateTimeInMillis = eventEndCalendar.timeInMillis
      }

      val weekDays =
          mapOf(
              Calendar.SUNDAY to RepeatDays.SUNDAY,
              RepeatDays.MONDAY to Calendar.MONDAY,
              RepeatDays.TUESDAY to Calendar.TUESDAY,
              RepeatDays.WEDNESDAY to Calendar.WEDNESDAY,
              RepeatDays.THURSDAY to Calendar.THURSDAY,
              RepeatDays.FRIDAY to Calendar.FRIDAY,
              RepeatDays.SATURDAY to Calendar.SATURDAY)

      if (repeatDays.any { it in weekDays }) {
        val calendar = Calendar.getInstance()
        val currentWeek = weekDays.filter { it.value == calendar.get(Calendar.DAY_OF_WEEK) }.keys

        val eventStartCalendar = Calendar.getInstance()
        eventStartCalendar.timeInMillis = eventStartTimeInMills
        val eventStartHour = eventStartCalendar.get(Calendar.HOUR_OF_DAY)
        val eventStartMinute = eventStartCalendar.get(Calendar.MINUTE)

        val eventEndCalendar = Calendar.getInstance()
        eventEndCalendar.timeInMillis = eventEndDateTimeInMillis
        val eventEndHour = eventEndCalendar.get(Calendar.HOUR_OF_DAY)
        val eventEndMinute = eventEndCalendar.get(Calendar.MINUTE)

        if (currentWeek.isNotEmpty() && repeatDays.contains(currentWeek.first())) {
          // Sets the eventStartTimeInMills and eventEndDateTimeInMillis to the current day
          // since the event is repeating every week, so we don't need to change the time
          // of the event only the date.
          eventStartCalendar.set(Calendar.DATE, calendar.get(Calendar.DATE))
          eventEndCalendar.set(Calendar.DATE, calendar.get(Calendar.DATE))

          eventStartCalendar.set(Calendar.HOUR_OF_DAY, eventStartHour)
          eventStartCalendar.set(Calendar.MINUTE, eventStartMinute)

          eventEndCalendar.set(Calendar.HOUR_OF_DAY, eventEndHour)
          eventEndCalendar.set(Calendar.MINUTE, eventEndMinute)

          eventStartTimeInMills = eventStartCalendar.timeInMillis
          eventEndDateTimeInMillis = eventEndCalendar.timeInMillis
        }
      }

      val progress = getProgress(CUSTOM_EVENT, eventStartTimeInMills, eventEndDateTimeInMillis)

      return Triple(eventStartTimeInMills, eventEndDateTimeInMillis, progress)
    }
  }
}

class InvalidProgressType(msg: String) : Exception(msg)
