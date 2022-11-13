package com.a3.yearlyprogess.mwidgets

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.RelativeSizeSpan
import android.text.style.SuperscriptSpan
import android.util.SizeF
import android.view.View
import android.widget.RemoteViews
import com.a3.yearlyprogess.R
import com.a3.yearlyprogess.helper.ProgressPercentage
import com.a3.yearlyprogess.manager.AlarmHandler
import java.text.SimpleDateFormat
import java.util.*

/**
 * Implementation of App Widget functionality.
 */
class AllInWidget : AppWidgetProvider() {
    override fun onUpdate(
        context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray
    ) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onAppWidgetOptionsChanged(
        context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int, newOptions: Bundle?
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
    context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int
) {

    val small = RemoteViews(context.packageName, R.layout.all_in_widget)
    val medium = RemoteViews(context.packageName, R.layout.all_in_widget)
    val large = RemoteViews(context.packageName, R.layout.all_in_widget)
    val xlarge = RemoteViews(context.packageName, R.layout.all_in_widget)

    initiateView(small)
    initiateView(medium)
    initiateView(large)
    initiateView(xlarge)

    small.setViewVisibility(R.id.testWeek, View.GONE)
    small.setViewVisibility(R.id.testMonth, View.GONE)
    small.setViewVisibility(R.id.testYear, View.GONE)

    medium.setViewVisibility(R.id.testWeek, View.GONE)
    medium.setViewVisibility(R.id.testYear, View.GONE)

    large.setViewVisibility(R.id.testWeek, View.GONE)


    // Instruct the widget manager to update the widget

    var remoteViews = xlarge
    if (Build.VERSION.SDK_INT > 30) {


        val viewMapping: Map<SizeF, RemoteViews> = mapOf(
            SizeF(300f, 100f) to xlarge,
            SizeF(220f, 100f) to large,
            SizeF(160f, 100f) to medium,
            SizeF(100f, 100f) to small,
        )

        remoteViews = RemoteViews(viewMapping)
    }

    appWidgetManager.updateAppWidget(appWidgetId, remoteViews)

    val alarmHandler = AlarmHandler(context, AlarmHandler.ALL_IN_WIDGET_SERVICE)
    alarmHandler.cancelAlarmManager()
    alarmHandler.setAlarmManager()
}

internal fun initiateView(views: RemoteViews) {
    val progressPercentage = ProgressPercentage()

    val dayProgress = progressPercentage.getPercent(ProgressPercentage.DAY).toInt()
    val weekProgress = progressPercentage.getPercent(ProgressPercentage.WEEK).toInt()
    val monthProgress = progressPercentage.getPercent(ProgressPercentage.MONTH).toInt()
    val yearProgress = progressPercentage.getPercent(ProgressPercentage.YEAR).toInt()



    views.setTextViewText(R.id.progressTextDay, formatProgress(dayProgress))
    views.setTextViewText(R.id.progressTextWeek, formatProgress(weekProgress))
    views.setTextViewText(R.id.progressTextMonth, formatProgress(monthProgress))
    views.setTextViewText(R.id.progressTextYear, formatProgress(yearProgress))

    views.setProgressBar(R.id.progressBarDay, 100, dayProgress, false)
    views.setProgressBar(R.id.progressBarWeek, 100, weekProgress, false)
    views.setProgressBar(R.id.progressBarMonth, 100, monthProgress, false)
    views.setProgressBar(R.id.progressBarYear, 100, yearProgress, false)


    views.setTextViewText(R.id.progressTitle, formatCurrentDay(progressPercentage))
    views.setTextViewText(
        R.id.progressWeekTitle,
        SimpleDateFormat("EEE", Locale.getDefault()).format(System.currentTimeMillis())
    )
    views.setTextViewText(
        R.id.progressMonthTitle,
        SimpleDateFormat("MMM", Locale.getDefault()).format(System.currentTimeMillis())
    )
    views.setTextViewText(R.id.progressYearTitle, progressPercentage.getYear())


    views.setViewVisibility(R.id.testDay, View.VISIBLE)
    views.setViewVisibility(R.id.testWeek, View.VISIBLE)
    views.setViewVisibility(R.id.testMonth, View.VISIBLE)
    views.setViewVisibility(R.id.testYear, View.VISIBLE)
}

fun formatProgress(progress: Int): SpannableString {
    val spannable = SpannableString("${progress}%")
    spannable.setSpan(
        RelativeSizeSpan(0.7f),
        spannable.length - 1,
        spannable.length,
        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
    )
    return spannable
}

fun formatCurrentDay(progressPercentage: ProgressPercentage): SpannableString {
    val day = progressPercentage.getDay()
    val spannable = SpannableString(
        "${day}${
            when (day.last()) {
                '1' -> "st"
                '2' -> "nd"
                '3' -> "rd"
                else -> "th"
            }
        }"
    )
    spannable.setSpan(
        SuperscriptSpan(),
        spannable.length - 2,
        spannable.length,
        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE

    )
    spannable.setSpan(
        RelativeSizeSpan(0.5f),
        spannable.length - 2,
        spannable.length,
        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE

    )
    return spannable
}