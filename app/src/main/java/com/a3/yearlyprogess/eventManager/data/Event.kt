package com.a3.yearlyprogess.eventManager.data

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "event_table")
data class Event(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val eventTitle: String,
    val eventDescription: String,
    val eventStartTime: Long,
    val eventEndTime: Long
)
