package com.a3.yearlyprogess.feature.events.domain.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Calendar
import java.util.Date
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "event_table")
data class Event(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val eventTitle: String,
    val eventDescription: String,
    val allDayEvent: Boolean = false,
    val eventStartTime: Date,
    val eventEndTime: Date,
    val repeatEventDays: List<RepeatDays> = emptyList(),
    val hasWeekDays: Boolean = false,
    val backgroundImageUri: String? = null,
    
    val recurrenceType: RecurrenceType = RecurrenceType.NONE,
    val recurrenceInterval: Int = 1,
    val recurrenceEndType: RecurrenceEndType = RecurrenceEndType.NEVER,
    val recurrenceEndDate: Long? = null,
    val recurrenceEndOccurrences: Int? = null,
) : Parcelable {

    companion object {
        private const val MAX_ITERATION = 10_0000
    }

    fun nextStartAndEndTime(currentTime: Long = System.currentTimeMillis()): Pair<Long, Long> {
        if (recurrenceType == RecurrenceType.NONE) {
            return Pair(eventStartTime.time, eventEndTime.time)
        }

        var nextStartTime = eventStartTime.time
        var nextEndTime = eventEndTime.time

        if (currentTime < nextEndTime) {
            return Pair(nextStartTime, nextEndTime)
        }

        val eventStartCalendar = Calendar.getInstance().apply { timeInMillis = nextStartTime }
        val duration = nextEndTime - nextStartTime

        var occurrenceCount = 1
        var lastValidStart = nextStartTime
        var lastValidEnd = nextEndTime

        val repeatDaysMapped = repeatEventDays.mapNotNull {
            when (it) {
                RepeatDays.SUNDAY -> Calendar.SUNDAY
                RepeatDays.MONDAY -> Calendar.MONDAY
                RepeatDays.TUESDAY -> Calendar.TUESDAY
                RepeatDays.WEDNESDAY -> Calendar.WEDNESDAY
                RepeatDays.THURSDAY -> Calendar.THURSDAY
                RepeatDays.FRIDAY -> Calendar.FRIDAY
                RepeatDays.SATURDAY -> Calendar.SATURDAY
                else -> null
            }
        }

        while (occurrenceCount < MAX_ITERATION) {
            // Check if current `next` is past the end conditions
            if (recurrenceEndType == RecurrenceEndType.AFTER_OCCURRENCES && recurrenceEndOccurrences != null) {
                if (occurrenceCount > recurrenceEndOccurrences) {
                    return Pair(lastValidStart, lastValidEnd)
                }
            }
            if (recurrenceEndType == RecurrenceEndType.ON_DATE && recurrenceEndDate != null) {
                if (nextStartTime > recurrenceEndDate) {
                    return Pair(lastValidStart, lastValidEnd)
                }
            }

            // We know current `next` is valid. Is it in the future?
            if (nextEndTime > currentTime) {
                return Pair(nextStartTime, nextEndTime)
            }

            // Otherwise, it is valid but in the past, so it becomes our new `lastValid`
            lastValidStart = nextStartTime
            lastValidEnd = nextEndTime

            // Calculate the NEXT occurrence
            when (recurrenceType) {
                RecurrenceType.DAILY -> eventStartCalendar.add(Calendar.DAY_OF_YEAR, recurrenceInterval)
                RecurrenceType.WEEKLY -> {
                    if (repeatDaysMapped.isEmpty()) {
                        eventStartCalendar.add(Calendar.WEEK_OF_YEAR, recurrenceInterval)
                    } else {
                        do {
                            eventStartCalendar.add(Calendar.DAY_OF_YEAR, 1)
                            if (eventStartCalendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
                                if (recurrenceInterval > 1) {
                                    eventStartCalendar.add(Calendar.WEEK_OF_YEAR, recurrenceInterval - 1)
                                }
                            }
                        } while (!repeatDaysMapped.contains(eventStartCalendar.get(Calendar.DAY_OF_WEEK)))
                    }
                }
                RecurrenceType.MONTHLY -> eventStartCalendar.add(Calendar.MONTH, recurrenceInterval)
                RecurrenceType.YEARLY -> eventStartCalendar.add(Calendar.YEAR, recurrenceInterval)
            }
            nextStartTime = eventStartCalendar.timeInMillis
            nextEndTime = nextStartTime + duration
            occurrenceCount++
        }

        return Pair(lastValidStart, lastValidEnd)
    }
}