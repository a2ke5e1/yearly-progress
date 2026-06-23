package com.a3.yearlyprogess.app

import android.app.Application
import com.a3.yearlyprogess.BuildConfig
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.firebase.Firebase
import com.google.firebase.crashlytics.crashlytics
import dagger.hilt.android.HiltAndroidApp
import java.util.Arrays

@HiltAndroidApp
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        Firebase.crashlytics.setCrashlyticsCollectionEnabled(true)
        // Set test device configuration globally
        if (BuildConfig.DEBUG) {
            val configuration = RequestConfiguration.Builder()
                .setTestDeviceIds(Arrays.asList("231158506D258DB9BEC8E8086377082B"))
                .build()
            MobileAds.setRequestConfiguration(configuration)
        }
    }
}
