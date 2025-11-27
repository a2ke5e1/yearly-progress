package com.a3.yearlyprogess.feature.events.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class RepeatDays {
    SUNDAY,
    MONDAY,
    TUESDAY,
    WEDNESDAY,
    THURSDAY,
    FRIDAY,
    SATURDAY,
    EVERY_MONTH,
    EVERY_YEAR,
}