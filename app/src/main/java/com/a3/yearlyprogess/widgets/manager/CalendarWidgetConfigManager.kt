package com.a3.yearlyprogess.widgets.manager

import android.content.ContentResolver
import android.net.Uri
import android.os.Bundle
import android.provider.CalendarContract
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.a3.yearlyprogess.databinding.ActivityCalendarWidgetConfigManagerBinding
import com.a3.yearlyprogess.widgets.manager.CalendarEventInfo.getCalendarsDetails
import com.a3.yearlyprogess.widgets.manager.CalendarEventInfo.getCurrentEventOrUpcomingEvent
import com.a3.yearlyprogess.widgets.manager.eventManager.model.Event
import java.util.Calendar
import java.util.Date

object CalendarEventInfo {
  fun getTodayOrNearestEvents(
      contentResolver: ContentResolver,
      selectedCalendarId: Long
  ): List<Event> {
    val uri: Uri = CalendarContract.Events.CONTENT_URI
    val projection =
        arrayOf(
            CalendarContract.Events._ID,
            CalendarContract.Events.TITLE,
            CalendarContract.Events.DESCRIPTION,
            CalendarContract.Events.DTSTART,
            CalendarContract.Events.EVENT_LOCATION,
            CalendarContract.Events.DTEND,
            CalendarContract.Events.CALENDAR_ID)

    val now = System.currentTimeMillis()
    val calendar =
        Calendar.getInstance().apply {
          timeInMillis = now
          set(Calendar.HOUR_OF_DAY, 0)
          set(Calendar.MINUTE, 0)
          set(Calendar.SECOND, 0)
          set(Calendar.MILLISECOND, 0)
        }
    val startOfDay = calendar.timeInMillis
    calendar.add(Calendar.DAY_OF_MONTH, 1)
    val endOfDay = calendar.timeInMillis

    val events = mutableListOf<Event>()

    // Check for events happening today in the selected calendar
    val selectionToday =
        "${CalendarContract.Events.CALENDAR_ID} = ? AND ${CalendarContract.Events.DTSTART} >= ? AND ${CalendarContract.Events.DTSTART} < ?"
    val selectionArgsToday =
        arrayOf(selectedCalendarId.toString(), startOfDay.toString(), endOfDay.toString())
    val sortOrder = "${CalendarContract.Events.DTSTART} ASC"

    contentResolver.query(uri, projection, selectionToday, selectionArgsToday, sortOrder)?.use {
        cursor ->
      while (cursor.moveToNext()) {
        val id = cursor.getLong(cursor.getColumnIndexOrThrow(CalendarContract.Events._ID))
        val title = cursor.getString(cursor.getColumnIndexOrThrow(CalendarContract.Events.TITLE))
        val description =
            cursor.getString(cursor.getColumnIndexOrThrow(CalendarContract.Events.DESCRIPTION))
        val startTimeUtc =
            cursor.getLong(cursor.getColumnIndexOrThrow(CalendarContract.Events.DTSTART))
        val location =
            cursor.getString(cursor.getColumnIndexOrThrow(CalendarContract.Events.EVENT_LOCATION))
        val endTimeUtc = cursor.getLong(cursor.getColumnIndexOrThrow(CalendarContract.Events.DTEND))

        events.add(
            Event(
                id = 0,
                eventTitle = title,
                eventDescription = description,
                eventStartTime = Date(startTimeUtc),
                eventEndTime = Date(endTimeUtc)))
      }
    }

    if (events.isNotEmpty()) {
      return events // Return events for today
    }

    // If no events today, get the nearest upcoming event in the selected calendar
    val selectionUpcoming =
        "${CalendarContract.Events.CALENDAR_ID} = ? AND ${CalendarContract.Events.DTSTART} >= ?"
    val selectionArgsUpcoming = arrayOf(selectedCalendarId.toString(), now.toString())

    contentResolver
        .query(uri, projection, selectionUpcoming, selectionArgsUpcoming, sortOrder)
        ?.use { cursor ->
          if (cursor.moveToFirst()) {
            val id = cursor.getLong(cursor.getColumnIndexOrThrow(CalendarContract.Events._ID))
            val title =
                cursor.getString(cursor.getColumnIndexOrThrow(CalendarContract.Events.TITLE))
            val description =
                cursor.getString(cursor.getColumnIndexOrThrow(CalendarContract.Events.DESCRIPTION))
            val startTimeUtc =
                cursor.getLong(cursor.getColumnIndexOrThrow(CalendarContract.Events.DTSTART))
            val location =
                cursor.getString(
                    cursor.getColumnIndexOrThrow(CalendarContract.Events.EVENT_LOCATION))
            val endTimeUtc =
                cursor.getLong(cursor.getColumnIndexOrThrow(CalendarContract.Events.DTEND))

            events.add(
                Event(
                    id = 0,
                    eventTitle = title,
                    eventDescription = description,
                    eventStartTime = Date(startTimeUtc),
                    eventEndTime = Date(endTimeUtc)))
          }
        }

    return events // Return the nearest upcoming event (if any) as a single-item list
  }

  data class CalendarInfo(val id: Long, val displayName: String, val accountName: String)

  fun getCalendarsDetails(contentResolver: ContentResolver): List<CalendarInfo> {
    val uri: Uri = CalendarContract.Calendars.CONTENT_URI
    val projection =
        arrayOf(
            CalendarContract.Calendars._ID,
            CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,
            CalendarContract.Calendars.ACCOUNT_NAME)

    val calendars = mutableListOf<CalendarInfo>()

    contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
      while (cursor.moveToNext()) {
        val id = cursor.getLong(cursor.getColumnIndexOrThrow(CalendarContract.Calendars._ID))
        val displayName =
            cursor.getString(
                cursor.getColumnIndexOrThrow(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME))
        val accountName =
            cursor.getString(cursor.getColumnIndexOrThrow(CalendarContract.Calendars.ACCOUNT_NAME))

        calendars.add(CalendarInfo(id = id, displayName = displayName, accountName = accountName))
      }
    }

    return calendars
  }

  fun getCurrentEventOrUpcomingEvent(contentResolver: ContentResolver, calendarId: Long): Event? {
    val events = getTodayOrNearestEvents(contentResolver, calendarId)
    return events
        .filter { event -> event.eventEndTime.time > System.currentTimeMillis() }
        .minByOrNull { event -> event.eventStartTime }
  }
}


class CalendarWidgetConfigManager : AppCompatActivity() {
  private var _binding: ActivityCalendarWidgetConfigManagerBinding? = null
  private val binding
    get() = _binding!!

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    _binding = ActivityCalendarWidgetConfigManagerBinding.inflate(layoutInflater)
    setContentView(binding.root)
    ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
      val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
      v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
      insets
    }

    val calendars = getCalendarsDetails(this.contentResolver)
    println("Available Calendars:")
    for (calendar in calendars) {
      println("ID: ${calendar.id}, Name: ${calendar.displayName}, Account: ${calendar.accountName} ${getCurrentEventOrUpcomingEvent(this.contentResolver, calendar.id)}")
    }


  }
}
