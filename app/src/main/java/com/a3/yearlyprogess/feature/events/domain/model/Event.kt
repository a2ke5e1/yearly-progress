package com.a3.yearlyprogess.feature.events.domain.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Calendar
import java.util.Date
import kotlinx.parcelize.Parcelize
import java.time.DayOfWeek
import java.time.Duration
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.Locale

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

    val customProgressPrefix: String? = null,
    val customProgressSuffix: String? = null,
    val customProgressRate: Double? = null,
    val customProgressRateUnit: RateUnit? = null,
) : Parcelable {

    fun getCustomProgressString(currentTime: Long = System.currentTimeMillis()): String? {
        val rate = customProgressRate ?: return null
        val unit = customProgressRateUnit ?: return null
        
        val (nextStart, nextEnd) = nextStartAndEndTime(currentTime)
        if (currentTime < nextStart) return null
        
        val value = when (unit) {
            RateUnit.TOTAL -> {
                val totalDuration = (nextEnd - nextStart).toDouble()
                if (totalDuration <= 0) 0.0
                else {
                    val elapsed = (currentTime - nextStart).toDouble()
                    (elapsed / totalDuration) * rate
                }
            }
            RateUnit.SECOND -> (currentTime - nextStart) / 1000.0 * rate
            RateUnit.MINUTE -> (currentTime - nextStart) / (1000.0 * 60.0) * rate
            RateUnit.HOUR -> (currentTime - nextStart) / (1000.0 * 60.0 * 60.0) * rate
            RateUnit.DAY -> (currentTime - nextStart) / (1000.0 * 60.0 * 60.0 * 24.0) * rate
        }

        val formattedValue = if (value % 1.0 == 0.0) {
            value.toLong().toString()
        } else {
            String.format(Locale.getDefault(), "%.2f", value)
        }

        return buildString {
            customProgressPrefix?.let { append(it) }
            append(formattedValue)
            customProgressSuffix?.let { 
                append(it)
            }
        }
    }

    companion object {
        private const val MAX_ITERATION = 10_0000
    }

    fun nextStartAndEndTime(currentTime: Long = System.currentTimeMillis()): Pair<Long, Long> {
        if (recurrenceType == RecurrenceType.NONE) {
            return Pair(eventStartTime.time, eventEndTime.time)
        }

        val zone = ZoneId.systemDefault()
        val startZDT = ZonedDateTime.ofInstant(eventStartTime.toInstant(), zone)
        val endZDT = ZonedDateTime.ofInstant(eventEndTime.toInstant(), zone)
        val duration = Duration.between(startZDT, endZDT)
        val currentZDT = ZonedDateTime.ofInstant(java.time.Instant.ofEpochMilli(currentTime), zone)

        if (currentZDT.isBefore(startZDT.plus(duration))) {
            return Pair(startZDT.toInstant().toEpochMilli(), startZDT.plus(duration).toInstant().toEpochMilli())
        }

        var nextStart = startZDT
        var occurrenceCount = 1
        var lastValidStart = startZDT

        when (recurrenceType) {
            RecurrenceType.DAILY -> {
                if (recurrenceEndType == RecurrenceEndType.NEVER) {
                    val diffDays = java.time.temporal.ChronoUnit.DAYS.between(startZDT.toLocalDate(), currentZDT.toLocalDate())
                    val steps = if (diffDays < 0) 0L else (diffDays / recurrenceInterval)
                    nextStart = startZDT.plusDays(steps * recurrenceInterval)
                    occurrenceCount += steps.toInt()
                    lastValidStart = if (steps > 0) startZDT.plusDays((steps - 1) * recurrenceInterval) else startZDT
                }

                var loopGuard = 0
                while (nextStart.plus(duration).isBefore(currentZDT) && loopGuard < MAX_ITERATION) {
                    lastValidStart = nextStart
                    val candidate = nextStart.plusDays(recurrenceInterval.toLong())

                    if (recurrenceEndType == RecurrenceEndType.AFTER_OCCURRENCES && recurrenceEndOccurrences != null) {
                        if (occurrenceCount >= recurrenceEndOccurrences) break
                    }
                    if (recurrenceEndType == RecurrenceEndType.ON_DATE && recurrenceEndDate != null) {
                        val endDateZDT = java.time.ZonedDateTime.ofInstant(java.time.Instant.ofEpochMilli(recurrenceEndDate), zone)
                        if (candidate.isAfter(endDateZDT)) break
                    }

                    nextStart = candidate
                    occurrenceCount++
                    loopGuard++
                }
            }
            RecurrenceType.WEEKLY -> {
                val targetDays = if (repeatEventDays.isEmpty()) {
                    setOf(startZDT.dayOfWeek)
                } else {
                    repeatEventDays.mapNotNull {
                        when (it) {
                            RepeatDays.MONDAY -> DayOfWeek.MONDAY
                            RepeatDays.TUESDAY -> DayOfWeek.TUESDAY
                            RepeatDays.WEDNESDAY -> DayOfWeek.WEDNESDAY
                            RepeatDays.THURSDAY -> DayOfWeek.THURSDAY
                            RepeatDays.FRIDAY -> DayOfWeek.FRIDAY
                            RepeatDays.SATURDAY -> DayOfWeek.SATURDAY
                            RepeatDays.SUNDAY -> DayOfWeek.SUNDAY
                            else -> null
                        }
                    }.toSet()
                }.ifEmpty { setOf(startZDT.dayOfWeek) }

                var weekStart = startZDT
                if (recurrenceEndType == RecurrenceEndType.NEVER) {
                    val diffWeeks = java.time.temporal.ChronoUnit.WEEKS.between(startZDT.with(java.time.DayOfWeek.MONDAY).toLocalDate(), currentZDT.with(java.time.DayOfWeek.MONDAY).toLocalDate())
                    val validWeekOffset = if (diffWeeks < 0) 0L else (diffWeeks / recurrenceInterval) * recurrenceInterval
                    weekStart = startZDT.plusWeeks(validWeekOffset)
                }

                var loopGuard = 0
                var foundNext = false

                while (!foundNext && loopGuard < MAX_ITERATION) {
                    for (i in 0..6) {
                        val candidateDay = weekStart.with(java.time.DayOfWeek.MONDAY).plusDays(i.toLong())

                        if (candidateDay.dayOfWeek !in targetDays) continue

                        val candidateZDT = candidateDay.withHour(startZDT.hour).withMinute(startZDT.minute).withSecond(startZDT.second).withNano(startZDT.nano)

                        if (candidateZDT.isBefore(startZDT)) continue

                        if (recurrenceEndType == RecurrenceEndType.AFTER_OCCURRENCES && recurrenceEndOccurrences != null) {
                            if (occurrenceCount >= recurrenceEndOccurrences) {
                                nextStart = lastValidStart
                                foundNext = true
                                break
                            }
                        }
                        if (recurrenceEndType == RecurrenceEndType.ON_DATE && recurrenceEndDate != null) {
                            val endDateZDT = java.time.ZonedDateTime.ofInstant(java.time.Instant.ofEpochMilli(recurrenceEndDate), zone)
                            if (candidateZDT.isAfter(endDateZDT)) {
                                nextStart = lastValidStart
                                foundNext = true
                                break
                            }
                        }

                        if (candidateZDT.plus(duration).isAfter(currentZDT) || candidateZDT.plus(duration).isEqual(currentZDT)) {
                            nextStart = candidateZDT
                            foundNext = true
                            break
                        } else {
                            lastValidStart = candidateZDT
                            occurrenceCount++
                        }
                    }
                    if (!foundNext) {
                        weekStart = weekStart.plusWeeks(recurrenceInterval.toLong())
                    }
                    loopGuard++
                }
            }
            RecurrenceType.MONTHLY -> {
                val originalDay = startZDT.dayOfMonth
                fun nextMonthly(base: java.time.ZonedDateTime, monthsToAdd: Long): java.time.ZonedDateTime {
                    val candidate = base.plusMonths(monthsToAdd)
                    val lastDay = candidate.toLocalDate().lengthOfMonth()
                    return candidate.withDayOfMonth(minOf(originalDay, lastDay))
                }

                var totalMonthsAdded = 0L
                if (recurrenceEndType == RecurrenceEndType.NEVER) {
                    val diffMonths = java.time.temporal.ChronoUnit.MONTHS.between(startZDT.toLocalDate(), currentZDT.toLocalDate())
                    val steps = if (diffMonths < 0) 0L else (diffMonths / recurrenceInterval)
                    totalMonthsAdded = steps * recurrenceInterval
                    nextStart = nextMonthly(startZDT, totalMonthsAdded)
                    occurrenceCount += steps.toInt()
                    lastValidStart = if (steps > 0) nextMonthly(startZDT, (steps - 1) * recurrenceInterval) else startZDT
                }

                var loopGuard = 0
                while (nextStart.plus(duration).isBefore(currentZDT) && loopGuard < MAX_ITERATION) {
                    lastValidStart = nextStart
                    val nextMonthsAdded = totalMonthsAdded + recurrenceInterval
                    val candidate = nextMonthly(startZDT, nextMonthsAdded.toLong())

                    if (recurrenceEndType == RecurrenceEndType.AFTER_OCCURRENCES && recurrenceEndOccurrences != null) {
                        if (occurrenceCount >= recurrenceEndOccurrences) break
                    }
                    if (recurrenceEndType == RecurrenceEndType.ON_DATE && recurrenceEndDate != null) {
                        val endDateZDT = java.time.ZonedDateTime.ofInstant(java.time.Instant.ofEpochMilli(recurrenceEndDate), zone)
                        if (candidate.isAfter(endDateZDT)) break
                    }

                    nextStart = candidate
                    totalMonthsAdded = nextMonthsAdded
                    occurrenceCount++
                    loopGuard++
                }
            }
            RecurrenceType.YEARLY -> {
                if (recurrenceEndType == RecurrenceEndType.NEVER) {
                    val diffYears = java.time.temporal.ChronoUnit.YEARS.between(startZDT.toLocalDate(), currentZDT.toLocalDate())
                    val steps = if (diffYears < 0) 0L else (diffYears / recurrenceInterval)
                    nextStart = startZDT.plusYears(steps * recurrenceInterval)
                    occurrenceCount += steps.toInt()
                    lastValidStart = if (steps > 0) startZDT.plusYears((steps - 1) * recurrenceInterval) else startZDT
                }

                var loopGuard = 0
                while (nextStart.plus(duration).isBefore(currentZDT) && loopGuard < MAX_ITERATION) {
                    lastValidStart = nextStart
                    val candidate = nextStart.plusYears(recurrenceInterval.toLong())

                    if (recurrenceEndType == RecurrenceEndType.AFTER_OCCURRENCES && recurrenceEndOccurrences != null) {
                        if (occurrenceCount >= recurrenceEndOccurrences) break
                    }
                    if (recurrenceEndType == RecurrenceEndType.ON_DATE && recurrenceEndDate != null) {
                        val endDateZDT = java.time.ZonedDateTime.ofInstant(java.time.Instant.ofEpochMilli(recurrenceEndDate), zone)
                        if (candidate.isAfter(endDateZDT)) break
                    }

                    nextStart = candidate
                    occurrenceCount++
                    loopGuard++
                }
            }
        }
        return Pair(nextStart.toInstant().toEpochMilli(), nextStart.plus(duration).toInstant().toEpochMilli())
    }

}