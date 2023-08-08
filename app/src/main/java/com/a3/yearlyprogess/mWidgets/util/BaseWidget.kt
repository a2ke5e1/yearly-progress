package com.a3.yearlyprogess.mWidgets.util

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.os.Bundle
import android.util.Log
import com.a3.yearlyprogess.helper.ProgressPercentage
import com.a3.yearlyprogess.manager.AlarmHandler

abstract class BaseWidget(private val widgetServiceType: Int) :
    AppWidgetProvider() {

    abstract fun updateWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    )

    override fun onEnabled(context: Context) {
        super.onEnabled(context)

        val alarmHandler = AlarmHandler(context, widgetServiceType)
        alarmHandler.setAlarmManager()
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

    override fun onDisabled(context: Context) {
        val alarmHandler = AlarmHandler(context, widgetServiceType)
        alarmHandler.cancelAlarmManager()
    }

    open fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        Log.d("updateAppWidget", widgetServiceType.toString())
        updateWidget(context, appWidgetManager, appWidgetId)

        val alarmHandler = AlarmHandler(context, widgetServiceType)
        alarmHandler.cancelAlarmManager()
        alarmHandler.setAlarmManager()
    }

}