package com.a3.yearlyprogess.feature.widgets.domain.model

import androidx.annotation.FloatRange
import androidx.annotation.IntRange
import kotlinx.serialization.Serializable

@Serializable
data class AllInWidgetOptions(
    val theme: WidgetTheme,
    val showDay: Boolean,
    val showWeek: Boolean,
    val showMonth: Boolean,
    val showYear: Boolean,
    val timeLeftCounter: Boolean,
    val dynamicLeftCounter: Boolean,
    val replaceProgressWithDaysLeft: Boolean,
    @param:IntRange(0, 5) val decimalPlaces: Int,
    @param:IntRange(from = 0, to = 100) val backgroundTransparency: Int,
    @param:FloatRange(from = 0.5, to = 2.0) val fontScale: Float
)