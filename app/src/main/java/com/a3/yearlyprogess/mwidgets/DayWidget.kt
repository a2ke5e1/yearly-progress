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
import com.a3.yearlyprogess.helper.ProgressPercentage
import com.a3.yearlyprogess.helper.format
import com.a3.yearlyprogess.manager.AlarmHandler
import kotlin.math.roundToInt

/**
 * Implementation of App Widget functionality.
 */
class DayWidget : AppWidgetProvider() {
    /* override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) { // There may be multiple widgets active, so update all of them
         for (appWidgetId in appWidgetIds) {
             updateAppWidget(context, appWidgetManager, appWidgetId)
         }
     }

     override fun onAppWidgetOptionsChanged(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int, newOptions: Bundle?) {
         updateAppWidget(context, appWidgetManager, appWidgetId)
         super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
     }

     private fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {

         val smallView = RemoteViews(context.packageName, R.layout.day_widget)
         val progressPercentage = ProgressPercentage()
         val progress = progressPercentage.getPercent(ProgressPercentage.DAY)
         val widgetText = "${progress.roundToInt()}%"


         smallView.setTextViewText(R.id.text_day, progressPercentage.getDay(true))
         smallView.setProgressBar(R.id.progress_bar_day, 100, progress.roundToInt(), false)
         smallView.setTextViewText(R.id.progress_text_day, widgetText)


         appWidgetManager.updateAppWidget(appWidgetId, smallView)
     }*/

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {

        val smallView = RemoteViews(context.packageName, R.layout.day_widget)
        val progressPercentage = ProgressPercentage()
        val progress = progressPercentage.getPercent(ProgressPercentage.DAY)
        val widgetText = SpannableString("${progress.format(2)}%")
        widgetText.setSpan(
            RelativeSizeSpan(0.7f),
            widgetText.indexOf('.'),
            widgetText.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        smallView.setTextViewText(R.id.text_day, progressPercentage.getDay(true))
        smallView.setProgressBar(R.id.progress_bar_day, 100, progress.roundToInt(), false)
        smallView.setTextViewText(R.id.progress_text_day, widgetText)

        smallView.setOnClickPendingIntent(android.R.id.background, PendingIntent.getActivity(
            context, 0, Intent(context, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE
        ))

        appWidgetManager.updateAppWidget(appWidgetId, smallView)

        val alarmHandler = AlarmHandler(context, AlarmHandler.DAY_WIDGET_SERVICE)
        alarmHandler.cancelAlarmManager()
        alarmHandler.setAlarmManager()
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: Bundle?
    ) {
        updateAppWidget(context, appWidgetManager, appWidgetId)
    }


    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onDisabled(context: Context) {
        val alarmHandler = AlarmHandler(context, AlarmHandler.DAY_WIDGET_SERVICE)
        alarmHandler.cancelAlarmManager()
    }
}