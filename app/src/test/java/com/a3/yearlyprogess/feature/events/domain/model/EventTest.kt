package com.a3.yearlyprogess.feature.events.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Calendar
import java.util.Date

class EventTest {

    private fun createDate(year: Int, month: Int, day: Int, hour: Int, minute: Int): Long {
        return Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, day)
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    @Test
    fun `test NONE recurrence always returns original start and end`() {
        val start = createDate(2023, Calendar.OCTOBER, 1, 10, 0)
        val end = createDate(2023, Calendar.OCTOBER, 1, 11, 0)
        val event = Event(
            eventTitle = "Test",
            eventDescription = "",
            eventStartTime = Date(start),
            eventEndTime = Date(end),
            recurrenceType = RecurrenceType.NONE
        )

        // Before event ends
        val currentBefore = createDate(2023, Calendar.OCTOBER, 1, 9, 0)
        val result1 = event.nextStartAndEndTime(currentBefore)
        assertEquals(start, result1.first)
        assertEquals(end, result1.second)

        // After event ends
        val currentAfter = createDate(2023, Calendar.OCTOBER, 1, 12, 0)
        val result2 = event.nextStartAndEndTime(currentAfter)
        assertEquals(start, result2.first)
        assertEquals(end, result2.second)
    }

    @Test
    fun `test DAILY recurrence`() {
        val start = createDate(2023, Calendar.OCTOBER, 1, 10, 0)
        val end = createDate(2023, Calendar.OCTOBER, 1, 11, 0)
        val event = Event(
            eventTitle = "Test",
            eventDescription = "",
            eventStartTime = Date(start),
            eventEndTime = Date(end),
            recurrenceType = RecurrenceType.DAILY,
            recurrenceInterval = 2 // Every 2 days
        )

        // 3 days later, should be the 2nd occurrence (Oct 3)
        // If current is Oct 4, next valid occurrence that hasn't ended is Oct 5.
        val current = createDate(2023, Calendar.OCTOBER, 4, 9, 0)
        val expectedStart = createDate(2023, Calendar.OCTOBER, 5, 10, 0)
        val expectedEnd = createDate(2023, Calendar.OCTOBER, 5, 11, 0)
        
        val result = event.nextStartAndEndTime(current)
        assertEquals(expectedStart, result.first)
        assertEquals(expectedEnd, result.second)
    }

    @Test
    fun `test WEEKLY recurrence with specific days`() {
        // Oct 2, 2023 is Monday
        val start = createDate(2023, Calendar.OCTOBER, 2, 10, 0)
        val end = createDate(2023, Calendar.OCTOBER, 2, 11, 0)
        val event = Event(
            eventTitle = "Test",
            eventDescription = "",
            eventStartTime = Date(start),
            eventEndTime = Date(end),
            recurrenceType = RecurrenceType.WEEKLY,
            recurrenceInterval = 2, // Every 2 weeks
            repeatEventDays = listOf(RepeatDays.MONDAY, RepeatDays.WEDNESDAY)
        )

        // Occurrences: 
        // Wk 1: Mon Oct 2, Wed Oct 4
        // Wk 3: Mon Oct 16, Wed Oct 18

        // If current is Oct 3 (Tue), next is Oct 4 (Wed)
        var current = createDate(2023, Calendar.OCTOBER, 3, 10, 0)
        var expectedStart = createDate(2023, Calendar.OCTOBER, 4, 10, 0)
        var result = event.nextStartAndEndTime(current)
        assertEquals(expectedStart, result.first)

        // If current is Oct 5 (Thu), next is Oct 16 (Mon)
        current = createDate(2023, Calendar.OCTOBER, 5, 10, 0)
        expectedStart = createDate(2023, Calendar.OCTOBER, 16, 10, 0)
        result = event.nextStartAndEndTime(current)
        assertEquals(expectedStart, result.first)
    }

    @Test
    fun `test AFTER_OCCURRENCES end condition returns last valid occurrence`() {
        val start = createDate(2023, Calendar.OCTOBER, 1, 10, 0)
        val end = createDate(2023, Calendar.OCTOBER, 1, 11, 0)
        val event = Event(
            eventTitle = "Test",
            eventDescription = "",
            eventStartTime = Date(start),
            eventEndTime = Date(end),
            recurrenceType = RecurrenceType.DAILY,
            recurrenceInterval = 1,
            recurrenceEndType = RecurrenceEndType.AFTER_OCCURRENCES,
            recurrenceEndOccurrences = 3
        )

        // Occurrences: Oct 1, Oct 2, Oct 3
        // If current is Oct 4, it should return the last valid occurrence (Oct 3)
        val current = createDate(2023, Calendar.OCTOBER, 4, 10, 0)
        val expectedStart = createDate(2023, Calendar.OCTOBER, 3, 10, 0)
        val expectedEnd = createDate(2023, Calendar.OCTOBER, 3, 11, 0)

        val result = event.nextStartAndEndTime(current)
        assertEquals(expectedStart, result.first)
        assertEquals(expectedEnd, result.second)
    }

    @Test
    fun `test ON_DATE end condition returns last valid occurrence`() {
        val start = createDate(2023, Calendar.OCTOBER, 1, 10, 0)
        val end = createDate(2023, Calendar.OCTOBER, 1, 11, 0)
        val endDate = createDate(2023, Calendar.OCTOBER, 3, 23, 59) // End on Oct 3
        val event = Event(
            eventTitle = "Test",
            eventDescription = "",
            eventStartTime = Date(start),
            eventEndTime = Date(end),
            recurrenceType = RecurrenceType.DAILY,
            recurrenceInterval = 1,
            recurrenceEndType = RecurrenceEndType.ON_DATE,
            recurrenceEndDate = endDate
        )

        // Occurrences: Oct 1, Oct 2, Oct 3
        // If current is Oct 4, it should return the last valid occurrence (Oct 3)
        val current = createDate(2023, Calendar.OCTOBER, 4, 10, 0)
        val expectedStart = createDate(2023, Calendar.OCTOBER, 3, 10, 0)
        val expectedEnd = createDate(2023, Calendar.OCTOBER, 3, 11, 0)

        val result = event.nextStartAndEndTime(current)
        assertEquals(expectedStart, result.first)
        assertEquals(expectedEnd, result.second)
    }

    @Test
    fun `test MONTHLY recurrence`() {
        val start = createDate(2023, Calendar.OCTOBER, 15, 10, 0)
        val end = createDate(2023, Calendar.OCTOBER, 15, 11, 0)
        val event = Event(
            eventTitle = "Test",
            eventDescription = "",
            eventStartTime = Date(start),
            eventEndTime = Date(end),
            recurrenceType = RecurrenceType.MONTHLY,
            recurrenceInterval = 2 // Every 2 months
        )

        // Oct 15 -> Dec 15 -> Feb 15
        val current = createDate(2023, Calendar.NOVEMBER, 1, 10, 0)
        val expectedStart = createDate(2023, Calendar.DECEMBER, 15, 10, 0)
        
        val result = event.nextStartAndEndTime(current)
        assertEquals(expectedStart, result.first)
    }
}
