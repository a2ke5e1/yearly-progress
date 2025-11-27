package com.a3.yearlyprogess.feature.events.data.repository

import com.a3.yearlyprogess.feature.events.data.local.EventDao
import com.a3.yearlyprogess.feature.events.domain.model.Event
import com.a3.yearlyprogess.feature.events.domain.repository.EventRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class EventRepositoryImpl @Inject constructor(
    private val eventDao: EventDao
) : EventRepository {

    override val allEvents: Flow<List<Event>> = eventDao.getAllEvents()

    override suspend fun getEvent(id: Int): Event? = eventDao.getEvent(id)

    override suspend fun addEvent(event: Event) = eventDao.addEvent(event)

    override suspend fun updateEvent(event: Event) = eventDao.updateEvent(event)

    override suspend fun deleteEvent(event: Event) = eventDao.deleteEvent(event)

    override suspend fun deleteAllEvents() = eventDao.deleteAllEvents()

    override suspend fun insertAllEvents(events: List<Event>) =
        eventDao.insertAllEvents(events)

    override suspend fun filterEvents(query: String): List<Event> =
        eventDao.filterEvent(query)
}