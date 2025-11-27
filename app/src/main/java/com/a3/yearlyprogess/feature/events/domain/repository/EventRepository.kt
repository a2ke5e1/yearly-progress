package com.a3.yearlyprogess.feature.events.domain.repository

import com.a3.yearlyprogess.feature.events.domain.model.Event
import kotlinx.coroutines.flow.Flow

interface EventRepository {
    val allEvents: Flow<List<Event>>
    suspend fun getEvent(id: Int): Event?
    suspend fun addEvent(event: Event)
    suspend fun updateEvent(event: Event)
    suspend fun deleteEvent(event: Event)
    suspend fun deleteAllEvents()
    suspend fun insertAllEvents(events: List<Event>)
    suspend fun filterEvents(query: String): List<Event>
}