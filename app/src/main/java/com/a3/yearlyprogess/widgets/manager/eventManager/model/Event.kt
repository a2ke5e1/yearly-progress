package com.a3.yearlyprogess.widgets.manager.eventManager.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

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
) : Parcelable


// Enum class for days event will be repeated
enum class RepeatDays {
    SUNDAY, MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, EVERY_MONTH, EVERY_YEAR
}

