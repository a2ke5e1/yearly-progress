package com.a3.yearlyprogess.widgets.ui

import android.Manifest
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.SizeF
import android.widget.RemoteViews
import com.a3.yearlyprogess.R
import com.a3.yearlyprogess.calculateProgress
import com.a3.yearlyprogess.calculateTimeLeft
import com.a3.yearlyprogess.widgets.manager.CalendarEventInfo.getCurrentEventOrUpcomingEvent
import com.a3.yearlyprogess.widgets.manager.CalendarEventInfo.getSelectedCalendarIds
import com.a3.yearlyprogess.widgets.manager.eventManager.model.Event
import com.a3.yearlyprogess.widgets.ui.util.styleFormatted
import com.a3.yearlyprogess.widgets.ui.util.toTimePeriodText
import java.util.Date
import kotlin.math.roundToInt

class CalendarWidget : BaseWidget() {
  override fun updateWidget(
      context: Context,
      appWidgetManager: AppWidgetManager,
      appWidgetId: Int
  ) {

    val smallView = RemoteViews(context.packageName, R.layout.calendar_widget_small_layout)
    val largeView = RemoteViews(context.packageName, R.layout.calendar_widget_layout)

    if (context.checkSelfPermission(Manifest.permission.READ_CALENDAR) ==
        PackageManager.PERMISSION_DENIED) {
      updateWidgetError(
          context,
          appWidgetId,
          "Error",
          "Calendar permission required",
          appWidgetManager,
          smallView,
          largeView,
      )
      return
    }

    val selectedCalendars = getSelectedCalendarIds(context)
    if (selectedCalendars.isNullOrEmpty()) {
      updateWidgetError(
          context,
          appWidgetId,
          "Error",
          "No calendars available",
          appWidgetManager,
          smallView,
          largeView,
      )
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
      updateWidgetError(
          context,
          appWidgetId,
          "Error",
          "No upcoming events",
          appWidgetManager,
          smallView,
          largeView,
      )
      return
    }

    val event =
        events
            .filter { event -> event.eventEndTime.time > System.currentTimeMillis() }
            .minBy { event -> event.eventStartTime }

    setupCalendarWidgetView(context, smallView, event)
    smallView.setViewVisibility(R.id.event_description, android.view.View.GONE)
    setupCalendarWidgetView(context, largeView, event)

    val view = mapRemoteView(context, appWidgetId, smallView, largeView)
    appWidgetManager.updateAppWidget(appWidgetId, view)
  }

  fun mapRemoteView(
      context: Context,
      widgetId: Int,
      smallView: RemoteViews,
      largeViews: RemoteViews
  ): RemoteViews {
    if (Build.VERSION.SDK_INT > 30) {
      val viewMapping: Map<SizeF, RemoteViews> =
          mapOf(
              SizeF(60f, 140f) to smallView,
              SizeF(130f, 140f) to largeViews,
          )
      return RemoteViews(viewMapping)
    }

    val option = AppWidgetManager.getInstance(context).getAppWidgetOptions(widgetId)
    val height = option.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT)
    val width = option.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH)

    if (height >= 100 && width >= 100) {
      return largeViews
    }

    return smallView
  }

  fun setupCalendarWidgetView(context: Context, view: RemoteViews, event: Event) {

    val progress = calculateProgress(context, event.eventStartTime.time, event.eventEndTime.time)
    view.setTextViewText(
        R.id.widgetProgress,
        progress.styleFormatted(2, cloverMode = true),
    )
    view.setTextViewText(R.id.event_title, event.eventTitle)
    view.setTextViewText(R.id.event_description, event.eventDescription)
    view.setTextViewText(
        R.id.event_duration,
        "${event.eventStartTime.formattedDateTime(context)} — ${
        event.eventEndTime.formattedDateTime(
          context
        )
      }")
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

    view.setViewVisibility(R.id.event_title, android.view.View.VISIBLE)
    if (event.eventDescription.isNotEmpty()) {
      view.setViewVisibility(R.id.event_description, android.view.View.VISIBLE)
    }
  }

  private fun emptyWidget(view: RemoteViews) {
    view.setTextViewText(R.id.event_status, "")
    view.setTextViewText(R.id.event_title, "")
    view.setTextViewText(R.id.event_description, "")
    view.setTextViewText(R.id.event_duration, "")
    view.setTextViewText(R.id.widgetProgress, "")
    view.setTextViewText(R.id.widgetDays, "")
    view.setProgressBar(R.id.widgetProgressBar, 100, 0, false)
    view.setViewVisibility(R.id.event_description, android.view.View.GONE)
    view.setViewVisibility(R.id.event_title, android.view.View.GONE)
    view.setViewVisibility(R.id.widgetProgressBar, android.view.View.GONE)
  }

  private fun updateWidgetError(
      context: Context,
      appWidgetId: Int,
      status: String,
      description: String,
      appWidgetManager: AppWidgetManager,
      smallView: RemoteViews,
      largeView: RemoteViews,
  ) {
    emptyWidget(smallView)
    emptyWidget(largeView)
    smallView.setTextViewText(R.id.event_status, status)
    largeView.setTextViewText(R.id.event_status, status)
    smallView.setViewVisibility(R.id.event_description, android.view.View.VISIBLE)
    largeView.setViewVisibility(R.id.event_description, android.view.View.VISIBLE)
    smallView.setTextViewText(R.id.event_description, description)
    largeView.setTextViewText(R.id.event_description, description)
    val view = mapRemoteView(context, appWidgetId, smallView, largeView)
    appWidgetManager.updateAppWidget(appWidgetId, view)
  }

  private fun Date.formattedDateTime(context: Context): String {
    return android.text.format.DateFormat.format(
            if (android.text.format.DateFormat.is24HourFormat(context)) "MMM dd, yyyy · HH:mm"
            else "MMM dd, yyyy · hh:mm a",
            this,
        )
        .toString()
        .uppercase()
  }
}
