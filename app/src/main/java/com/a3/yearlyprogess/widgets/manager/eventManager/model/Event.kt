package com.a3.yearlyprogess.widgets.manager.eventManager.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import java.util.Calendar

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
        if (currentTime < nextEndTime) {
            return Pair(nextStartTime, nextEndTime)
        }

        // If event is not repeating, return current start and end time
        if (repeatEventDays.isEmpty()) {
            return Pair(nextStartTime, nextEndTime)
        }

        val currCalendar = Calendar.getInstance()
        currCalendar.timeInMillis = currentTime
        val eventCalendar = Calendar.getInstance()

        // Handle yearly recurrence
        if (repeatEventDays.contains(RepeatDays.EVERY_YEAR)) {
            eventCalendar.timeInMillis = nextStartTime
            var yearsAdd = 0
            while (currCalendar.timeInMillis >= eventCalendar.timeInMillis) {
                eventCalendar.add(Calendar.YEAR, 1)
                yearsAdd++
            }
            nextStartTime = eventCalendar.timeInMillis

            eventCalendar.timeInMillis = nextEndTime
            eventCalendar.add(Calendar.YEAR, yearsAdd)
            nextEndTime = eventCalendar.timeInMillis
        }

        // Handle monthly recurrence
        if (repeatEventDays.contains(RepeatDays.EVERY_MONTH)) {
            eventCalendar.timeInMillis = nextStartTime
            var monthsAdd = 0
            while (currCalendar.timeInMillis >= eventCalendar.timeInMillis) {
                eventCalendar.add(Calendar.MONTH, 1)
                monthsAdd++
            }
            nextStartTime = eventCalendar.timeInMillis

            eventCalendar.timeInMillis = nextEndTime
            eventCalendar.add(Calendar.MONTH, monthsAdd)
            nextEndTime = eventCalendar.timeInMillis
        }

        // Handle weekly recurrence
        if (repeatEventDays.any {
                it in listOf(
                    RepeatDays.SUNDAY,
                    RepeatDays.MONDAY,
                    RepeatDays.TUESDAY,
                    RepeatDays.WEDNESDAY,
                    RepeatDays.THURSDAY,
                    RepeatDays.FRIDAY,
                    RepeatDays.SATURDAY
                )
            }) {
            val nextDayOfWeek = repeatEventDays.map {
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
            }.filter { it != -1 }

            var daysUntilNext = Int.MAX_VALUE
            for (day in nextDayOfWeek) {
                val diff = (day + 7 - currCalendar.get(Calendar.DAY_OF_WEEK)) % 7
                if (diff > 0 && diff < daysUntilNext) daysUntilNext = diff
            }

            if (daysUntilNext != Int.MAX_VALUE) {
                currCalendar.add(Calendar.DAY_OF_YEAR, daysUntilNext)
                nextStartTime = currCalendar.timeInMillis

                currCalendar.timeInMillis = nextEndTime
                currCalendar.add(Calendar.DAY_OF_YEAR, daysUntilNext)
                nextEndTime = currCalendar.timeInMillis
            }
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
