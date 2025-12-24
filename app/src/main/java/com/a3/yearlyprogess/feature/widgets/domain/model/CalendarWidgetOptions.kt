package com.a3.yearlyprogess.feature.widgets.domain.model

import kotlinx.serialization.Serializable

/**
 * Configuration options for the Calendar widget
 * Follows the same pattern as EventWidgetOptions
 */
@Serializable
data class CalendarWidgetOptions(
    val theme: WidgetTheme = WidgetTheme.DEFAULT,
    val timeStatusCounter: Boolean = true,
    val dynamicTimeStatusCounter: Boolean = false,
    val replaceProgressWithTimeLeft: Boolean = false,
    val decimalDigits: Int = 2,
    val backgroundTransparency: Int = 100,
    val fontScale: Float = 1.0f,
    val selectedCalendarIds: Set<Long> = emptySet()
)