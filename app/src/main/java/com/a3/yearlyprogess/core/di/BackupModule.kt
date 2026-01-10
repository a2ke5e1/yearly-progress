package com.a3.yearlyprogess.core.di

import android.content.Context
import com.a3.yearlyprogess.core.backup.BackupManager
import com.a3.yearlyprogess.core.domain.repository.AppSettingsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object BackupModule {

    @Provides
    @Singleton
    fun provideBackupManager(
        @ApplicationContext context: Context,
        appSettingsRepository: AppSettingsRepository
    ): BackupManager {
        return BackupManager(
            context = context,
            appSettingsRepository = appSettingsRepository,
            json = Json { ignoreUnknownKeys = true }
        )
    }
}