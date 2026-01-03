package com.a3.yearlyprogess.core.di

import android.content.Context
import com.a3.yearlyprogess.core.util.ConsentManager
import com.a3.yearlyprogess.core.util.IConsentManager
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import dagger.Provides

@Module
@InstallIn(SingletonComponent::class)
object ConsentModule {

    @Provides
    @Singleton
    fun provideConsentManager(@ApplicationContext context: Context): IConsentManager {
        return ConsentManager(context)
    }
}
