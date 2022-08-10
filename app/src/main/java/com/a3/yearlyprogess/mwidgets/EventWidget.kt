package com.a3.yearlyprogess.mwidgets

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.format.DateFormat
import android.text.style.RelativeSizeSpan
import android.util.Log
import android.util.SizeF
import android.widget.RemoteViews
import com.a3.yearlyprogess.R
import com.a3.yearlyprogess.helper.ProgressPercentage
import com.a3.yearlyprogess.helper.format
import com.a3.yearlyprogess.manager.AlarmHandler
import java.text.SimpleDateFormat

/**
 * Implementation of App Widget functionality.
 */
class EventWidget : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            updateEventWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: Bundle?
    ) {
        updateEventWidget(context, appWidgetManager, appWidgetId)
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
        val alarmHandler = AlarmHandler(context, AlarmHandler.EVENT_WIDGET_SERVICE)
        alarmHandler.cancelAlarmManager()
    }
}

fun updateEventWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int
) {

    // Construct the RemoteViews object
    val smallView = RemoteViews(context.packageName, R.layout.event_widget_small)
    val mediumView = RemoteViews(context.packageName, R.layout.event_widget_medium)

    val pref = context.getSharedPreferences(appWidgetId.toString(), Context.MODE_PRIVATE)

    val eventTitle = pref.getString("eventTitle", "null").toString()
    val eventDesc = pref.getString("eventDesc", "null")
    val eventStartTimeInMills = pref.getLong("eventStartTimeInMills", 0)
    val eventEndDateTimeInMillis = pref.getLong("eventEndDateTimeInMillis", 0)


    val progressPercentage = ProgressPercentage()
    progressPercentage.getSeconds(ProgressPercentage.CUSTOM_EVENT)

    val progress = progressPercentage.getPercent(ProgressPercentage.CUSTOM_EVENT, eventStartTimeInMills, eventEndDateTimeInMillis)

    Log.d("Event_Widget", "ratio $progress")
    val progressText = "${progress.format(2)}%"

    val spannable = SpannableString(progressText)
    spannable.setSpan(
        RelativeSizeSpan(2f),
        0,
        2,
        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
    )

    mediumView.setTextViewText(R.id.eventProgressText, spannable)
    mediumView.setProgressBar(R.id.eventProgressBar, 100, progress.toInt(), false)

    smallView.setProgressBar(R.id.eventProgressBar, 100, progress.toInt(), false)
    smallView.setTextViewText(R.id.eventProgressText, progressText)


    val viewMapping: Map<SizeF, RemoteViews> = mapOf(
        SizeF(150f, 100f) to smallView,
        SizeF(150f, 200f) to mediumView,
    )
    val remoteViews = RemoteViews(viewMapping)

    mediumView.setTextViewText(R.id.eventTitle, eventTitle)
    mediumView.setTextViewText(R.id.eventDesc, eventDesc)
    mediumView.setTextViewText(
        R.id.eventTime,
        if (DateFormat.is24HourFormat(context)) SimpleDateFormat("MM/dd · HH:mm").format(
            eventEndDateTimeInMillis
        ) else SimpleDateFormat("MM/dd · hh:mm a").format(eventEndDateTimeInMillis)
    )

    smallView.setTextViewText(R.id.eventTitle, eventTitle)

    // Instruct the widget manager to update the widget
    appWidgetManager.updateAppWidget(appWidgetId, remoteViews)

    val alarmHandler = AlarmHandler(context, AlarmHandler.EVENT_WIDGET_SERVICE)
    alarmHandler.cancelAlarmManager()
    alarmHandler.setAlarmManager()
}