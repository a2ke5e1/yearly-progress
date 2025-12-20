package com.a3.yearlyprogess.core.di

import android.content.Context
import androidx.room.Room
import com.a3.yearlyprogess.feature.events.data.local.EventDao
import com.a3.yearlyprogess.feature.events.data.local.EventDatabase
import com.a3.yearlyprogess.feature.events.data.repository.EventRepositoryImpl
import com.a3.yearlyprogess.feature.events.domain.repository.EventRepository
import com.a3.yearlyprogess.feature.widgets.data.datastore.EventWidgetOptionsDataStore
import com.a3.yearlyprogess.feature.widgets.data.datastore.StandaloneWidgetOptionsDataStore
import com.a3.yearlyprogess.feature.widgets.data.repository.EventWidgetOptionsRepositoryImpl
import com.a3.yearlyprogess.feature.widgets.domain.repository.EventWidgetOptionsRepository
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

    @Provides
    @Singleton
    fun provideEventWidgetOptionsDataStore(
        @ApplicationContext context: Context
    ): EventWidgetOptionsDataStore {
        return EventWidgetOptionsDataStore(context)
    }

    @Provides
    @Singleton
    fun provideStandaloneWidgetOptionsDataStore(
        @ApplicationContext context: Context
    ): StandaloneWidgetOptionsDataStore {
        return StandaloneWidgetOptionsDataStore(context)
    }

}