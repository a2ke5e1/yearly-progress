package com.a3.yearlyprogess.eventManager.repo

import androidx.lifecycle.LiveData
import com.a3.yearlyprogess.eventManager.model.Event
import com.a3.yearlyprogess.eventManager.data.EventDao

class EventRepository(private val eventDao: EventDao) {

    val getAllEvent: LiveData<List<Event>> = eventDao.getAllEvent()

    suspend fun addEvent(event: Event) {
        eventDao.addEvent(event)
    }

    suspend fun updateEvent(event: Event) {
        eventDao.updateEvent(event)
    }

    suspend fun deleteEvent(event: Event) {
        eventDao.deleteEvent(event)
    }

    suspend fun deleteAllEvent() {
        eventDao.deleteAllEvents()
    }

}