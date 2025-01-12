package com.a3.yearlyprogess.widgets.ui

import android.Manifest
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.pm.PackageManager
import android.widget.RemoteViews
import com.a3.yearlyprogess.R
import com.a3.yearlyprogess.calculateProgress
import com.a3.yearlyprogess.calculateTimeLeft
import com.a3.yearlyprogess.widgets.manager.CalendarEventInfo.getCurrentEventOrUpcomingEvent
import com.a3.yearlyprogess.widgets.manager.CalendarEventInfo.getSelectedCalendarIds
import com.a3.yearlyprogess.widgets.manager.eventManager.model.Event
import com.a3.yearlyprogess.widgets.ui.util.styleFormatted
import com.a3.yearlyprogess.widgets.ui.util.toTimePeriodText
import java.text.DateFormat
import java.util.Date
import kotlin.math.roundToInt

class CalendarWidget : BaseWidget() {
  override fun updateWidget(
      context: Context,
      appWidgetManager: AppWidgetManager,
      appWidgetId: Int
  ) {

    val view = RemoteViews(context.packageName, R.layout.calendar_widget_layout)
    if (context.checkSelfPermission(Manifest.permission.READ_CALENDAR) ==
        PackageManager.PERMISSION_DENIED) {
      emptyWidget(view)
      view.setTextViewText(R.id.event_title, "Calendar permission required")
      appWidgetManager.updateAppWidget(appWidgetId, view)
      return
    }

    /*var text = ""
    val selectedCalendars = getSelectedCalendarIds(context)
    if (selectedCalendars.isNullOrEmpty()) {
      text = "No calendars selected"
    } else {
      text = "Selected calendars: ${selectedCalendars.joinToString(", ")}"
    }

    if (selectedCalendars != null) {
      for (calendarId in selectedCalendars) {
        text += "\n\n"
        val event = getCurrentEventOrUpcomingEvent(context.contentResolver, calendarId)
        text += "Calendar: $calendarId\n"
        if (event != null) {
          text += event.eventTitle
          text += "\n"
          text += event.eventDescription.take(20)
          text += "\n"
          text += event.eventStartTime
          text += "\n"
          text += event.eventEndTime
          text += "\n"
          text += calculateProgress(context, event.eventStartTime.time, event.eventEndTime.time).styleFormatted(2)
        } else {
          text += "No upcoming events"
        }
      }
    }

    view.setTextViewText(R.id.text, text)
*/
    val selectedCalendars = getSelectedCalendarIds(context)
    if (selectedCalendars.isNullOrEmpty()) {
      emptyWidget(view)
      view.setTextViewText(R.id.event_title, "No calendars selected")
      appWidgetManager.updateAppWidget(appWidgetId, view)
      return
    }

    val events = mutableListOf<Event>()
    for (calendarId in selectedCalendars) {
      val event = getCurrentEventOrUpcomingEvent(context.contentResolver, calendarId)
      if (event != null) {
        events.add(event)
      }
    }

    if (events.isEmpty()) {
      emptyWidget(view)
      view.setTextViewText(R.id.event_title, "No upcoming events")
      appWidgetManager.updateAppWidget(appWidgetId, view)
      return
    }

    val event = events
      .filter { event -> event.eventEndTime.time > System.currentTimeMillis() }
      .minBy { event -> event.eventStartTime }

    val progress = calculateProgress(context, event.eventStartTime.time, event.eventEndTime.time)
    view.setTextViewText(
      R.id.widgetProgress,
      progress.styleFormatted(2, cloverMode = true),
    )
    view.setTextViewText(R.id.event_title, event.eventTitle)
    view.setTextViewText(R.id.event_description, event.eventDescription)
    view.setTextViewText(R.id.event_duration, "${event.eventStartTime.formattedDateTime(context)} — ${event.eventEndTime.formattedDateTime(context)}")
    view.setProgressBar(R.id.widgetProgressBar, 100, progress.roundToInt(), false)


    val widgetDays =
      if (System.currentTimeMillis() < event.eventStartTime.time) {
        "in " + (event.eventStartTime.time - System.currentTimeMillis()).toTimePeriodText()
      } else {
        calculateTimeLeft(event.eventEndTime.time).toTimePeriodText() + " left"
      }
    view.setTextViewText(R.id.widgetDays, widgetDays)

    if (System.currentTimeMillis() in event.eventStartTime.time..event.eventEndTime.time) {
      view.setViewVisibility(R.id.widgetProgressBar, android.view.View.VISIBLE)
      view.setTextViewText(R.id.event_status, "ongoing")
    } else {
      view.setViewVisibility(R.id.widgetProgressBar, android.view.View.GONE)
      view.setTextViewText(R.id.event_status, "upcoming")
    }



    if (event.eventDescription.isEmpty()) {
      view.setViewVisibility(R.id.event_description, android.view.View.GONE)
    }

    appWidgetManager.updateAppWidget(appWidgetId, view)
  }

  private fun emptyWidget(view: RemoteViews) {
    view.setTextViewText(R.id.event_status, "")
    view.setTextViewText(R.id.event_title, "")
    view.setTextViewText(R.id.event_description, "")
    view.setTextViewText(R.id.event_duration, "")
    view.setTextViewText(R.id.widgetProgress, "")
    view.setTextViewText(R.id.widgetDays, "")
    view.setViewVisibility(R.id.widgetProgressBar, android.view.View.GONE)
    view.setViewVisibility(R.id.event_description, android.view.View.VISIBLE)
  }

  private fun Date.formattedDateTime(context: Context): String {
    return  android.text.format.DateFormat.format(
      if (android.text.format.DateFormat.is24HourFormat(context)) "MMM dd, yyyy · HH:mm" else "MMM dd, yyyy · hh:mm a",
      this,
    )
      .toString()
      .uppercase()
  }

}
