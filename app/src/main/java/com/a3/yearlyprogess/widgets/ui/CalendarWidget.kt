package com.a3.yearlyprogess.widgets.ui

import android.appwidget.AppWidgetManager
import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.CalendarContract
import com.a3.yearlyprogess.widgets.manager.eventManager.model.Event
import com.a3.yearlyprogess.widgets.ui.EventWidget.Companion.eventWidgetPreview
import java.util.Calendar
import java.util.Date
import java.util.TimeZone

fun getNearestUpcomingEvent(contentResolver: ContentResolver): Event? {
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
  val selection = "${CalendarContract.Events.DTSTART} >= ?"
  val selectionArgs = arrayOf(now.toString())
  val sortOrder = "${CalendarContract.Events.DTSTART} ASC LIMIT 1"

  val cursor: Cursor? = contentResolver.query(uri, projection, selection, selectionArgs, sortOrder)

  cursor?.use {
    if (it.moveToFirst()) {
      val id = it.getLong(it.getColumnIndexOrThrow(CalendarContract.Events._ID))
      val title = it.getString(it.getColumnIndexOrThrow(CalendarContract.Events.TITLE))
      val description = it.getString(it.getColumnIndexOrThrow(CalendarContract.Events.DESCRIPTION))
      val startTime = it.getLong(it.getColumnIndexOrThrow(CalendarContract.Events.DTSTART))
      val location = it.getString(it.getColumnIndexOrThrow(CalendarContract.Events.EVENT_LOCATION))
      val dtEnd = it.getLong(it.getColumnIndexOrThrow(CalendarContract.Events.DTEND))
      return Event(
          id = 0,
          eventTitle = title,
          eventDescription = description,
          eventStartTime = Date(startTime),
          eventEndTime = Date(dtEnd),
      )
    }
  }
  return null
}

fun getTodayOrNearestEvent(contentResolver: ContentResolver): Event? {
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

  // Check for events happening today
  val selectionToday =
      "${CalendarContract.Events.DTSTART} >= ? AND ${CalendarContract.Events.DTSTART} < ?"
  val selectionArgsToday = arrayOf(startOfDay.toString(), endOfDay.toString())
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

      // Adjust start and end times to local time zone
      val startTimeLocal = adjustToLocalTimeZone(startTimeUtc)
      val endTimeLocal = adjustToLocalTimeZone(endTimeUtc)

      return Event(
          id = 0,
          eventTitle = title,
          eventDescription = description,
          eventStartTime = Date(startTimeLocal),
          eventEndTime = Date(endTimeLocal),
      )
    }
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

          // Adjust start and end times to local time zone
          val startTimeLocal = adjustToLocalTimeZone(startTimeUtc)
          val endTimeLocal = adjustToLocalTimeZone(endTimeUtc)

          return Event(
              id = 0,
              eventTitle = title,
              eventDescription = description,
              eventStartTime = Date(startTimeLocal),
              eventEndTime = Date(endTimeLocal),
          )
        }
      }

  return null
}

// Helper function to adjust time to the local time zone
private fun adjustToLocalTimeZone(utcTime: Long): Long {
  val timeZone = TimeZone.getDefault()
  val offset = timeZone.getOffset(utcTime)
  return utcTime - offset
}

class CalendarWidget : BaseWidget() {
  override fun updateWidget(
      context: Context,
      appWidgetManager: AppWidgetManager,
      appWidgetId: Int
  ) {
    val event = getTodayOrNearestEvent(context.contentResolver)
    appWidgetManager.updateAppWidget(appWidgetId, eventWidgetPreview(context, event!!))
  }
}
