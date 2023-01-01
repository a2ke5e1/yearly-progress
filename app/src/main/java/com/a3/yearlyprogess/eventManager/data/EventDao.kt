package com.a3.yearlyprogess.eventManager.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface EventDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addEvent(event: Event)

    @Query("SELECT * FROM event_table ORDER BY id ASC")
    fun getAllEvent(): LiveData<List<Event>>

}
