package com.a3.yearlyprogess.feature.events.domain.model


import androidx.room.TypeConverter
import java.util.Date

class Converters {
    @TypeConverter
    fun fromRepeatDaysList(value: List<RepeatDays>): String {
        return value.joinToString(",")
    }

    @TypeConverter
    fun toRepeatDaysList(value: String): List<RepeatDays> {
        if (value.isEmpty()) {
            return emptyList()
        }
        return value.split(",").map { RepeatDays.valueOf(it) }
    }

    @TypeConverter
    fun fromDate(date: Date): Long {
        return date.time
    }

    @TypeConverter
    fun toDate(date: Long): Date {
        return Date(date)
    }

    @TypeConverter
    fun fromRecurrenceType(value: RecurrenceType): String {
        return value.name
    }

    @TypeConverter
    fun toRecurrenceType(value: String): RecurrenceType {
        return try {
            RecurrenceType.valueOf(value)
        } catch (e: Exception) {
            RecurrenceType.NONE
        }
    }

    @TypeConverter
    fun fromRecurrenceEndType(value: RecurrenceEndType): String {
        return value.name
    }

    @TypeConverter
    fun toRecurrenceEndType(value: String): RecurrenceEndType {
        return try {
            RecurrenceEndType.valueOf(value)
        } catch (e: Exception) {
            RecurrenceEndType.NEVER
        }
    }
}
