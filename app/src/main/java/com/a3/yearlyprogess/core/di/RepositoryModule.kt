package com.a3.yearlyprogess.core.di

import com.a3.yearlyprogess.core.data.repository.AppSettingsRepositoryImpl
import com.a3.yearlyprogess.core.data.repository.CalendarRepositoryImpl
import com.a3.yearlyprogess.core.domain.repository.AppSettingsRepository
import com.a3.yearlyprogess.core.domain.repository.CalendarRepository
import com.a3.yearlyprogess.data.repository.LocationRepositoryImpl
import com.a3.yearlyprogess.data.repository.SunriseSunsetRepositoryImpl
import com.a3.yearlyprogess.domain.repository.LocationRepository
import com.a3.yearlyprogess.domain.repository.SunriseSunsetRepository
import com.a3.yearlyprogess.feature.widgets.data.repository.CalendarWidgetOptionsRepositoryImpl
import com.a3.yearlyprogess.feature.widgets.data.repository.EventWidgetOptionsRepositoryImpl
import com.a3.yearlyprogess.feature.widgets.data.repository.StandaloneWidgetOptionsRepositoryImpl
import com.a3.yearlyprogess.feature.widgets.domain.repository.CalendarWidgetOptionsRepository
import com.a3.yearlyprogess.feature.widgets.domain.repository.EventWidgetOptionsRepository
import com.a3.yearlyprogess.feature.widgets.domain.repository.StandaloneWidgetOptionsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindSunriseSunsetRepository(
        impl: SunriseSunsetRepositoryImpl
    ): SunriseSunsetRepository

    @Binds
    @Singleton
    abstract fun bindAppSettingsRepository(
        appSettingsRepositoryImpl: AppSettingsRepositoryImpl
    ): AppSettingsRepository

    @Binds
    @Singleton
    abstract fun bindLocationRepository(
        locationRepositoryImpl: LocationRepositoryImpl
    ): LocationRepository

    @Binds
    @Singleton
    abstract fun bindCalendarRepository(
        calendarRepositoryImpl: CalendarRepositoryImpl
    ): CalendarRepository

    @Binds
    @Singleton
    abstract fun bindEventWidgetOptionsRepository(
        impl: EventWidgetOptionsRepositoryImpl
    ): EventWidgetOptionsRepository


    @Binds
    @Singleton
    abstract fun bindStandaloneWidgetOptionsRepository(
        impl: StandaloneWidgetOptionsRepositoryImpl
    ): StandaloneWidgetOptionsRepository

    @Binds
    @Singleton
    abstract fun bindCalendarWidgetOptionsRepository(
        impl: CalendarWidgetOptionsRepositoryImpl
    ): CalendarWidgetOptionsRepository

}
