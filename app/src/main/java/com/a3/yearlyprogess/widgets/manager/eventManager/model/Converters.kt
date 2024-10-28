package com.a3.yearlyprogess.widgets.manager.eventManager.model

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

}
