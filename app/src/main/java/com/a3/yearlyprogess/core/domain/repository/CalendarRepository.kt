package com.a3.yearlyprogess.core.domain.repository

import com.a3.yearlyprogess.core.domain.model.CalendarInfo

interface CalendarRepository {
    suspend fun getAvailableCalendars(): Result<List<CalendarInfo>>
    suspend fun hasCalendarPermission(): Boolean
    suspend fun getSelectedCalendarDetails(calendarIds: Set<Long>): Result<List<CalendarInfo>>

}