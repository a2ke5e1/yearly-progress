package com.a3.yearlyprogess.mwidgets

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.os.Bundle
import android.widget.RemoteViews
import com.a3.yearlyprogess.R
import com.a3.yearlyprogess.helper.ProgressPercentage
import com.a3.yearlyprogess.manager.AlarmHandler
import kotlin.math.roundToInt

/**
 * Implementation of App Widget functionality.
 */
class AllInWidget : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
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
        // Enter relevant functionality for when the last widget is disabled
        val alarmHandler = AlarmHandler(context, AlarmHandler.ALL_IN_WIDGET_SERVICE)
        alarmHandler.cancelAlarmManager()
    }
}

internal fun updateAppWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int
) {
    val widgetText = context.getString(R.string.appwidget_text)
    // Construct the RemoteViews object
    val views = RemoteViews(context.packageName, R.layout.all_in_widget)

    val progressPercentage = ProgressPercentage()
    val dayProgress = progressPercentage.getPercent(ProgressPercentage.DAY).roundToInt()
    val weekProgress = progressPercentage.getPercent(ProgressPercentage.WEEK).roundToInt()
    val monthProgress = progressPercentage.getPercent(ProgressPercentage.MONTH).roundToInt()
    val yearProgress = progressPercentage.getPercent(ProgressPercentage.YEAR).roundToInt()


   
    views.setTextViewText(R.id.progressTextDay, "${dayProgress}%")
    views.setTextViewText(R.id.progressTextWeek, "$weekProgress%")
    views.setTextViewText(R.id.progressTextMonth, "$monthProgress%")
    views.setTextViewText(R.id.progressTextYear, "$yearProgress%")

    views.setProgressBar(R.id.progressBarDay, 100, dayProgress, false)
    views.setProgressBar(R.id.progressBarWeek, 100, weekProgress, false)
    views.setProgressBar(R.id.progressBarMonth, 100, monthProgress, false)
    views.setProgressBar(R.id.progressBarYear, 100, yearProgress, false)

    // Instruct the widget manager to update the widget
    appWidgetManager.updateAppWidget(appWidgetId, views)

    val alarmHandler = AlarmHandler(context, AlarmHandler.ALL_IN_WIDGET_SERVICE)
    alarmHandler.cancelAlarmManager()
    alarmHandler.setAlarmManager()
}