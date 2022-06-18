package com.a3.yearlyprogess.mwidgets

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews
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

    private fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {

        val smallView = RemoteViews(context.packageName, R.layout.day_widget)
        val progressPercentage = ProgressPercentage()
        val progress = progressPercentage.getPercent(ProgressPercentage.DAY)
        val widgetText = "${progress.format(2)}%"


        smallView.setTextViewText(R.id.text_day, progressPercentage.getDay(true))
        smallView.setProgressBar(R.id.progress_bar_day, 100, progress.roundToInt(), false)
        smallView.setTextViewText(R.id.progress_text_day, widgetText)


        appWidgetManager.updateAppWidget(appWidgetId, smallView)

        val alarmHandler = AlarmHandler(context)
        alarmHandler.cancelAlarmManager()
        alarmHandler.setAlarmManager()
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
        val alarmHandler = AlarmHandler(context)
        alarmHandler.cancelAlarmManager()
    }
}