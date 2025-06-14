package com.a3.yearlyprogess.widgets.ui

import android.appwidget.AppWidgetManager
import android.content.Context
import android.os.Build
import android.text.format.DateFormat
import android.util.SizeF
import android.view.View
import android.widget.RemoteViews
import androidx.annotation.FloatRange
import androidx.annotation.IntRange
import androidx.preference.PreferenceManager
import com.a3.yearlyprogess.R
import com.a3.yearlyprogess.TimePeriod
import com.a3.yearlyprogess.YearlyProgressUtil
import com.a3.yearlyprogess.components.EventDetailView.Companion.displayRelativeDifferenceMessage
import com.a3.yearlyprogess.widgets.manager.eventManager.data.EventDatabase
import com.a3.yearlyprogess.widgets.manager.eventManager.model.Event
import com.a3.yearlyprogess.widgets.manager.eventManager.repo.EventRepository
import com.a3.yearlyprogess.widgets.ui.util.styleFormatted
import com.a3.yearlyprogess.widgets.ui.util.toFormattedTimePeriod
import com.a3.yearlyprogess.widgets.ui.util.toTimePeriodText
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class EventWidgetOption(
    val widgetId: Int,
    val decimalPlaces: Int,
    val timeLeftCounter: Boolean,
    val dynamicLeftCounter: Boolean,
    val replaceProgressWithDaysLeft: Boolean,
    @IntRange(from = 0, to = 100) val backgroundTransparency: Int,
    @FloatRange(from = 0.1, to = 2.0) val fontScale: Float
) {
  companion object {

    private const val EVENT_WIDGET_PREF = "event_widget_pref"

    fun load(
        context: Context,
        widgetId: Int,
    ): EventWidgetOption {
      val globalPref = PreferenceManager.getDefaultSharedPreferences(context)
      val eventWidgetPreferences =
          context.getSharedPreferences(EVENT_WIDGET_PREF, Context.MODE_PRIVATE)

      val globalDecimalPointKey = context.getString(R.string.widget_event_widget_decimal_point)
      val globalTimeLeftKey = context.getString(R.string.widget_widget_time_left)
      val globalDynamicTimeLeftKey = context.getString(R.string.widget_widget_use_dynamic_time_left)
      val globalReplaceWithCounterKey =
          context.getString(R.string.widget_widget_event_replace_progress_with_days_counter)
      val globalBackgroundTransparencyKey =
          context.getString(R.string.widget_widget_background_transparency)

      val globalDecimalPoint = globalPref.getInt(globalDecimalPointKey, 2)
      val globalTimeLeft = globalPref.getBoolean(globalTimeLeftKey, false)
      val globalDynamicTimeLeft = globalPref.getBoolean(globalDynamicTimeLeftKey, false)
      val globalReplaceWithCounter = globalPref.getBoolean(globalReplaceWithCounterKey, false)
      val globalBackgroundTransparency = globalPref.getInt(globalBackgroundTransparencyKey, 100)

      val eventWidgetOptionsJsonString =
          eventWidgetPreferences.getString(widgetId.toString(), null)
              ?: return EventWidgetOption(
                  widgetId = widgetId,
                  decimalPlaces = globalDecimalPoint,
                  timeLeftCounter = globalTimeLeft,
                  dynamicLeftCounter = globalDynamicTimeLeft,
                  replaceProgressWithDaysLeft = globalReplaceWithCounter,
                  backgroundTransparency = globalBackgroundTransparency,
                  fontScale = 1f)
      return Gson().fromJson(eventWidgetOptionsJsonString, EventWidgetOption::class.java)
    }

    fun save(context: Context, options: EventWidgetOption) {
      val sharedPreferences = context.getSharedPreferences(EVENT_WIDGET_PREF, Context.MODE_PRIVATE)
      val edit = sharedPreferences.edit()
      val jsonString = Gson().toJson(options)
      edit.putString(options.widgetId.toString(), jsonString).apply()
    }

    fun delete(context: Context, widgetId: Int) {
      context
          .getSharedPreferences(EVENT_WIDGET_PREF, Context.MODE_PRIVATE)
          .edit()
          .putString(widgetId.toString(), null)
          .apply()
    }
  }
}

/** Implementation of App Widget functionality. */
class EventWidget : BaseWidget() {
  companion object {
    fun eventWidgetPreview(
        context: Context,
        event: Event,
        options: EventWidgetOption
    ): RemoteViews {
      // Construct the RemoteViews object
      val smallView = RemoteViews(context.packageName, R.layout.event_widget_small)
      val wideView = RemoteViews(context.packageName, R.layout.event_widget_wideview)
      val tallView = RemoteViews(context.packageName, R.layout.event_widget_tallview)

      val eventTitle = event.eventTitle
      val eventDesc = event.eventDescription

      val yp = YearlyProgressUtil(context)
      val (newEventStart, newEventEnd) =
          event.nextStartAndEndTime(currentTime = System.currentTimeMillis())
      val progress = yp.calculateProgress(newEventStart, newEventEnd).coerceIn(0.0, 100.0)

      val progressText = progress.styleFormatted(options.decimalPlaces)
      val formattedEndTime =
          DateFormat.format(
                  if (DateFormat.is24HourFormat(context)) "HH:mm" else "hh:mm a",
                  newEventEnd,
              )
              .toString()
              .uppercase()

      val formattedEndDateTime =
          DateFormat.format(
                  if (DateFormat.is24HourFormat(context)) "MMM dd, yyyy" else "MMM dd, yyyy",
                  newEventEnd,
              )
              .toString() + " · " + formattedEndTime

      wideView.setTextViewText(R.id.eventProgressText, progressText)
      wideView.setProgressBar(R.id.eventProgressBar, 100, progress.toInt(), false)
      wideView.setTextViewText(
          R.id.currentDate,
          yp.getCurrentPeriodValue(TimePeriod.DAY).toFormattedTimePeriod(context, TimePeriod.DAY),
      )
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
          displayRelativeDifferenceMessage(context, newEventStart, newEventEnd, event.allDayEvent),
      )

      tallView.setTextViewText(R.id.eventProgressText, progressText)
      tallView.setProgressBar(R.id.eventProgressBar, 100, progress.toInt(), false)
      tallView.setTextViewText(
          R.id.currentDate,
          yp.getCurrentPeriodValue(TimePeriod.DAY).toFormattedTimePeriod(context, TimePeriod.DAY),
      )
      tallView.setTextViewText(R.id.eventTitle, eventTitle)
      tallView.setTextViewText(R.id.eventTime, formattedEndDateTime)

      smallView.setProgressBar(R.id.eventProgressBar, 100, progress.toInt(), false)
      smallView.setTextViewText(
          R.id.currentDate,
          yp.getCurrentPeriodValue(TimePeriod.DAY).toFormattedTimePeriod(context, TimePeriod.DAY),
      )
      smallView.setTextViewText(R.id.eventProgressText, progressText)
      smallView.setTextViewText(R.id.eventTitle, eventTitle)

      val timeLeftCounter = options.timeLeftCounter
      val replaceProgressWithDaysLeft = options.replaceProgressWithDaysLeft
      val dynamicTimeLeft = options.dynamicLeftCounter

      val eventTimeLeft =
          context.getString(
              R.string.time_left,
              yp.calculateTimeLeft(newEventEnd).toTimePeriodText(dynamicTimeLeft),
          )

      if (timeLeftCounter) {

        smallView.setViewVisibility(R.id.widgetDaysLeft, View.VISIBLE)
        wideView.setViewVisibility(R.id.widgetDaysLeft, View.VISIBLE)
        tallView.setViewVisibility(R.id.widgetDaysLeft, View.VISIBLE)

        smallView.setTextViewTextSize(R.id.eventProgressText, 0, 50f)
        wideView.setTextViewTextSize(R.id.eventProgressText, 0, 50f)
        tallView.setTextViewTextSize(R.id.eventProgressText, 0, 50f)

        smallView.setTextViewText(R.id.widgetDaysLeft, eventTimeLeft)
        wideView.setTextViewText(R.id.widgetDaysLeft, eventTimeLeft)
        tallView.setTextViewText(R.id.widgetDaysLeft, eventTimeLeft)
      }

      if (timeLeftCounter && replaceProgressWithDaysLeft) {

        smallView.setViewVisibility(R.id.widgetDaysLeft, View.GONE)
        wideView.setViewVisibility(R.id.widgetDaysLeft, View.GONE)
        tallView.setViewVisibility(R.id.widgetDaysLeft, View.GONE)

        smallView.setTextViewText(R.id.eventProgressText, eventTimeLeft)
        wideView.setTextViewText(R.id.eventProgressText, eventTimeLeft)
        tallView.setTextViewText(R.id.eventProgressText, eventTimeLeft)
        smallView.setTextViewTextSize(R.id.eventProgressText, 0, 35f)
        wideView.setTextViewTextSize(R.id.eventProgressText, 0, 35f)
        tallView.setTextViewTextSize(R.id.eventProgressText, 0, 35f)
      }

      var widgetBackgroundAlpha = options.backgroundTransparency
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
                SizeF(130f, 140f) to tallView,
            )
        remoteViews = RemoteViews(viewMapping)
      }

      return remoteViews
    }
  }

  private var updateJob: Job? = null

  override fun updateWidget(
      context: Context,
      appWidgetManager: AppWidgetManager,
      appWidgetId: Int,
  ) {
    val pref = context.getSharedPreferences("eventWidget_$appWidgetId", Context.MODE_PRIVATE)

    val eventDao = EventDatabase.getDatabase(context).eventDao()
    val repository = EventRepository(eventDao)

    val eventId = pref.getInt("eventId", 0)

    updateJob =
        CoroutineScope(Dispatchers.IO).launch {
          var counter = 0
          while (isActive && counter < 5) { // Check if the coroutine is still active
            counter++

            val event = repository.getEvent(eventId)
            val options = EventWidgetOption.load(context, appWidgetId)
            val remoteViews = event?.let { eventWidgetPreview(context, it, options) }
            appWidgetManager.updateAppWidget(appWidgetId, remoteViews)

            delay(900)
          }
        }
  }

  /** Delete all cached widget information in the memory after widget has been deleted. */
  override fun onDeleted(
      context: Context,
      appWidgetIds: IntArray,
  ) {
    super.onDeleted(context, appWidgetIds)
    appWidgetIds.forEach { id -> EventWidgetOption.delete(context, id) }
    clearJob()
  }

  private fun clearJob() {
    updateJob?.cancel()
    updateJob = null
  }

  override fun onDisabled(context: Context) {
    clearJob()
    super.onDisabled(context)
  }
}
