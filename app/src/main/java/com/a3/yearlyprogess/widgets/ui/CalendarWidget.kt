package com.a3.yearlyprogess.widgets.ui

import android.Manifest
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.SizeF
import android.view.View
import android.widget.RemoteViews
import androidx.core.graphics.toColor
import com.a3.yearlyprogess.R
import com.a3.yearlyprogess.YearlyProgressUtil
import com.a3.yearlyprogess.widgets.manager.CalendarEventInfo.getCalendarsDetails
import com.a3.yearlyprogess.widgets.manager.CalendarEventInfo.getTodayOrNearestEvents
import com.a3.yearlyprogess.widgets.manager.CalendarWidgetConfig
import com.a3.yearlyprogess.widgets.manager.eventManager.model.Event
import com.a3.yearlyprogess.widgets.ui.util.styleFormatted
import com.a3.yearlyprogess.widgets.ui.util.toTimePeriodText
import java.util.Date
import kotlin.math.roundToInt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class CalendarEventsSwiper(val context: Context, events: List<Event>, limits: Int = 5) {

  private val pref = context.getSharedPreferences(SWIPER_KEY, Context.MODE_PRIVATE)

  private val _events =
      events
          .filter { it.eventEndTime.time > System.currentTimeMillis() }
          .sortedBy { it.eventStartTime }
          .take(limits)

  private var _currentEventIndex: Int
    get() {
      return if (pref.getInt(SWIPER_CURRENT_INDEX, 0) < _events.size) {
        pref.getInt(SWIPER_CURRENT_INDEX, 0)
      } else {
        0
      }
    }
    set(value) {
      pref.edit().putInt(SWIPER_CURRENT_INDEX, value).apply()
    }

  fun next() {
    if (_events.isEmpty()) return
    _currentEventIndex = (_currentEventIndex + 1) % _events.size
  }

  fun previous() {
    if (_events.isEmpty()) return
    _currentEventIndex = (_currentEventIndex - 1 + _events.size) % _events.size
  }

  fun current(): Event? {
    return _events.getOrNull(_currentEventIndex)
  }

  fun indicator(): SpannableString {
    val indicatorText = _events.indices.joinToString("") { _ -> "⬤" }
    val spannableString = SpannableString(indicatorText)
    val indicatorColor = context.getColor(R.color.widget_text_color).toColor()
    val colorWithOpacity =
        Color.argb(0.5f, indicatorColor.red(), indicatorColor.green(), indicatorColor.blue())
    spannableString.setSpan(
        ForegroundColorSpan(colorWithOpacity),
        0,
        _currentEventIndex,
        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    spannableString.setSpan(
        ForegroundColorSpan(colorWithOpacity),
        _currentEventIndex + 1,
        indicatorText.length,
        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    return spannableString
  }

  companion object {
    private const val SWIPER_KEY = "CalendarEventsSwiper"
    private const val SWIPER_CURRENT_INDEX = "CalendarEventsSwiperIndex"
    const val ACTION_NEXT = "com.a3.yearlyprogress.widgets.ui.CalendarWidget.ACTION_NEXT"
    const val ACTION_PREV = "com.a3.yearlyprogress.widgets.ui.CalendarWidget.ACTION_PREV"
  }
}

class CalendarWidget : BaseWidget() {

  override fun onReceive(context: Context, intent: Intent) {
    super.onReceive(context, intent)

    if (intent.action == CalendarEventsSwiper.ACTION_NEXT ||
        intent.action == CalendarEventsSwiper.ACTION_PREV) {

      val widgetConfig = CalendarWidgetConfig.load(context)

      val selectedCalendars =
          widgetConfig.selectedCalendarIds
              ?: getCalendarsDetails(context.contentResolver).map { it.id }
      if (selectedCalendars.isEmpty()) {
        return
      }

      val events = mutableListOf<Event>()
      for (calendarId in selectedCalendars) {
        val event = getTodayOrNearestEvents(context.contentResolver, calendarId)
        events.addAll(event)
      }

      if (events.isEmpty()) {
        return
      }

      val swiper = CalendarEventsSwiper(context, events)
      if (intent.action == CalendarEventsSwiper.ACTION_NEXT) {
        swiper.next()
      } else {
        swiper.previous()
      }

      // Clearing any existing jobs.
      // It should help to avoid widget flickering.
      clearJob()
      val appWidgetManager = AppWidgetManager.getInstance(context)
      val componentName = ComponentName(context, CalendarWidget::class.java)
      appWidgetManager.getAppWidgetIds(componentName).forEach { appWidgetId ->
        updateWidget(context, appWidgetManager, appWidgetId)
      }
    }
  }

  private var updateJob: Job? = null

  override fun updateWidget(
      context: Context,
      appWidgetManager: AppWidgetManager,
      appWidgetId: Int
  ) {

    val smallView = RemoteViews(context.packageName, R.layout.calendar_widget_small_layout)
    val largeView = RemoteViews(context.packageName, R.layout.calendar_widget_layout)
    var widgetConfig = CalendarWidgetConfig.load(context)
    setWidgetBackgroundTransparency(smallView, widgetConfig)
    setWidgetBackgroundTransparency(largeView, widgetConfig)

    if (context.checkSelfPermission(Manifest.permission.READ_CALENDAR) ==
        PackageManager.PERMISSION_DENIED) {
      updateWidgetError(
          context,
          appWidgetId,
          context.getString(R.string.error),
          context.getString(R.string.calendar_permission_required),
          appWidgetManager,
          smallView,
          largeView,
      )
      return
    }

    updateJob =
        CoroutineScope(Dispatchers.IO).launch {
          var counter = 0
          while (isActive && counter < 5) { // Check if the coroutine is still active
            counter++

            widgetConfig = CalendarWidgetConfig.load(context)
            setWidgetBackgroundTransparency(smallView, widgetConfig)
            setWidgetBackgroundTransparency(largeView, widgetConfig)

            val selectedCalendars =
                widgetConfig.selectedCalendarIds
                    ?: getCalendarsDetails(context.contentResolver).map { it.id }
            if (selectedCalendars.isEmpty()) {
              updateWidgetError(
                  context,
                  appWidgetId,
                  context.getString(R.string.error),
                  context.getString(R.string.no_calendars_available),
                  appWidgetManager,
                  smallView,
                  largeView,
              )
              return@launch
            }

            val events = mutableListOf<Event>()
            for (calendarId in selectedCalendars) {
              val event = getTodayOrNearestEvents(context.contentResolver, calendarId)
              events.addAll(event)
            }

            /*Log.d("CalendarWidget","All Events: \n${
              events
                .sortedBy { it.eventStartTime }
                .joinToString("\n") { "${it.eventTitle.padEnd(20)}\t${it.eventStartTime}\t${it.eventEndTime}\t${calculateProgress(context, it.eventStartTime.time, it.eventEndTime.time)}" }
            }")

            Log.d("CalendarWidget","Filtered Events: \n${
              events
                .filter { it.eventEndTime.time > System.currentTimeMillis() }
                .sortedBy { it.eventStartTime }
                .joinToString("\n") { "${it.eventTitle.padEnd(20)}\t${it.eventStartTime}\t${it.eventEndTime}\t${calculateProgress(context, it.eventStartTime.time, it.eventEndTime.time)}\t${it.eventDescription.length}" }
            }")*/

            if (events.isEmpty()) {
              updateWidgetError(
                  context,
                  appWidgetId,
                  context.getString(R.string.error),
                  context.getString(R.string.no_upcoming_events),
                  appWidgetManager,
                  smallView,
                  largeView,
              )
              return@launch
            }

            val calendarEventsSwiper = CalendarEventsSwiper(context, events)
            val event = calendarEventsSwiper.current()

            if (event == null) {
              updateWidgetError(
                  context,
                  appWidgetId,
                  context.getString(R.string.error),
                  context.getString(R.string.no_upcoming_events),
                  appWidgetManager,
                  smallView,
                  largeView,
              )
              continue
            }

            setupCalendarWidgetView(context, smallView, event, widgetConfig)
            smallView.setViewVisibility(R.id.event_description, android.view.View.GONE)
            smallView.setTextViewText(R.id.indicator, calendarEventsSwiper.indicator())
            setupCalendarWidgetView(context, largeView, event, widgetConfig)
            largeView.setTextViewText(R.id.indicator, calendarEventsSwiper.indicator())

            val view = mapRemoteView(context, appWidgetId, smallView, largeView)
            appWidgetManager.updateAppWidget(appWidgetId, view)
            delay(900)
          }
        }
  }

  fun mapRemoteView(
      context: Context,
      widgetId: Int,
      smallView: RemoteViews,
      largeViews: RemoteViews
  ): RemoteViews {
    if (Build.VERSION.SDK_INT > 30) {
      val viewMapping: Map<SizeF, RemoteViews> =
          mapOf(
              SizeF(60f, 140f) to smallView,
              SizeF(130f, 140f) to largeViews,
          )
      return RemoteViews(viewMapping)
    }

    val option = AppWidgetManager.getInstance(context).getAppWidgetOptions(widgetId)
    val height = option.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT)
    val width = option.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH)

    if (height >= 100 && width >= 100) {
      return largeViews
    }

    return smallView
  }

  fun setupCalendarWidgetView(
      context: Context,
      view: RemoteViews,
      event: Event,
      widgetConfig: CalendarWidgetConfig
  ) {
    val yp = YearlyProgressUtil(context)
    val progress = yp.calculateProgress(event.eventStartTime.time, event.eventEndTime.time)
    view.setTextViewText(R.id.event_title, event.eventTitle)
    view.setTextViewText(R.id.event_description, event.eventDescription)
    view.setTextViewText(
        R.id.event_duration,
        "${event.eventStartTime.formattedDateTime(context)} — ${
        event.eventEndTime.formattedDateTime(
          context
        )
      }")
    view.setProgressBar(R.id.widgetProgressBar, 100, progress.roundToInt(), false)

    val widgetDays =
        if (System.currentTimeMillis() < event.eventStartTime.time) {
          context.getString(
              R.string.time_in,
              (event.eventStartTime.time - System.currentTimeMillis()).toTimePeriodText(
                  widgetConfig.dynamicLeftCounter))
        } else {
          context.getString(
              R.string.time_left,
              yp.calculateTimeLeft(event.eventEndTime.time)
                  .toTimePeriodText(widgetConfig.dynamicLeftCounter))
        }
    if (widgetConfig.timeLeftCounter && widgetConfig.replaceProgressWithDaysLeft) {
      view.setTextViewText(R.id.widgetDays, "")
      view.setViewVisibility(R.id.widgetDays, View.GONE)
      view.setTextViewText(R.id.widgetProgress, widgetDays)
    } else {
      view.setTextViewText(R.id.widgetDays, widgetDays)
      view.setViewVisibility(R.id.widgetDays, View.VISIBLE)
      view.setTextViewText(
          R.id.widgetProgress,
          progress.styleFormatted(widgetConfig.decimalPlaces),
      )
    }

    if (System.currentTimeMillis() in event.eventStartTime.time..event.eventEndTime.time) {
      view.setViewVisibility(R.id.widgetProgressBar, android.view.View.VISIBLE)
      view.setTextViewText(R.id.event_status, context.getString(R.string.ongoing))
    } else {
      view.setViewVisibility(R.id.widgetProgressBar, android.view.View.GONE)
      view.setTextViewText(R.id.event_status, context.getString(R.string.upcoming))
    }

    view.setViewVisibility(R.id.event_title, android.view.View.VISIBLE)
    if (event.eventDescription.isNotEmpty() && event.eventDescription.isNotBlank()) {
      view.setViewVisibility(R.id.event_description, android.view.View.VISIBLE)
    } else {
      view.setViewVisibility(R.id.event_description, android.view.View.GONE)
    }

    val nextIntent =
        Intent(context, CalendarWidget::class.java).apply {
          action = CalendarEventsSwiper.ACTION_NEXT
        }
    val prevIntent =
        Intent(context, CalendarWidget::class.java).apply {
          action = CalendarEventsSwiper.ACTION_PREV
        }

    val nextPendingIntent =
        PendingIntent.getBroadcast(
            context,
            0,
            nextIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    val prevPendingIntent =
        PendingIntent.getBroadcast(
            context,
            1,
            prevIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

    view.setOnClickPendingIntent(R.id.next_btn, nextPendingIntent)
    view.setOnClickPendingIntent(R.id.prev_btn, prevPendingIntent)
  }

  private fun emptyWidget(view: RemoteViews) {
    view.setTextViewText(R.id.event_status, "")
    view.setTextViewText(R.id.event_title, "")
    view.setTextViewText(R.id.event_description, "")
    view.setTextViewText(R.id.event_duration, "")
    view.setTextViewText(R.id.widgetProgress, "")
    view.setTextViewText(R.id.widgetDays, "")
    view.setProgressBar(R.id.widgetProgressBar, 100, 0, false)
    view.setViewVisibility(R.id.event_description, android.view.View.GONE)
    view.setViewVisibility(R.id.event_title, android.view.View.GONE)
    view.setViewVisibility(R.id.widgetProgressBar, android.view.View.GONE)
    view.setTextViewText(R.id.indicator, "")
  }

  private fun updateWidgetError(
      context: Context,
      appWidgetId: Int,
      status: String,
      description: String,
      appWidgetManager: AppWidgetManager,
      smallView: RemoteViews,
      largeView: RemoteViews,
  ) {
    emptyWidget(smallView)
    emptyWidget(largeView)
    smallView.setTextViewText(R.id.event_status, status)
    largeView.setTextViewText(R.id.event_status, status)
    smallView.setViewVisibility(R.id.event_description, android.view.View.VISIBLE)
    largeView.setViewVisibility(R.id.event_description, android.view.View.VISIBLE)
    smallView.setTextViewText(R.id.event_description, description)
    largeView.setTextViewText(R.id.event_description, description)
    val view = mapRemoteView(context, appWidgetId, smallView, largeView)
    appWidgetManager.updateAppWidget(appWidgetId, view)
  }

  private fun setWidgetBackgroundTransparency(
      view: RemoteViews,
      widgetConfig: CalendarWidgetConfig
  ) {
    val widgetBackgroundAlpha = ((widgetConfig.backgroundTransparency) * 255).toInt()
    view.setInt(R.id.widgetContainer, "setImageAlpha", widgetBackgroundAlpha)
  }

  private fun Date.formattedDateTime(context: Context): String {
    return android.text.format.DateFormat.format(
            if (android.text.format.DateFormat.is24HourFormat(context)) "MMM dd, yyyy · HH:mm"
            else "MMM dd, yyyy · hh:mm a",
            this,
        )
        .toString()
        .uppercase()
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
