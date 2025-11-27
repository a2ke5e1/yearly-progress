package com.a3.yearlyprogess.feature.events.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.a3.yearlyprogess.feature.events.domain.model.Event
import kotlinx.coroutines.flow.Flow

@Dao
interface EventDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE) 
    suspend fun addEvent(event: Event)

    @Insert(onConflict = OnConflictStrategy.REPLACE) 
    suspend fun addAllEvents(events: List<Event>)

    @Transaction
    suspend fun insertAllEvents(events: List<Event>) {
        addAllEvents(events)
    }

    @Update 
    suspend fun updateEvent(event: Event)

    @Delete 
    suspend fun deleteEvent(event: Event)

    @Query("DELETE FROM event_table") 
    suspend fun deleteAllEvents()

    @Query("SELECT * FROM event_table ORDER BY id ASC") 
    fun getAllEvents(): Flow<List<Event>>

    @Query("SELECT * FROM event_table WHERE id = :id") 
    suspend fun getEvent(id: Int): Event?

    @Query(
        "SELECT * FROM event_table WHERE eventTitle LIKE '%'||:query||'%' OR eventDescription LIKE '%'||:query||'%' ORDER BY id ASC"
    )
    suspend fun filterEvent(query: String): List<Event>
}