package com.a3.yearlyprogess.widgets.manager.eventManager.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import java.util.Calendar
import kotlin.math.abs

@Parcelize
@Entity(tableName = "event_table")
data class Event(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val eventTitle: String,
    val eventDescription: String,
    val allDayEvent: Boolean = false,
    val eventStartTime: Long,
    val eventEndTime: Long,
    val repeatEventDays: List<RepeatDays> = emptyList(),
) : Parcelable {
    fun nextStartAndEndTime(currentTime: Long = System.currentTimeMillis()): Pair<Long, Long> {
        var nextStartTime = eventStartTime
        var nextEndTime = eventEndTime

        // If event has not completed, return current start and end time
        // Do not need to calculate next start and end time
        if (currentTime < nextEndTime) {
            return Pair(nextStartTime, nextEndTime)
        }

        // If event is not repeating, return current start and end time
        // Do not need to calculate next start and end time
        if (repeatEventDays.isEmpty()) {
            return Pair(nextStartTime, nextEndTime)
        }

        // If user has selected to repeat event every year, move to next year to start and end time
        if (repeatEventDays.contains(RepeatDays.EVERY_YEAR)) {
            val currCalendar = Calendar.getInstance()
            val eventCalendar = Calendar.getInstance()

            currCalendar.timeInMillis = currentTime
            eventCalendar.timeInMillis = nextStartTime

            var diffYear = abs(currCalendar.get(Calendar.YEAR) - eventCalendar.get(Calendar.YEAR))
            if (currentTime.getMonth() > nextEndTime.getMonth()) {
                diffYear += 1
            }

            eventCalendar.add(Calendar.YEAR, diffYear)
            nextStartTime = eventCalendar.timeInMillis

            eventCalendar.timeInMillis = nextEndTime
            eventCalendar.add(Calendar.YEAR, diffYear)
            nextEndTime = eventCalendar.timeInMillis

        }

        if (repeatEventDays.contains(RepeatDays.EVERY_MONTH)) {
            val currCalendar = Calendar.getInstance()
            val eventCalendar = Calendar.getInstance()

            currCalendar.timeInMillis = System.currentTimeMillis()
            eventCalendar.timeInMillis = nextStartTime

            val diffMonth = eventCalendar.get(Calendar.MONTH) - currCalendar.get(Calendar.MONTH)

            eventCalendar.add(Calendar.MONTH, diffMonth)
            nextStartTime = eventCalendar.timeInMillis

            eventCalendar.timeInMillis = nextEndTime
            eventCalendar.add(Calendar.MONTH, diffMonth)
            nextEndTime = eventCalendar.timeInMillis

        }


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
