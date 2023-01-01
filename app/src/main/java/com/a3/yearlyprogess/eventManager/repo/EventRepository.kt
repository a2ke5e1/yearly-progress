package com.a3.yearlyprogess.eventManager.repo

import androidx.lifecycle.LiveData
import com.a3.yearlyprogess.eventManager.data.Event
import com.a3.yearlyprogess.eventManager.data.EventDao

class EventRepository(private val eventDao: EventDao) {

    val getAllEvent: LiveData<List<Event>> = eventDao.getAllEvent()

    suspend fun addEvent(event: Event) {
        eventDao.addEvent(event)
    }

}