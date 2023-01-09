package com.a3.yearlyprogess.mWidgets

import android.appwidget.AppWidgetManager
import android.content.Context
import android.os.Build
import android.text.format.DateFormat
import android.util.Log
import android.util.SizeF
import android.widget.RemoteViews
import com.a3.yearlyprogess.R
import com.a3.yearlyprogess.helper.ProgressPercentage.Companion.formatProgressStyle
import com.a3.yearlyprogess.helper.ProgressPercentage
import com.a3.yearlyprogess.mWidgets.util.BaseWidget
import com.a3.yearlyprogess.manager.AlarmHandler
import java.text.SimpleDateFormat
import java.util.*

/**
 * Implementation of App Widget functionality.
 */
class EventWidget : BaseWidget(AlarmHandler.EVENT_WIDGET_SERVICE) {


    override fun updateWidget(
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


        val progress = ProgressPercentage.getProgress(
            ProgressPercentage.CUSTOM_EVENT,
            eventStartTimeInMills,
            eventEndDateTimeInMillis
        )

        Log.d("Event_Widget", "ratio $progress")
        val progressText = formatProgressStyle(progress)


        mediumView.setTextViewText(R.id.eventProgressText, progressText)
        mediumView.setProgressBar(R.id.eventProgressBar, 100, progress.toInt(), false)
        mediumView.setTextViewText(R.id.currentDate, ProgressPercentage.getDay(formatted = true))
        mediumView.setTextViewText(R.id.eventTitle, eventTitle)
        mediumView.setTextViewText(R.id.eventDesc, eventDesc)
        mediumView.setTextViewText(
            R.id.eventTime,
            if (DateFormat.is24HourFormat(context)) SimpleDateFormat(
                "MM/dd · HH:mm",
                Locale.getDefault()
            ).format(
                eventEndDateTimeInMillis
            ) else SimpleDateFormat("MM/dd · hh:mm a", Locale.getDefault()).format(
                eventEndDateTimeInMillis
            )
        )

        smallView.setProgressBar(R.id.eventProgressBar, 100, progress.toInt(), false)
        smallView.setTextViewText(R.id.currentDate, ProgressPercentage.getDay(formatted = true))
        smallView.setTextViewText(R.id.eventProgressText, progressText)
        smallView.setTextViewText(R.id.eventTitle, eventTitle)


        var remoteViews = mediumView
        if (Build.VERSION.SDK_INT > 30) {
            val viewMapping: Map<SizeF, RemoteViews> = mapOf(
                SizeF(80f, 50f) to smallView,
                SizeF(80f, 200f) to mediumView,
            )
            remoteViews = RemoteViews(viewMapping)
        }


        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, remoteViews)

    }
}

