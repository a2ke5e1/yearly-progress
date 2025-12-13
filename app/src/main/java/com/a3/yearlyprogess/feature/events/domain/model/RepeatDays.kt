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


enum class Weekday(
    val label: String,
    val repeatDay: RepeatDays
) {
    SUNDAY("S", RepeatDays.SUNDAY),
    MONDAY("M", RepeatDays.MONDAY),
    TUESDAY("T", RepeatDays.TUESDAY),
    WEDNESDAY("W", RepeatDays.WEDNESDAY),
    THURSDAY("T", RepeatDays.THURSDAY),
    FRIDAY("F", RepeatDays.FRIDAY),
    SATURDAY("S", RepeatDays.SATURDAY);
}
