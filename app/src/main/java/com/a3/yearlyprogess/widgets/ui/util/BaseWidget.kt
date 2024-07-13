package com.a3.yearlyprogess.widgets.ui.util

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.preference.PreferenceManager
import com.a3.yearlyprogess.R
import com.a3.yearlyprogess.widgets.manager.updateManager.WidgetUpdateAlarmHandler
import com.a3.yearlyprogess.widgets.manager.updateManager.WakeLocker
import com.a3.yearlyprogess.widgets.manager.updateManager.services.useForegroundService
import com.a3.yearlyprogess.widgets.ui.AllInWidget
import com.a3.yearlyprogess.widgets.ui.DayWidget
import com.a3.yearlyprogess.widgets.ui.EventWidget
import com.a3.yearlyprogess.widgets.ui.MonthWidget
import com.a3.yearlyprogess.widgets.ui.WeekWidget
import com.a3.yearlyprogess.widgets.ui.YearWidget

abstract class BaseWidget :
    AppWidgetProvider() {


    abstract fun updateWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    )

    override fun onEnabled(context: Context) {
        super.onEnabled(context)

        if (useForegroundService(context)) return

        val widgetUpdateAlarmHandler = WidgetUpdateAlarmHandler(context)
        widgetUpdateAlarmHandler.setAlarmManager()
    }

    override fun onUpdate(
        context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray
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
        newOptions: Bundle?
    ) {
        updateAppWidget(context, appWidgetManager, appWidgetId)
    }


    // Checks if any other widgets are active.
    // If not, cancels the alarm.
    override fun onDisabled(context: Context) {

        if (useForegroundService(context)) return


        val widgetUpdateAlarmHandler = WidgetUpdateAlarmHandler(context)

        val widgetIntentsAndComponents = arrayOf(
            DayWidget::class.java,
            WeekWidget::class.java,
            MonthWidget::class.java,
            YearWidget::class.java,
            AllInWidget::class.java,
            EventWidget::class.java
        )

        var totalWidgets = 0
        widgetIntentsAndComponents.forEach {
            val ids = AppWidgetManager.getInstance(context)
                .getAppWidgetIds(ComponentName(context, it))
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
        appWidgetId: Int
    ) {
        // Log.d("updateAppWidget", widgetServiceType.toString())
        updateWidget(context, appWidgetManager, appWidgetId)

        /*
        Checks if user want to use foreground service
        to update widgets.

        If does then skips the alarm manager system to update the widget.
        */
        if (useForegroundService(context)) return


        val widgetUpdateAlarmHandler = WidgetUpdateAlarmHandler(context)
        WakeLocker.acquire(context)
        widgetUpdateAlarmHandler.cancelAlarmManager()
        widgetUpdateAlarmHandler.setAlarmManager()
    }

    companion object {
        private const val TAG = "BaseWidget"
    }
}