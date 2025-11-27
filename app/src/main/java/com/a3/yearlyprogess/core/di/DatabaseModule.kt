package com.a3.yearlyprogess.core.di

import android.content.Context
import androidx.room.Room
import com.a3.yearlyprogess.feature.events.data.local.EventDao
import com.a3.yearlyprogess.feature.events.data.local.EventDatabase
import com.a3.yearlyprogess.feature.events.data.repository.EventRepositoryImpl
import com.a3.yearlyprogess.feature.events.domain.repository.EventRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideEventDatabase(
        @ApplicationContext context: Context
    ): EventDatabase {
        return EventDatabase.getDatabase(context)
    }

    @Provides
    @Singleton
    fun provideEventDao(database: EventDatabase): EventDao {
        return database.eventDao()
    }

    @Provides
    @Singleton
    fun provideEventRepository(eventDao: EventDao): EventRepository {
        return EventRepositoryImpl(eventDao)
    }
}