package com.a3.yearlyprogess.widgets.ui

import android.appwidget.AppWidgetManager
import android.content.Context
import com.a3.yearlyprogess.widgets.manager.CalendarEventInfo.getCurrentEventOrUpcomingEvent
import com.a3.yearlyprogess.widgets.ui.EventWidget.Companion.eventWidgetPreview

class CalendarWidget : BaseWidget() {
  override fun updateWidget(
      context: Context,
      appWidgetManager: AppWidgetManager,
      appWidgetId: Int
  ) {
    appWidgetManager.updateAppWidget(
        appWidgetId,
        eventWidgetPreview(context, getCurrentEventOrUpcomingEvent(context.contentResolver, 20L)!!))
  }
}
