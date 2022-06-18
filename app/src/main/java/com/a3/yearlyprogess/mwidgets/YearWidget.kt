package com.a3.yearlyprogess.mwidgets

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.os.Bundle
import android.widget.RemoteViews
import com.a3.yearlyprogess.R
import kotlin.math.roundToInt
import com.a3.yearlyprogess.helper.*
import com.a3.yearlyprogess.manager.AlarmHandler


/**
 * Implementation of App Widget functionality.
 */
class YearWidget : AppWidgetProvider() {
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
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
    }

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val progressPercentage = ProgressPercentage()
        val progress = progressPercentage.getPercent(ProgressPercentage.YEAR)
        val widgetText = "${progress.format(5)}%"

        // Construct the RemoteViews object
        val smallView = RemoteViews(context.packageName, R.layout.year_widget)
        //   val mediumView = RemoteViews(context.packageName, R.layout.year_progress_medium)


        smallView.setTextViewText(R.id.text_year, progressPercentage.getYear())
        smallView.setTextViewText(R.id.progress_text_year, widgetText)
        smallView.setProgressBar(R.id.progress_bar_year, 100, progress.roundToInt(), false)

        /* mediumView.setTextViewText(R.id.appwidget_text, widgetText)
         mediumView.setProgressBar(R.id.appwidget_progress, 100, progress.roundToInt(), false)

         val viewMapping: Map<SizeF, RemoteViews> = mapOf(
             SizeF(110f, 40f) to smallView,
             SizeF(120f, 120f) to mediumView,
         )
     */    // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, smallView)


        val alarmHandler = AlarmHandler(context, AlarmHandler.YEAR_WIDGET_SERVICE)
        alarmHandler.cancelAlarmManager()
        alarmHandler.setAlarmManager()
    }

    override fun onDisabled(context: Context) {
        val alarmHandler = AlarmHandler(context, AlarmHandler.YEAR_WIDGET_SERVICE)
        alarmHandler.cancelAlarmManager()
    }


}

