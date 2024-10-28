package com.a3.yearlyprogess.widgets.ui

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
import com.a3.yearlyprogess.TimePeriod
import com.a3.yearlyprogess.calculateProgress
import com.a3.yearlyprogess.calculateTimeLeft
import com.a3.yearlyprogess.components.EventDetailView.Companion.displayRelativeDifferenceMessage
import com.a3.yearlyprogess.getCurrentPeriodValue
import com.a3.yearlyprogess.widgets.manager.eventManager.model.Converters
import com.a3.yearlyprogess.widgets.manager.eventManager.model.Event
import com.a3.yearlyprogess.widgets.ui.util.styleFormatted
import com.a3.yearlyprogess.widgets.ui.util.toFormattedTimePeriod
import com.a3.yearlyprogess.widgets.ui.util.toTimePeriodLeftText
import java.util.Date

/** Implementation of App Widget functionality. */
class EventWidget : BaseWidget() {

  companion object {
    fun eventWidgetPreview(context: Context, event: Event): RemoteViews {
      // Construct the RemoteViews object
      val smallView = RemoteViews(context.packageName, R.layout.event_widget_small)
      val wideView = RemoteViews(context.packageName, R.layout.event_widget_wideview)
      val tallView = RemoteViews(context.packageName, R.layout.event_widget_tallview)

      val eventTitle = event.eventTitle
      val eventDesc = event.eventDescription

        val (newEventStart, newEventEnd) = event.nextStartAndEndTime(currentTime = System.currentTimeMillis())
        var progress = calculateProgress(context, newEventStart, newEventEnd)


      progress = if (progress > 100) 100.0 else progress
      progress = if (progress < 0) 0.0 else progress

      val settingsPref = PreferenceManager.getDefaultSharedPreferences(context)
      val decimalPlace: Int =
          settingsPref.getInt(context.getString(R.string.widget_event_widget_decimal_point), 2)

      val progressText =
          progress.styleFormatted(decimalPlace)
      val formattedEndTime =
          DateFormat.format(
              if (DateFormat.is24HourFormat(context)) "HH:mm" else "hh:mm a", newEventEnd
          )
              .toString()
              .uppercase()

      val formattedEndDateTime =
          DateFormat.format(
              if (DateFormat.is24HourFormat(context)) "MMM dd, yyyy" else "MMM dd, yyyy",
              newEventEnd
          )
              .toString() + " · " + formattedEndTime

      wideView.setTextViewText(R.id.eventProgressText, progressText)
      wideView.setProgressBar(R.id.eventProgressBar, 100, progress.toInt(), false)
      wideView.setTextViewText(R.id.currentDate, getCurrentPeriodValue(TimePeriod.DAY).toFormattedTimePeriod(TimePeriod.DAY))
      wideView.setTextViewText(R.id.eventTitle, eventTitle)
      if (eventDesc.isEmpty()) {
        wideView.setViewVisibility(R.id.eventDesc, View.GONE)
        tallView.setViewVisibility(R.id.eventDesc, View.GONE)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
          wideView.setTextViewTextSize(R.id.eventTitle, 0, 85f)
          tallView.setTextViewTextSize(R.id.eventTitle, 0, 60f)
        }
      } else {
        wideView.setTextViewText(R.id.eventDesc, eventDesc)
        tallView.setTextViewText(R.id.eventDesc, eventDesc)

        wideView.setViewVisibility(R.id.eventDesc, View.VISIBLE)
        tallView.setViewVisibility(R.id.eventDesc, View.VISIBLE)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
          wideView.setTextViewTextSize(R.id.eventTitle, 0, 45f)
          tallView.setTextViewTextSize(R.id.eventTitle, 0, 45f)
        }
      }

      wideView.setTextViewText(
          R.id.eventTime,
          displayRelativeDifferenceMessage(
              context, newEventStart, newEventEnd, event.allDayEvent
          )
      )

      tallView.setTextViewText(R.id.eventProgressText, progressText)
      tallView.setProgressBar(R.id.eventProgressBar, 100, progress.toInt(), false)
      tallView.setTextViewText(R.id.currentDate, getCurrentPeriodValue(TimePeriod.DAY).toFormattedTimePeriod(TimePeriod.DAY))
      tallView.setTextViewText(R.id.eventTitle, eventTitle)
      tallView.setTextViewText(R.id.eventTime, formattedEndDateTime)

      smallView.setProgressBar(R.id.eventProgressBar, 100, progress.toInt(), false)
      smallView.setTextViewText(R.id.currentDate, getCurrentPeriodValue(TimePeriod.DAY).toFormattedTimePeriod(TimePeriod.DAY))
      smallView.setTextViewText(R.id.eventProgressText, progressText)
      smallView.setTextViewText(R.id.eventTitle, eventTitle)

      val timeLeftCounter =
          settingsPref.getBoolean(context.getString(R.string.widget_widget_time_left), false)

      val replaceProgressWithDaysLeft =
          settingsPref.getBoolean(
              context.getString(R.string.widget_widget_event_replace_progress_with_days_counter),
              false)

        val dynamicTimeLeft =
          settingsPref.getBoolean(
              context.getString(R.string.widget_widget_use_dynamic_time_left), false)

        val eventTimeLeft = calculateTimeLeft(newEventEnd).toTimePeriodLeftText(dynamicTimeLeft)

      if (timeLeftCounter && replaceProgressWithDaysLeft) {
        smallView.setTextViewText(R.id.eventProgressText, eventTimeLeft)
        wideView.setTextViewText(R.id.eventProgressText, eventTimeLeft)
        tallView.setTextViewText(R.id.eventProgressText, eventTimeLeft)
        smallView.setTextViewTextSize(R.id.eventProgressText, 0, 35f)
        wideView.setTextViewTextSize(R.id.eventProgressText, 0, 35f)
        tallView.setTextViewTextSize(R.id.eventProgressText, 0, 35f)
      }

      // Loads user preference if widgets needs to be transparent or
      // not. Other prefs might be loaded the same way.

      // TODO: Make a better way to load user prefs that
      //  applies to kind of the widgets.
      var widgetBackgroundAlpha =
          settingsPref.getInt(
              context.getString(R.string.widget_widget_background_transparency), 100)

      widgetBackgroundAlpha = ((widgetBackgroundAlpha / 100.0) * 255).toInt()

      smallView.setInt(R.id.widgetContainer, "setImageAlpha", widgetBackgroundAlpha)
      tallView.setInt(R.id.widgetContainer, "setImageAlpha", widgetBackgroundAlpha)
      wideView.setInt(R.id.widgetContainer, "setImageAlpha", widgetBackgroundAlpha)

      var remoteViews = wideView
      if (Build.VERSION.SDK_INT > 30) {
        val viewMapping: Map<SizeF, RemoteViews> =
            mapOf(
                SizeF(60f, 140f) to smallView,
                SizeF(200f, 200f) to wideView,
                SizeF(130f, 140f) to tallView)
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
    val conv = Converters()

    val eventId = pref.getInt("eventId", 0)
    val eventTitle = pref.getString("eventTitle", "Loading").toString()
    val eventDesc = pref.getString("eventDesc", "").toString()
    val allDayEvent = pref.getBoolean("allDayEvent", false)
    val eventStartTimeInMills = pref.getLong("eventStartTimeInMills", 0)
    val eventEndDateTimeInMillis = pref.getLong("eventEndDateTimeInMillis", 0)
    val eventRepeatDays = conv.toRepeatDaysList(pref.getString("eventRepeatDays", "").toString())

    Log.d("EventWidget", "Event: $eventRepeatDays")

    val event =
        Event(
            eventId,
            eventTitle,
            eventDesc,
            allDayEvent, Date(eventStartTimeInMills), Date(eventEndDateTimeInMillis),
            eventRepeatDays)

    // Instruct the widget manager to update the widget
    val remoteViews = eventWidgetPreview(context, event)
    appWidgetManager.updateAppWidget(appWidgetId, remoteViews)
  }

  /** Delete all cached widget information in the memory after widget has been deleted. */
  override fun onDeleted(context: Context?, appWidgetIds: IntArray?) {
    super.onDeleted(context, appWidgetIds)
    appWidgetIds?.forEach { id ->
      context
          ?.getSharedPreferences("eventWidget_${id}", Context.MODE_PRIVATE)
          ?.edit()
          ?.clear()
          ?.apply()
    }
  }
}
