package com.a3.yearlyprogess.core.di

import com.a3.yearlyprogess.domain.repository.SunriseSunsetRepository
import com.a3.yearlyprogess.data.repository.SunriseSunsetRepositoryImpl
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
}
