package com.a3.yearlyprogess.core.domain.model

import com.a3.yearlyprogess.core.util.ProgressSettings
import kotlinx.serialization.Serializable

@Serializable
data class AppSettings(
    val isFirstLaunch: Boolean = true,
    val progressSettings: ProgressSettings = ProgressSettings(),
    val selectedCalendarIds: Set<Long> = emptySet(),
    val automaticallyDetectLocation: Boolean = false
)