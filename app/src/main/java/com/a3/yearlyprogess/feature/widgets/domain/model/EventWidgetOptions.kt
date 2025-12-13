package com.a3.yearlyprogess.feature.widgets.domain.model

import androidx.annotation.FloatRange
import androidx.annotation.IntRange
import kotlinx.serialization.Serializable

@Serializable
data class EventWidgetOptions(
    val theme: WidgetTheme,
    val timeStatusCounter: Boolean,
    val dynamicTimeStatusCounter: Boolean,
    @param:IntRange(0, 5) val decimalDigits: Int,
    @param:IntRange(from = 0, to = 100) val backgroundTransparency: Int,
    @param:FloatRange(from = 0.5, to = 2.0) val fontScale: Float,
    val showEventImage: Boolean,
    val selectedEventIds: Set<Int> = emptySet()
)
