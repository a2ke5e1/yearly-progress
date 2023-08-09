package com.a3.yearlyprogess.mWidgets

import android.appwidget.AppWidgetManager
import android.content.Context
import android.os.Build
import android.text.SpannableString
import android.text.format.DateFormat
import android.util.Log
import android.util.SizeF
import android.widget.RemoteViews
import androidx.preference.PreferenceManager
import com.a3.yearlyprogess.R
import com.a3.yearlyprogess.eventManager.model.Event
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

    companion object {
        fun eventWidgetPreview(context: Context, event: Event) : RemoteViews {
            // Construct the RemoteViews object
            val smallView = RemoteViews(context.packageName, R.layout.event_widget_small)
            val mediumView = RemoteViews(context.packageName, R.layout.event_widget_medium)



            val eventTitle = event.eventTitle
            val eventDesc =  event.eventDescription
            val eventStartTimeInMills = event.eventStartTime
            val eventEndDateTimeInMillis = event.eventEndTime


            val progress = ProgressPercentage.getProgress(
                ProgressPercentage.CUSTOM_EVENT,
                eventStartTimeInMills,
                eventEndDateTimeInMillis
            )

            val settingsPref = PreferenceManager.getDefaultSharedPreferences(context)
            val decimalPlace: Int =
                settingsPref.getInt(context.getString(R.string.widget_event_widget_decimal_point), 2)

            val progressText = formatProgressStyle(
                SpannableString(
                    "%,.${decimalPlace}f".format(progress) + "%"
                )
            )

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

            return remoteViews
        }
    }

    override fun updateWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {

        val pref = context.getSharedPreferences("eventWidget_${appWidgetId}", Context.MODE_PRIVATE)

        val eventId = pref.getInt("eventId", 0)
        val eventTitle = pref.getString("eventTitle", "Loading").toString()
        val eventDesc = pref.getString("eventDesc", "").toString()
        val eventStartTimeInMills = pref.getLong("eventStartTimeInMills", 0)
        val eventEndDateTimeInMillis = pref.getLong("eventEndDateTimeInMillis", 0)

        val event = Event(
            eventId,
            eventTitle,
            eventDesc,
            eventStartTimeInMills,
            eventEndDateTimeInMillis
        )

        // Instruct the widget manager to update the widget
        val remoteViews = eventWidgetPreview(context, event)
        appWidgetManager.updateAppWidget(appWidgetId, remoteViews)

    }
}

