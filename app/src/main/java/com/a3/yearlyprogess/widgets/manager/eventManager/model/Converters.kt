package com.a3.yearlyprogess.widgets.manager.eventManager.model

import androidx.room.TypeConverter

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
}
