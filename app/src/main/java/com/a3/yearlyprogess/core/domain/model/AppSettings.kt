package com.a3.yearlyprogess.core.domain.model

import androidx.annotation.IntRange
import com.a3.yearlyprogess.core.util.ProgressSettings
import com.a3.yearlyprogess.feature.widgets.domain.model.WidgetTheme
import kotlinx.serialization.Serializable

@Serializable
data class NotificationSettings(
    val progressShowNotification: Boolean = false,
    val progressShowNotificationYear: Boolean = true,
    val progressShowNotificationMonth: Boolean = true,
    val progressShowNotificationWeek: Boolean = true,
    val progressShowNotificationDay: Boolean = true
)

@Serializable
data class AppSettings(
    val isFirstLaunch: Boolean = true,
    val progressSettings: ProgressSettings = ProgressSettings(),
    val notificationSettings: NotificationSettings = NotificationSettings(),
    val selectedCalendarIds: Set<Long> = emptySet(),
    val automaticallyDetectLocation: Boolean = false,
    val appTheme: WidgetTheme = WidgetTheme.DEFAULT,
    @IntRange(from = 0) val eventProgressDecimalDigits: Int = 2,
    val disableWidgetClickToApp: Boolean = false
)
