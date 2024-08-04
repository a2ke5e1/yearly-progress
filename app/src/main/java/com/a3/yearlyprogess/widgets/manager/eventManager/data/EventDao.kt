package com.a3.yearlyprogess.widgets.manager.eventManager.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.a3.yearlyprogess.widgets.manager.eventManager.model.Event

@Dao
interface EventDao {

  @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun addEvent(event: Event)

  @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun addAllEvents(events: List<Event>)

  @Transaction
  suspend fun insertAllEvents(events: List<Event>) {
    addAllEvents(events)
  }

  @Update suspend fun updateEvent(event: Event)

  @Delete suspend fun deleteEvent(event: Event)

  @Query("DELETE FROM event_table") suspend fun deleteAllEvents()

  @Query("SELECT * FROM event_table ORDER BY id ASC") fun getAllEvent(): LiveData<List<Event>>

  @Query(
      "SELECT * FROM event_table WHERE eventTitle LIKE '%'||:query||'%' OR eventDescription LIKE '%'||:query||'%' ORDER BY id ASC")
  suspend fun filterEvent(query: String): List<Event>
}
