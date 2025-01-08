package com.a3.yearlyprogess.widgets.ui

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.os.Bundle
import android.util.Log
import com.a3.yearlyprogess.widgets.manager.updateManager.WakeLocker
import com.a3.yearlyprogess.widgets.manager.updateManager.WidgetUpdateAlarmHandler

abstract class BaseWidget : AppWidgetProvider() {
  abstract fun updateWidget(
      context: Context,
      appWidgetManager: AppWidgetManager,
      appWidgetId: Int,
  )

  override fun onEnabled(context: Context) {
    super.onEnabled(context)

    val widgetUpdateAlarmHandler = WidgetUpdateAlarmHandler(context)
    widgetUpdateAlarmHandler.setAlarmManager()
  }

  override fun onUpdate(
      context: Context,
      appWidgetManager: AppWidgetManager,
      appWidgetIds: IntArray,
  ) {
    // There may be multiple widgets active, so update all of them
    for (appWidgetId in appWidgetIds) {
      updateAppWidget(context, appWidgetManager, appWidgetId)
    }
  }

  override fun onAppWidgetOptionsChanged(
      context: Context,
      appWidgetManager: AppWidgetManager,
      appWidgetId: Int,
      newOptions: Bundle?,
  ) {
    updateAppWidget(context, appWidgetManager, appWidgetId)
  }

  // Checks if any other widgets are active.
  // If not, cancels the alarm.
  override fun onDisabled(context: Context) {
    val widgetUpdateAlarmHandler = WidgetUpdateAlarmHandler(context)

    val widgetIntentsAndComponents =
        arrayOf(
            DayWidget::class.java,
            WeekWidget::class.java,
            MonthWidget::class.java,
            YearWidget::class.java,
            AllInWidget::class.java,
            EventWidget::class.java,
        )

    var totalWidgets = 0
    widgetIntentsAndComponents.forEach {
      val ids = AppWidgetManager.getInstance(context).getAppWidgetIds(ComponentName(context, it))
      totalWidgets += ids.size
    }

    if (totalWidgets > 0) {
      Log.d(TAG, "canceling alarm shutdown, $totalWidgets still active")
      WakeLocker.release()
      return
    }

    widgetUpdateAlarmHandler.cancelAlarmManager()
    WakeLocker.release()
  }

  private fun updateAppWidget(
      context: Context,
      appWidgetManager: AppWidgetManager,
      appWidgetId: Int,
  ) {
    // Log.d("updateAppWidget", widgetServiceType.toString())
    updateWidget(context, appWidgetManager, appWidgetId)

    val widgetUpdateAlarmHandler = WidgetUpdateAlarmHandler(context)
    WakeLocker.acquire(context)
    widgetUpdateAlarmHandler.cancelAlarmManager()
    widgetUpdateAlarmHandler.setAlarmManager()
  }

  companion object {
    private const val TAG = "BaseWidget"
  }
}
