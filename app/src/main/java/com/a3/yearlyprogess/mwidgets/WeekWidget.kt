package com.a3.yearlyprogess.mwidgets

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.os.Bundle
import android.widget.RemoteViews
import com.a3.yearlyprogess.R
import kotlin.math.roundToInt
import com.a3.yearlyprogess.helper.*


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
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
    }

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val progressPercentage = ProgressPercentage()
        val progress = progressPercentage.getPercent(ProgressPercentage.WEEK)
        val widgetText = "${progress.format(2)}%"

        // Construct the RemoteViews object
        val smallView = RemoteViews(context.packageName, R.layout.week_widget)
        //   val mediumView = RemoteViews(context.packageName, R.layout.year_progress_medium)


        smallView.setTextViewText(R.id.text_week, progressPercentage.getWeek(str = true))
        smallView.setTextViewText(R.id.progress_text_week, widgetText)
        smallView.setProgressBar(R.id.progress_bar_week, 100, progress.roundToInt(), false)

        /* mediumView.setTextViewText(R.id.appwidget_text, widgetText)
         mediumView.setProgressBar(R.id.appwidget_progress, 100, progress.roundToInt(), false)

         val viewMapping: Map<SizeF, RemoteViews> = mapOf(
             SizeF(110f, 40f) to smallView,
             SizeF(120f, 120f) to mediumView,
         )
     */    // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, smallView)
    }

}

