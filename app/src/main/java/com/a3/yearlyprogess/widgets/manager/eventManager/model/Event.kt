package com.a3.yearlyprogess.widgets.manager.eventManager.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Calendar
import java.util.Date
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "event_table")
data class Event(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val eventTitle: String,
    val eventDescription: String,
    val allDayEvent: Boolean = false,
    val eventStartTime: Date,
    val eventEndTime: Date,
    val repeatEventDays: List<RepeatDays> = emptyList(),
) : Parcelable {

  fun nextStartAndEndTime(currentTime: Long = System.currentTimeMillis()): Pair<Long, Long> {
    var nextStartTime = eventStartTime.time
    var nextEndTime = eventEndTime.time

    // If event has not completed, return current start and end time
    if (currentTime < nextEndTime) {
      return Pair(nextStartTime, nextEndTime)
    }

    // If event is not repeating, return current start and end time
    if (repeatEventDays.isEmpty()) {
      return Pair(nextStartTime, nextEndTime)
    }

    val currCalendar = Calendar.getInstance()
    currCalendar.timeInMillis = currentTime
    val eventStartCalendar = Calendar.getInstance()
    val eventEndCalendar = Calendar.getInstance()

    // Handle yearly recurrence
    if (repeatEventDays.contains(RepeatDays.EVERY_YEAR)) {
      eventStartCalendar.timeInMillis = nextStartTime
      eventEndCalendar.timeInMillis = nextEndTime

      while (currCalendar.timeInMillis >= eventEndCalendar.timeInMillis) {
        eventStartCalendar.add(Calendar.YEAR, 1)
        eventEndCalendar.add(Calendar.YEAR, 1)
      }

      nextStartTime = eventStartCalendar.timeInMillis
      nextEndTime = eventEndCalendar.timeInMillis
    }

    // Handle monthly recurrence
    if (repeatEventDays.contains(RepeatDays.EVERY_MONTH)) {
      eventStartCalendar.timeInMillis = nextStartTime
      eventEndCalendar.timeInMillis = nextEndTime
      while (currCalendar.timeInMillis >= eventEndCalendar.timeInMillis) {
        eventStartCalendar.add(Calendar.MONTH, 1)
        eventEndCalendar.add(Calendar.MONTH, 1)
      }
      nextStartTime = eventStartCalendar.timeInMillis
      nextEndTime = eventEndCalendar.timeInMillis
    }

    val repeatWeekDays =
        repeatEventDays
            .map {
              when (it) {
                RepeatDays.SUNDAY -> Calendar.SUNDAY
                RepeatDays.MONDAY -> Calendar.MONDAY
                RepeatDays.TUESDAY -> Calendar.TUESDAY
                RepeatDays.WEDNESDAY -> Calendar.WEDNESDAY
                RepeatDays.THURSDAY -> Calendar.THURSDAY
                RepeatDays.FRIDAY -> Calendar.FRIDAY
                RepeatDays.SATURDAY -> Calendar.SATURDAY
                else -> -1
              }
            }
            .filter { it != -1 }
            .toList()

    if (repeatWeekDays.isEmpty()) {
      return Pair(nextStartTime, nextEndTime)
    }

    val cal = Calendar.getInstance()
    cal.timeInMillis = currentTime

    val (startHour, startMinute) =
        Calendar.getInstance().run {
          timeInMillis = nextStartTime
          Pair(get(Calendar.HOUR_OF_DAY), get(Calendar.MINUTE))
        }
    val (endHour, endMinute) =
        Calendar.getInstance().run {
          timeInMillis = nextEndTime
          Pair(get(Calendar.HOUR_OF_DAY), get(Calendar.MINUTE))
        }

    while (!repeatWeekDays.contains(cal.get(Calendar.DAY_OF_WEEK))) {
      cal.add(Calendar.DAY_OF_MONTH, 1)
    }

    cal.set(Calendar.HOUR_OF_DAY, startHour)
    cal.set(Calendar.MINUTE, startMinute)
    cal.set(Calendar.SECOND, 0)

    nextStartTime = cal.timeInMillis

    cal.set(Calendar.HOUR_OF_DAY, endHour)
    cal.set(Calendar.MINUTE, endMinute)
    cal.set(Calendar.SECOND, 0)

    nextEndTime = cal.timeInMillis

    return Pair(nextStartTime, nextEndTime)
  }

  companion object {
    private fun Long.getMonth(): Int {
      val calendar = Calendar.getInstance()
      calendar.timeInMillis = this
      return calendar.get(Calendar.MONTH)
    }
  }
}

// Enum class for days event will be repeated
enum class RepeatDays {
  SUNDAY,
  MONDAY,
  TUESDAY,
  WEDNESDAY,
  THURSDAY,
  FRIDAY,
  SATURDAY,
  EVERY_MONTH,
  EVERY_YEAR
}
