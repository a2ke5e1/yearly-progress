package com.a3.yearlyprogess

import com.a3.yearlyprogess.widgets.manager.eventManager.model.Event
import com.a3.yearlyprogess.widgets.manager.eventManager.model.RepeatDays
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Calendar
import java.util.Date

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class EventScheduleTest {
    @Test
    fun repeat_every_year_one_year() {
        val cal = Calendar.getInstance()
        cal.set(2022, 11, 1, 0, 0, 0)
        var currentTime = cal.timeInMillis

        cal.set(2022, 9, 1, 0, 0, 0)
        val start = cal.timeInMillis
        cal.set(2022, 9, 1, 1, 0, 0)
        val end = cal.timeInMillis

        cal.set(2023, 9, 1, 0, 0, 0)
        val nextStart = cal.timeInMillis
        cal.set(2023, 9, 1, 1, 0, 0)
        val nextEnd = cal.timeInMillis

        val event = Event(
            1,
            "Event 1",
            "Event 1 Description",
            false,
            Date(start),
            Date(end),
            listOf(RepeatDays.EVERY_YEAR)
        )

        val nextSchedule = event.nextStartAndEndTime(
            currentTime = currentTime
        )

        assertEquals(Date(nextStart), Date(nextSchedule.first))
        assertEquals(Date(nextEnd), Date(nextSchedule.second))
    }

    @Test
    fun repeat_every_year_five_years_case_1() {
        val cal = Calendar.getInstance()
        cal.set(2027, 4, 1, 0, 0, 0)
        var currentTime = cal.timeInMillis

        cal.set(2022, 9, 1, 0, 0, 0)
        val start = cal.timeInMillis
        cal.set(2022, 9, 1, 1, 0, 0)
        val end = cal.timeInMillis

        cal.set(2027, 9, 1, 0, 0, 0)
        val nextStart5 = cal.timeInMillis
        cal.set(2027, 9, 1, 1, 0, 0)
        val nextEnd5 = cal.timeInMillis

        val event = Event(
            1,
            "Event 1",
            "Event 1 Description",
            false,
            Date(start),
            Date(end),
            listOf(RepeatDays.EVERY_YEAR)
        )

        val nextSchedule5 = event.nextStartAndEndTime(
            currentTime = currentTime
        )

        assertEquals(Date(nextStart5), Date(nextSchedule5.first))
        assertEquals(Date(nextEnd5), Date(nextSchedule5.second))
    }

    @Test
    fun repeat_every_year_five_years_case_2() {
        val cal = Calendar.getInstance()
        cal.set(2025, 3, 1, 0, 0, 0)
        var currentTime = cal.timeInMillis

        cal.set(2022, 9, 1, 0, 0, 0)
        val start = cal.timeInMillis
        cal.set(2022, 9, 1, 1, 0, 0)
        val end = cal.timeInMillis

        cal.set(2025, 9, 1, 0, 0, 0)
        val nextStart6 = cal.timeInMillis
        cal.set(2025, 9, 1, 1, 0, 0)
        val nextEnd6 = cal.timeInMillis

        val event = Event(
            1,
            "Event 1",
            "Event 1 Description",
            false,
            Date(start),
            Date(end),
            listOf(RepeatDays.EVERY_YEAR)
        )

        val nextSchedule6 = event.nextStartAndEndTime(
            currentTime = currentTime
        )

        assertEquals(Date(nextStart6), Date(nextSchedule6.first))
        assertEquals(Date(nextEnd6), Date(nextSchedule6.second))
    }


    @Test
    fun repeat_monthly_test1() {
        val cal = Calendar.getInstance()
        cal.set(2022, 10, 2, 0, 0, 0)
        var currentTime = cal.timeInMillis

        cal.set(2022, 9, 1, 0, 0, 0)
        val start = cal.timeInMillis

        cal.set(2022, 9, 1, 1, 0, 0)
        val end = cal.timeInMillis

        cal.set(2022, 11, 1, 0, 0, 0)
        val nextStart = cal.timeInMillis

        cal.set(2022, 11, 1, 1, 0, 0)
        val nextEnd = cal.timeInMillis

        val event = Event(
            1,
            "Event 1",
            "Event 1 Description",
            false,
            Date(start),
            Date(end),
            listOf(RepeatDays.EVERY_MONTH)
        )

        val nextSchedule = event.nextStartAndEndTime(
            currentTime = currentTime
        )

        assertEquals(Date(nextStart), Date(nextSchedule.first))
        assertEquals(Date(nextEnd), Date(nextSchedule.second))

    }

    @Test
    fun repeat_week_test1() {
        val cal = Calendar.getInstance()
        cal.set(2024, 9, 28, 14, 0, 0)
        var currentTime = cal.timeInMillis

        cal.set(2024, 9, 15, 0, 0, 0)
        val start = cal.timeInMillis

        cal.set(2024, 9, 15, 23, 59, 0)
        val end = cal.timeInMillis

        cal.set(2024, 9, 30, 0, 0, 0)
        val nextStart = cal.timeInMillis

        cal.set(2024, 9, 30, 23, 59, 0)
        val nextEnd = cal.timeInMillis

        val event = Event(
            1,
            "Event 1",
            "Event 1 Description",
            false,
            Date(start),
            Date(end),
            listOf(RepeatDays.WEDNESDAY)
        )

        val nextSchedule = event.nextStartAndEndTime(
            currentTime = currentTime
        )

        assertEquals(Date(nextStart), Date(nextSchedule.first))
        assertEquals(Date(nextEnd), Date(nextSchedule.second))

    }

    @Test
    fun repeat_week_test2() {
        val cal = Calendar.getInstance()
        cal.set(2024, 9, 31, 14, 0, 0)
        var currentTime = cal.timeInMillis

        cal.set(2024, 9, 15, 0, 0, 0)
        val start = cal.timeInMillis

        cal.set(2024, 9, 15, 23, 59, 0)
        val end = cal.timeInMillis

        cal.set(2024, 10, 1, 0, 0, 0)
        val nextStart = cal.timeInMillis

        cal.set(2024, 10, 1, 23, 59, 0)
        val nextEnd = cal.timeInMillis

        val event = Event(
            1,
            "Event 1",
            "Event 1 Description",
            false,
            Date(start),
            Date(end),
            listOf(RepeatDays.WEDNESDAY, RepeatDays.FRIDAY)
        )

        val nextSchedule = event.nextStartAndEndTime(
            currentTime = currentTime
        )

        assertEquals(Date(nextStart), Date(nextSchedule.first))
        assertEquals(Date(nextEnd), Date(nextSchedule.second))

    }


}