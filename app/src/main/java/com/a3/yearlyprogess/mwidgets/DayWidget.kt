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
import com.a3.yearlyprogess.mwidgets.util.BaseWidget
import kotlin.math.roundToInt

/**
 * Implementation of App Widget functionality.
 */
class DayWidget : BaseWidget(AlarmHandler.DAY_WIDGET_SERVICE) {

    override fun updateWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val smallView = RemoteViews(context.packageName, R.layout.day_widget)
        val progressPercentage = ProgressPercentage()
        val progress = progressPercentage.getPercent(ProgressPercentage.DAY)
        val widgetText = formatProgressStyle(progress)

        smallView.setTextViewText(R.id.text_day, progressPercentage.getDay(true))
        smallView.setProgressBar(R.id.progress_bar_day, 100, progress.roundToInt(), false)
        smallView.setTextViewText(R.id.progress_text_day, widgetText)

        smallView.setOnClickPendingIntent(android.R.id.background, PendingIntent.getActivity(
            context, 0, Intent(context, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE
        ))

        appWidgetManager.updateAppWidget(appWidgetId, smallView)
    }


}

fun formatProgressStyle(progress: Double): SpannableString {
    val widgetText = SpannableString("${progress.format(2)}%")
    widgetText.setSpan(
        RelativeSizeSpan(0.7f),
        widgetText.indexOf('.'),
        widgetText.length,
        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
    )
    return widgetText
}