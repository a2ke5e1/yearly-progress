package com.a3.yearlyprogess.widgets.ui

import android.appwidget.AppWidgetManager
import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.CalendarContract
import com.a3.yearlyprogess.widgets.manager.eventManager.model.Event
import com.a3.yearlyprogess.widgets.ui.EventWidget.Companion.eventWidgetPreview
import java.util.Calendar
import java.util.Date

fun getEventsFromAllCalendars(contentResolver: ContentResolver): List<Event> {
  val events = mutableListOf<Event>()

  // Query to get all calendar IDs
  val calendarUri: Uri = CalendarContract.Calendars.CONTENT_URI
  val calendarProjection =
      arrayOf(CalendarContract.Calendars._ID, CalendarContract.Calendars.CALENDAR_DISPLAY_NAME)

  contentResolver.query(calendarUri, calendarProjection, null, null, null)?.use { cursor ->
    while (cursor.moveToNext()) {
      val calendarId = cursor.getLong(cursor.getColumnIndexOrThrow(CalendarContract.Calendars._ID))
      val calendarName =
          cursor.getString(
              cursor.getColumnIndexOrThrow(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME))

      // Fetch today's or nearest event for this calendar
      val event = getTodayOrNearestEventFromCalendar(contentResolver, calendarId)
      if (event != null) {
        events.add(event)
        println("Calendar: $calendarName, Event: ${event.eventTitle}")
      } else {
        println("Calendar: $calendarName has no upcoming events.")
      }
    }
  }

  return events
}

// Helper function to fetch today's or nearest event from a specific calendar
private fun getTodayOrNearestEventFromCalendar(
    contentResolver: ContentResolver,
    calendarId: Long
): Event? {
  val uri: Uri = CalendarContract.Events.CONTENT_URI
  val projection =
      arrayOf(
          CalendarContract.Events._ID,
          CalendarContract.Events.TITLE,
          CalendarContract.Events.DESCRIPTION,
          CalendarContract.Events.DTSTART,
          CalendarContract.Events.EVENT_LOCATION,
          CalendarContract.Events.DTEND)

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

  // Check for events happening today in this calendar
  val selectionToday =
      "${CalendarContract.Events.CALENDAR_ID} = ? AND ${CalendarContract.Events.DTSTART} >= ? AND ${CalendarContract.Events.DTSTART} < ?"
  val selectionArgsToday =
      arrayOf(calendarId.toString(), startOfDay.toString(), endOfDay.toString())
  val sortOrder = "${CalendarContract.Events.DTSTART} ASC LIMIT 1"

  contentResolver.query(uri, projection, selectionToday, selectionArgsToday, sortOrder)?.use {
      cursor ->
    if (cursor.moveToFirst()) {
      val id = cursor.getLong(cursor.getColumnIndexOrThrow(CalendarContract.Events._ID))
      val title = cursor.getString(cursor.getColumnIndexOrThrow(CalendarContract.Events.TITLE))
      val description =
          cursor.getString(cursor.getColumnIndexOrThrow(CalendarContract.Events.DESCRIPTION))
      val startTimeUtc =
          cursor.getLong(cursor.getColumnIndexOrThrow(CalendarContract.Events.DTSTART))
      val location =
          cursor.getString(cursor.getColumnIndexOrThrow(CalendarContract.Events.EVENT_LOCATION))
      val endTimeUtc = cursor.getLong(cursor.getColumnIndexOrThrow(CalendarContract.Events.DTEND))

      return Event(
          id = 0,
          eventTitle = title,
          eventDescription = description,
          eventStartTime = Date(startTimeUtc),
          eventEndTime = Date(endTimeUtc),
      )
    }
  }

  // If no events today, get the nearest upcoming event for this calendar
  val selectionUpcoming =
      "${CalendarContract.Events.CALENDAR_ID} = ? AND ${CalendarContract.Events.DTSTART} >= ?"
  val selectionArgsUpcoming = arrayOf(calendarId.toString(), now.toString())

  contentResolver
      .query(uri, projection, selectionUpcoming, selectionArgsUpcoming, sortOrder)
      ?.use { cursor ->
        if (cursor.moveToFirst()) {
          val id = cursor.getLong(cursor.getColumnIndexOrThrow(CalendarContract.Events._ID))
          val title = cursor.getString(cursor.getColumnIndexOrThrow(CalendarContract.Events.TITLE))
          val description =
              cursor.getString(cursor.getColumnIndexOrThrow(CalendarContract.Events.DESCRIPTION))
          val startTimeUtc =
              cursor.getLong(cursor.getColumnIndexOrThrow(CalendarContract.Events.DTSTART))
          val location =
              cursor.getString(cursor.getColumnIndexOrThrow(CalendarContract.Events.EVENT_LOCATION))
          val endTimeUtc =
              cursor.getLong(cursor.getColumnIndexOrThrow(CalendarContract.Events.DTEND))

          return Event(
              id = 0,
              eventTitle = title,
              eventDescription = description,
              eventStartTime = Date(startTimeUtc),
              eventEndTime = Date(endTimeUtc),
          )
        }
      }

  return null
}

fun getTodayOrNearestEvents(contentResolver: ContentResolver): List<Event> {
  val uri: Uri = CalendarContract.Events.CONTENT_URI
  val projection =
      arrayOf(
          CalendarContract.Events._ID,
          CalendarContract.Events.TITLE,
          CalendarContract.Events.DESCRIPTION,
          CalendarContract.Events.DTSTART,
          CalendarContract.Events.EVENT_LOCATION,
          CalendarContract.Events.DTEND)

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

  // Check for events happening today
  val selectionToday =
      "${CalendarContract.Events.DTSTART} >= ? AND ${CalendarContract.Events.DTSTART} < ?"
  val selectionArgsToday = arrayOf(startOfDay.toString(), endOfDay.toString())
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

  // If no events today, get the nearest upcoming event
  val selectionUpcoming = "${CalendarContract.Events.DTSTART} >= ?"
  val selectionArgsUpcoming = arrayOf(now.toString())

  contentResolver
      .query(uri, projection, selectionUpcoming, selectionArgsUpcoming, sortOrder)
      ?.use { cursor ->
        if (cursor.moveToFirst()) {
          val id = cursor.getLong(cursor.getColumnIndexOrThrow(CalendarContract.Events._ID))
          val title = cursor.getString(cursor.getColumnIndexOrThrow(CalendarContract.Events.TITLE))
          val description =
              cursor.getString(cursor.getColumnIndexOrThrow(CalendarContract.Events.DESCRIPTION))
          val startTimeUtc =
              cursor.getLong(cursor.getColumnIndexOrThrow(CalendarContract.Events.DTSTART))
          val location =
              cursor.getString(cursor.getColumnIndexOrThrow(CalendarContract.Events.EVENT_LOCATION))
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

fun getCurrentEventOrUpcomingEvent(contentResolver: ContentResolver): Event? {
  val events = getTodayOrNearestEvents(contentResolver)
  return events
      .filter { event -> event.eventEndTime.time > System.currentTimeMillis() }
      .minByOrNull { event -> event.eventStartTime }
}

class CalendarWidget : BaseWidget() {
  override fun updateWidget(
      context: Context,
      appWidgetManager: AppWidgetManager,
      appWidgetId: Int
  ) {
    appWidgetManager.updateAppWidget(
        appWidgetId,
        eventWidgetPreview(context, getCurrentEventOrUpcomingEvent(context.contentResolver)!!))
  }
}
