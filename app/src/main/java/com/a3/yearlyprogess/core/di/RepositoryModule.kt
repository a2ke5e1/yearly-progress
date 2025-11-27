package com.a3.yearlyprogess.core.di

import com.a3.yearlyprogess.core.data.repository.AppSettingsRepositoryImpl
import com.a3.yearlyprogess.core.domain.repository.AppSettingsRepository
import com.a3.yearlyprogess.data.repository.LocationRepositoryImpl
import com.a3.yearlyprogess.data.repository.SunriseSunsetRepositoryImpl
import com.a3.yearlyprogess.domain.repository.LocationRepository
import com.a3.yearlyprogess.domain.repository.SunriseSunsetRepository
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

}
