package com.a3.yearlyprogess.mWidgets

import android.appwidget.AppWidgetManager
import android.content.Context
import android.os.Build
import android.text.SpannableString
import android.text.format.DateFormat
import android.util.Log
import android.util.SizeF
import android.view.View
import android.widget.RemoteViews
import androidx.preference.PreferenceManager
import com.a3.yearlyprogess.R
import com.a3.yearlyprogess.components.CustomEventCardView.Companion.displayRelativeDifferenceMessage
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
        fun eventWidgetPreview(context: Context, event: Event): RemoteViews {
            // Construct the RemoteViews object
            val smallView = RemoteViews(context.packageName, R.layout.event_widget_small)
            val wideView = RemoteViews(context.packageName, R.layout.event_widget_wideview)
            val tallView = RemoteViews(context.packageName, R.layout.event_widget_tallview)


            val eventTitle = event.eventTitle
            val eventDesc = event.eventDescription
            val eventStartTimeInMills = event.eventStartTime
            val eventEndDateTimeInMillis = event.eventEndTime


            var progress = ProgressPercentage.getProgress(
                ProgressPercentage.CUSTOM_EVENT,
                eventStartTimeInMills,
                eventEndDateTimeInMillis
            )

            if (progress > 100) {
                progress = 100.0
            }
            if (progress < 0) {
                progress = 0.0
            }

            val settingsPref = PreferenceManager.getDefaultSharedPreferences(context)
            val decimalPlace: Int =
                settingsPref.getInt(
                    context.getString(R.string.widget_event_widget_decimal_point),
                    2
                )

            val progressText = formatProgressStyle(
                SpannableString(
                    "%,.${decimalPlace}f".format(progress) + "%"
                )
            )



            val formattedEndTime = DateFormat.format(
                if (DateFormat.is24HourFormat(context)) "HH:mm" else "hh:mm a",
                eventEndDateTimeInMillis
            ).toString().uppercase()

            val formattedEndDateTime = DateFormat.format(
                if (DateFormat.is24HourFormat(context)) "MMM dd, yyyy" else "MMM dd, yyyy",
                eventEndDateTimeInMillis
            ).toString() + " · " + formattedEndTime






            wideView.setTextViewText(R.id.eventProgressText, progressText)
            wideView.setProgressBar(R.id.eventProgressBar, 100, progress.toInt(), false)
            wideView.setTextViewText(
                R.id.currentDate,
                ProgressPercentage.getDay(formatted = true)
            )
            wideView.setTextViewText(R.id.eventTitle, eventTitle)
            if (eventDesc.isEmpty()) {
                wideView.setViewVisibility(R.id.eventDesc, View.GONE)
                tallView.setViewVisibility(R.id.eventDesc, View.GONE)


                wideView.setTextViewTextSize(R.id.eventTitle, 0, 85f)
                tallView.setTextViewTextSize(R.id.eventTitle, 0, 60f)

            } else {
                wideView.setTextViewText(R.id.eventDesc, eventDesc)
                tallView.setTextViewText(R.id.eventDesc, eventDesc)

                wideView.setViewVisibility(R.id.eventDesc, View.VISIBLE)
                tallView.setViewVisibility(R.id.eventDesc, View.VISIBLE)

                wideView.setTextViewTextSize(R.id.eventTitle, 0, 45f)
                tallView.setTextViewTextSize(R.id.eventTitle, 0, 45f)

            }

            wideView.setTextViewText(
                R.id.eventTime,
                displayRelativeDifferenceMessage(
                    context,
                    eventStartTimeInMills,
                    eventEndDateTimeInMillis,
                    event.allDayEvent
                )
            )

            tallView.setTextViewText(R.id.eventProgressText, progressText)
            tallView.setProgressBar(R.id.eventProgressBar, 100, progress.toInt(), false)
            tallView.setTextViewText(R.id.currentDate, ProgressPercentage.getDay(formatted = true))
            tallView.setTextViewText(R.id.eventTitle, eventTitle)
            tallView.setTextViewText(
                R.id.eventTime,
                formattedEndDateTime
            )

            smallView.setProgressBar(R.id.eventProgressBar, 100, progress.toInt(), false)
            smallView.setTextViewText(R.id.currentDate, ProgressPercentage.getDay(formatted = true))
            smallView.setTextViewText(R.id.eventProgressText, progressText)
            smallView.setTextViewText(R.id.eventTitle, eventTitle)


            var remoteViews = wideView
            if (Build.VERSION.SDK_INT > 30) {
                val viewMapping: Map<SizeF, RemoteViews> = mapOf(
                    SizeF(60f, 140f) to smallView,
                    SizeF(200f, 200f) to wideView,
                    SizeF(130f, 140f) to tallView
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
        val allDayEvent = pref.getBoolean("allDayEvent", false)
        val eventStartTimeInMills = pref.getLong("eventStartTimeInMills", 0)
        val eventEndDateTimeInMillis = pref.getLong("eventEndDateTimeInMillis", 0)

        val event = Event(
            eventId,
            eventTitle,
            eventDesc,
            allDayEvent,
            eventStartTimeInMills,
            eventEndDateTimeInMillis
        )

        // Instruct the widget manager to update the widget
        val remoteViews = eventWidgetPreview(context, event)
        appWidgetManager.updateAppWidget(appWidgetId, remoteViews)

    }
    /** Delete all cached widget information in the memory
     *  after widget has been deleted.  */
    override fun onDeleted(context: Context?, appWidgetIds: IntArray?) {
        super.onDeleted(context, appWidgetIds)
        appWidgetIds?.forEach { id ->
            context?.getSharedPreferences("eventWidget_${id}", Context.MODE_PRIVATE)?.edit()
                ?.clear()?.apply()
        }
    }

}

