package com.a3.yearlyprogess.widgets.ui

import android.Manifest
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.pm.PackageManager
import android.widget.RemoteViews
import com.a3.yearlyprogess.R
import com.a3.yearlyprogess.calculateProgress
import com.a3.yearlyprogess.widgets.manager.CalendarEventInfo.getCurrentEventOrUpcomingEvent
import com.a3.yearlyprogess.widgets.manager.CalendarEventInfo.getSelectedCalendarIds
import com.a3.yearlyprogess.widgets.ui.util.styleFormatted

class CalendarWidget : BaseWidget() {
  override fun updateWidget(
      context: Context,
      appWidgetManager: AppWidgetManager,
      appWidgetId: Int
  ) {

    val view = RemoteViews(context.packageName, R.layout.calendar_widget_layout)
    if (context.checkSelfPermission(Manifest.permission.READ_CALENDAR) ==
        PackageManager.PERMISSION_DENIED) {
      view.setTextViewText(R.id.text, "Calendar permission required")
      appWidgetManager.updateAppWidget(appWidgetId, view)
      return
    }

    var text = ""
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
    appWidgetManager.updateAppWidget(appWidgetId, view)
  }
}
