package com.a3.yearlyprogess.mwidgets

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.RelativeSizeSpan
import android.widget.RemoteViews
import com.a3.yearlyprogess.MainActivity
import com.a3.yearlyprogess.R
import kotlin.math.roundToInt
import com.a3.yearlyprogess.helper.*
import com.a3.yearlyprogess.manager.AlarmHandler


/**
 * Implementation of App Widget functionality.
 */
class WeekWidget : AppWidgetProvider() {
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

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val progressPercentage = ProgressPercentage()
        val progress = progressPercentage.getPercent(ProgressPercentage.WEEK)
        val widgetText = SpannableString("${progress.format(2)}%")
        widgetText.setSpan(
            RelativeSizeSpan(0.7f),
            3,
            widgetText.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        // Construct the RemoteViews object
        val smallView = RemoteViews(context.packageName, R.layout.week_widget)
        //   val mediumView = RemoteViews(context.packageName, R.layout.year_progress_medium)


        smallView.setTextViewText(R.id.text_week, progressPercentage.getWeek(str = true))
        smallView.setTextViewText(R.id.progress_text_week, widgetText)
        smallView.setProgressBar(R.id.progress_bar_week, 100, progress.roundToInt(), false)

        smallView.setOnClickPendingIntent(android.R.id.background, PendingIntent.getActivity(
            context, 0, Intent(context, MainActivity::class.java),  PendingIntent.FLAG_IMMUTABLE
        ))

        /* mediumView.setTextViewText(R.id.appwidget_text, widgetText)
         mediumView.setProgressBar(R.id.appwidget_progress, 100, progress.roundToInt(), false)

         val viewMapping: Map<SizeF, RemoteViews> = mapOf(
             SizeF(110f, 40f) to smallView,
             SizeF(120f, 120f) to mediumView,
         )
     */    // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, smallView)
        val alarmHandler = AlarmHandler(context, AlarmHandler.WEEK_WIDGET_SERVICE)
        alarmHandler.cancelAlarmManager()
        alarmHandler.setAlarmManager()
    }

    override fun onDisabled(context: Context) {
        val alarmHandler = AlarmHandler(context, AlarmHandler.WEEK_WIDGET_SERVICE)
        alarmHandler.cancelAlarmManager()
    }

}

