package com.a3.yearlyprogess.widgets.manager.eventManager.repo

import androidx.lifecycle.LiveData
import com.a3.yearlyprogess.widgets.manager.eventManager.data.EventDao
import com.a3.yearlyprogess.widgets.manager.eventManager.model.Event

class EventRepository(private val eventDao: EventDao) {
  val getAllEvent: LiveData<List<Event>> = eventDao.getAllEvent()


  suspend fun getEvent(id: Int): Event? = eventDao.getEvent(id)

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

  suspend fun insertAllEvents(events: List<Event>) {
    eventDao.insertAllEvents(events)
  }

  suspend fun filterEvent(query: String): List<Event> {
    return eventDao.filterEvent(query)
  }
}
