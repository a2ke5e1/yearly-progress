package com.a3.yearlyprogess.feature.events.domain.model

enum class RecurrenceType {
    NONE, DAILY, WEEKLY, MONTHLY, YEARLY
}

enum class RecurrenceEndType {
    NEVER, ON_DATE, AFTER_OCCURRENCES
}
