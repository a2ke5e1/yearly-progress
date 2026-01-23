package com.a3.yearlyprogess.feature.widgets.ui

import android.Manifest
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.CalendarContract
import android.text.SpannableString
import android.text.format.DateFormat
import android.util.SizeF
import android.view.View
import android.widget.RemoteViews
import androidx.core.database.getLongOrNull
import androidx.core.database.getStringOrNull
import com.a3.yearlyprogess.R
import com.a3.yearlyprogess.core.domain.repository.AppSettingsRepository
import com.a3.yearlyprogess.core.util.Log
import com.a3.yearlyprogess.core.util.YearlyProgressUtil
import com.a3.yearlyprogess.core.util.formatEventDateTime
import com.a3.yearlyprogess.core.util.formatEventTimeStatus
import com.a3.yearlyprogess.core.util.styleFormatted
import com.a3.yearlyprogess.feature.events.domain.model.Event
import com.a3.yearlyprogess.feature.widgets.domain.model.CalendarWidgetOptions
import com.a3.yearlyprogess.feature.widgets.domain.model.WidgetColors
import com.a3.yearlyprogess.feature.widgets.domain.model.WidgetTheme
import com.a3.yearlyprogess.feature.widgets.domain.repository.CalendarWidgetOptionsRepository
import com.a3.yearlyprogess.feature.widgets.util.WidgetRenderer
import com.a3.yearlyprogess.feature.widgets.util.WidgetRenderer.applyTextViewTextSize
import com.a3.yearlyprogess.feature.widgets.util.WidgetSwiper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.util.Calendar
import java.util.Date
import javax.inject.Inject
import kotlin.math.roundToInt

/**
 * Calendar Widget that displays today's or upcoming calendar events
 * Uses WidgetSwiper for event navigation and follows EventWidget architecture
 */
@AndroidEntryPoint
class CalendarWidget : BaseWidget() {

    @Inject
    lateinit var calendarWidgetOptionsRepository: CalendarWidgetOptionsRepository

    @Inject
    lateinit var appSettingsRepository: AppSettingsRepository

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        val appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1)
        if (appWidgetId == -1) return

        when (intent.action) {
            ACTION_NEXT, ACTION_PREV -> {
                Log.d("CalendarWidget", "Received swiper action: ${intent.action}")

                val (userConfig, progressSettings) = runBlocking(Dispatchers.IO) {
                    val options = calendarWidgetOptionsRepository.getOptions(appWidgetId).first()
                    val appSettings = appSettingsRepository.appSettings.first()

                    // If theme is null, get it from AppSettings
                    if (options.theme == null) {
                        calendarWidgetOptionsRepository.updateOptions(appWidgetId, options.copy(
                            theme = appSettings.appTheme
                        ))
                        Pair(options.copy(theme = appSettings.appTheme), appSettings.progressSettings)
                    } else {
                        Pair(options, appSettings.progressSettings)
                    }

                }

                val events = getCalendarEvents(context, userConfig.selectedCalendarIds)

                if (events.isNotEmpty()) {
                    val swiper = WidgetSwiper.forCalendar(
                        context = context,
                        events = events,
                        widgetId = appWidgetId,
                        widgetTheme = userConfig.theme ?: WidgetTheme.DEFAULT
                    )

                    when (intent.action) {
                        ACTION_NEXT -> swiper.next()
                        ACTION_PREV -> swiper.previous()
                    }

                    updateWidget(context, appWidgetId)
                }
            }
        }
    }

    override fun updateWidget(context: Context, appWidgetId: Int): RemoteViews {
        val manager = AppWidgetManager.getInstance(context)
        val options = manager.getAppWidgetOptions(appWidgetId)

        val (userConfig, progressSettings) = runBlocking(Dispatchers.IO) {
            val options = calendarWidgetOptionsRepository.getOptions(appWidgetId).first()
            val appSettings = appSettingsRepository.appSettings.first()

            // If theme is null, get it from AppSettings
            if (options.theme == null) {
                calendarWidgetOptionsRepository.updateOptions(appWidgetId, options.copy(
                    theme = appSettings.appTheme
                ))
                Pair(options.copy(theme = appSettings.appTheme), appSettings.progressSettings)
            } else {
                Pair(options, appSettings.progressSettings)
            }
        }
        val yp = YearlyProgressUtil(progressSettings)


        // Check calendar permission
        if (context.checkSelfPermission(Manifest.permission.READ_CALENDAR) !=
            PackageManager.PERMISSION_GRANTED) {
            return WidgetRenderer.errorWidgetRemoteView(
                context,
                "Calendar Permission Required",
                userConfig.theme ?: WidgetTheme.DEFAULT,
            )
        }

        val events = getCalendarEvents(context, userConfig.selectedCalendarIds)

        if (events.isEmpty()) {
            return emptyCalendarRemoteView(context, userConfig.theme ?: WidgetTheme.DEFAULT)
        }

        // Use WidgetSwiper to get current event
        val swiper = WidgetSwiper.forCalendar(
            context = context,
            events = events,
            widgetId = appWidgetId,
            widgetTheme = userConfig.theme ?: WidgetTheme.DEFAULT
        )

        val event = swiper.current()

        if (event == null) {
            return emptyCalendarRemoteView(context, userConfig.theme ?: WidgetTheme.DEFAULT)
        }

        val indicator = swiper.indicator()

        return responsiveRemoteView(
            context,
            event,
            yp,
            userConfig.theme ?: WidgetTheme.DEFAULT,
            userConfig,
            options,
            appWidgetId,
            indicator
        )
    }

    companion object {
        const val ACTION_NEXT = "com.a3.yearlyprogess.feature.widgets.ui.CalendarWidget.ACTION_NEXT"
        const val ACTION_PREV = "com.a3.yearlyprogess.feature.widgets.ui.CalendarWidget.ACTION_PREV"

        /**
         * Retrieves calendar events from the system calendar and converts them to Event objects
         * Filters to show only future/ongoing events in the next 30 days
         */
        private fun getCalendarEvents(
            context: Context,
            selectedCalendarIds: Set<Long>
        ): List<Event> {
            if (context.checkSelfPermission(Manifest.permission.READ_CALENDAR) !=
                PackageManager.PERMISSION_GRANTED) {
                return emptyList()
            }

            val events = mutableListOf<Event>()

            // Get today's date range
            val now = System.currentTimeMillis()
            val calendar = Calendar.getInstance().apply {
                timeInMillis = now
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val startOfDay = calendar.timeInMillis
            calendar.add(Calendar.DAY_OF_MONTH, 30) // Look ahead 30 days
            val endOfRange = calendar.timeInMillis

            // Build URI for calendar instances
            val uri = CalendarContract.Instances.CONTENT_URI.buildUpon()
                .appendPath(startOfDay.toString())
                .appendPath(endOfRange.toString())
                .build()

            val projection = arrayOf(
                CalendarContract.Instances.EVENT_ID,
                CalendarContract.Instances.TITLE,
                CalendarContract.Instances.DESCRIPTION,
                CalendarContract.Instances.BEGIN,
                CalendarContract.Instances.END,
                CalendarContract.Instances.ALL_DAY,
                CalendarContract.Instances.CALENDAR_ID
            )

            // Build selection based on selected calendars
            val selection: String?
            val selectionArgs: Array<String>?

            if (selectedCalendarIds.isEmpty()) {
                // No filter - show all calendars
                selection = null
                selectionArgs = null
            } else {
                // Filter by selected calendar IDs
                val placeholders = selectedCalendarIds.joinToString(",") { "?" }
                selection = "${CalendarContract.Instances.CALENDAR_ID} IN ($placeholders)"
                selectionArgs = selectedCalendarIds.map { it.toString() }.toTypedArray()
            }

            try {
                context.contentResolver.query(
                    uri,
                    projection,
                    selection,
                    selectionArgs,
                    "${CalendarContract.Instances.BEGIN} ASC"
                )?.use { cursor ->
                    val idCol = cursor.getColumnIndex(CalendarContract.Instances.EVENT_ID)
                    val titleCol = cursor.getColumnIndex(CalendarContract.Instances.TITLE)
                    val descCol = cursor.getColumnIndex(CalendarContract.Instances.DESCRIPTION)
                    val startCol = cursor.getColumnIndex(CalendarContract.Instances.BEGIN)
                    val endCol = cursor.getColumnIndex(CalendarContract.Instances.END)
                    val allDayCol = cursor.getColumnIndex(CalendarContract.Instances.ALL_DAY)

                    var eventId = 0
                    while (cursor.moveToNext()) {
                        try {
                            val title = cursor.getStringOrNull(titleCol) ?: "Untitled Event"
                            val description = cursor.getStringOrNull(descCol).orEmpty()
                            val startTime = cursor.getLongOrNull(startCol) ?: continue
                            val endTime = cursor.getLongOrNull(endCol) ?: continue
                            val isAllDay = cursor.getInt(allDayCol) == 1

                            // Only include future or ongoing events
                            if (endTime > now) {
                                events.add(
                                    Event(
                                        id = eventId++, // Use incrementing ID for temporary events
                                        eventTitle = title,
                                        eventDescription = description,
                                        eventStartTime = Date(startTime),
                                        eventEndTime = Date(endTime),
                                        allDayEvent = isAllDay,
                                        backgroundImageUri = null // Calendar events don't have images
                                    )
                                )
                            }
                        } catch (e: Exception) {
                            Log.e("CalendarWidget", "Failed to parse event", e)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("CalendarWidget", "Failed to query calendar", e)
            }

            // Filter and sort: only future events, sorted by start time, limited to 5
            return events
                .filter { it.eventEndTime.time > now }
                .sortedBy { it.eventStartTime }
                .take(5)
        }

        private fun applyTheme(views: RemoteViews, colors: WidgetColors) {
            views.setTextColor(R.id.event_title, colors.primaryColor)
            views.setTextColor(R.id.widgetProgress, colors.primaryColor)
            views.setTextColor(R.id.event_description, colors.secondaryColor)
            views.setTextColor(R.id.currentDate, colors.secondaryColor)
            views.setTextColor(R.id.eventTime, colors.secondaryColor)
            views.setTextColor(R.id.widgetDays, colors.secondaryColor)
            views.setTextColor(R.id.event_status, colors.accentColor)
        }

        private fun applyText(
            context: Context,
            views: RemoteViews,
            event: Event,
            styledProgressBar: SpannableString,
            timeStatusText: String,
            eventDateText: String,
            currentDate: SpannableString,
            indicator: SpannableString,
            replaceProgress: Boolean
        ) {
            views.setTextViewText(R.id.event_title, event.eventTitle)
            views.setTextViewText(R.id.event_description, event.eventDescription)

            if (replaceProgress) {
                views.setTextViewText(R.id.widgetProgress, timeStatusText)
                views.setViewVisibility(R.id.widgetDays, View.GONE)
            } else {
                views.setTextViewText(R.id.widgetProgress, styledProgressBar)
                views.setTextViewText(R.id.widgetDays, timeStatusText)
                views.setViewVisibility(R.id.widgetDays, View.VISIBLE)
            }

            views.setTextViewText(R.id.event_duration, eventDateText)
            views.setTextViewText(R.id.currentDate, currentDate)
            views.setTextViewText(R.id.indicator, indicator)

            if (event.eventDescription.isEmpty()) {
                views.setViewVisibility(R.id.event_description, View.GONE)
            } else {
                views.setViewVisibility(R.id.event_description, View.VISIBLE)
            }

            if (System.currentTimeMillis() in event.eventStartTime.time..event.eventEndTime.time) {
                views.setViewVisibility(R.id.widgetProgressBar, android.view.View.VISIBLE)
                views.setTextViewText(R.id.event_status, context.getString(R.string.ongoing))
            } else {
                views.setViewVisibility(R.id.widgetProgressBar, android.view.View.GONE)
                views.setTextViewText(R.id.event_status, context.getString(R.string.upcoming))
            }
        }

        private fun applySwiperActions(
            views: RemoteViews,
            context: Context,
            appWidgetId: Int
        ) {
            val nextIntent = Intent(context, CalendarWidget::class.java).apply {
                action = ACTION_NEXT
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            }
            val prevIntent = Intent(context, CalendarWidget::class.java).apply {
                action = ACTION_PREV
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            }

            val nextPendingIntent = PendingIntent.getBroadcast(
                context,
                appWidgetId * 100 + 1,
                nextIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            val prevPendingIntent = PendingIntent.getBroadcast(
                context,
                appWidgetId * 100 + 2,
                prevIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            views.setOnClickPendingIntent(R.id.next_btn, nextPendingIntent)
            views.setOnClickPendingIntent(R.id.prev_btn, prevPendingIntent)
        }

        private fun calculateEventData(
            context: Context,
            event: Event,
            yp: YearlyProgressUtil,
            userConfig: CalendarWidgetOptions
        ): EventDisplayData {
            val (startTime, endTime) = event.nextStartAndEndTime()
            val progress = yp.calculateProgress(startTime, endTime)

            val styledProgressBar = progress.styleFormatted(userConfig.decimalDigits)

            val timeStatusText = if (userConfig.timeStatusCounter) {
                formatEventTimeStatus(
                    context = context,
                    startTime, endTime,
                    userConfig.dynamicTimeStatusCounter
                )
            } else {
                ""
            }

            val eventDateText = formatEventDateTime(
                context = context,
                startTime = startTime,
                endTime = endTime,
                allDayEvent = event.allDayEvent,
            )

            val currentDate = SpannableString(
                DateFormat.format("EEE, MMM dd", Date()).toString()
            )

            return EventDisplayData(
                progress = progress,
                styledProgressBar = styledProgressBar,
                timeStatusText = timeStatusText,
                eventDateText = eventDateText,
                currentDate = currentDate
            )
        }

        fun responsiveRemoteView(
            context: Context,
            event: Event,
            yp: YearlyProgressUtil,
            theme: WidgetTheme,
            userConfig: CalendarWidgetOptions,
            options: Bundle? = null,
            appWidgetId: Int = -1,
            indicator: SpannableString = SpannableString("")
        ): RemoteViews {
            val small = RemoteViews(context.packageName, R.layout.calendar_widget_small_layout)
//            val tall = RemoteViews(context.packageName, R.layout.calendar_widget_tallview)
            val wide = RemoteViews(context.packageName, R.layout. calendar_widget_layout)

            val eventData = calculateEventData(context, event, yp, userConfig)
            val colors = WidgetColors.fromTheme(context, theme)

            // Apply theme to all layouts
            applyTheme(small, colors)
//            applyTheme(tall, colors)
            applyTheme(wide, colors)

            // Apply background colors
            small.setInt(R.id.widgetContainer, "setColorFilter", colors.backgroundColor)
//            tall.setInt(R.id.widgetContainer, "setColorFilter", colors.backgroundColor)
            wide.setInt(R.id.widgetContainer, "setColorFilter", colors.backgroundColor)

            // Apply background transparency
            val alpha = ((userConfig.backgroundTransparency / 100.0) * 255).toInt()
            small.setInt(R.id.widgetContainer, "setImageAlpha", alpha)
//            tall.setInt(R.id.widgetContainer, "setImageAlpha", alpha)
            wide.setInt(R.id.widgetContainer, "setImageAlpha", alpha)

            // Apply progress bars
            WidgetRenderer.applyLinearProgressBar(small, eventData.progress.roundToInt(), theme)
//            WidgetProgressRenderer.applyLinearProgressBar(tall, eventData.progress.roundToInt(), theme)
            WidgetRenderer.applyLinearProgressBar(wide, eventData.progress.roundToInt(), theme)

            // Apply text content to all layouts
            applyText(
                context,small, event, eventData.styledProgressBar, eventData.timeStatusText,
                eventData.eventDateText, eventData.currentDate, indicator,
                userConfig.replaceProgressWithTimeLeft
            )
//            applyText(
//                tall, event, eventData.styledProgressBar, eventData.timeStatusText,
//                eventData.eventDateText, eventData.currentDate, indicator,
//                userConfig.replaceProgressWithTimeLeft
//            )
            applyText(
                context,wide, event, eventData.styledProgressBar, eventData.timeStatusText,
                eventData.eventDateText, eventData.currentDate, indicator,
                userConfig.replaceProgressWithTimeLeft
            )

            // Apply swiper actions if appWidgetId is valid
            if (appWidgetId != -1) {
                applySwiperActions(small, context, appWidgetId)
//                applySwiperActions(tall, context, appWidgetId)
                applySwiperActions(wide, context, appWidgetId)
            }

            // Apply font scaling (similar to EventWidget)
            applyFontScaling(small, userConfig.fontScale, context, "small")
//            applyFontScaling(tall, userConfig.fontScale, context, "tall")
            applyFontScaling(wide, userConfig.fontScale, context, "wide")

            // Return appropriate layout based on Android version
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                return RemoteViews(
                    mapOf(
                        SizeF(60f, 140f) to small,
//                        SizeF(130f, 140f) to tall,
                        SizeF(200f, 140f) to wide
                    )
                )
            } else {
                val minWidth = options?.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH) ?: 0
                val minHeight = options?.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT) ?: 0

                return when {
                    minWidth >= 200 && minHeight >= 100 -> wide
//                    minWidth >= 130 && minHeight >= 140 -> tall
                    else -> small
                }
            }
        }

        private fun applyFontScaling(
            views: RemoteViews,
            fontScale: Float,
            context: Context,
            layout: String
        ) {
            when (layout) {
                "small" -> {
                    views.applyTextViewTextSize(context, R.id.event_title, R.dimen.calendar_widget_small_event_title, fontScale)
                    views.applyTextViewTextSize(context, R.id.widgetDays, R.dimen.calendar_widget_small_event_days_left, fontScale)
                    views.applyTextViewTextSize(context, R.id.widgetProgress, R.dimen.calendar_widget_small_event_progress, fontScale)
                    views.applyTextViewTextSize(context, R.id.currentDate, R.dimen.calendar_widget_small_event_duration, fontScale)
                }
//                "tall" -> {
//                    views.applyTextViewTextSize(context, R.id.event_title, R.dimen.calendar_widget_tallview_title, fontScale)
//                    views.applyTextViewTextSize(context, R.id.event_description, R.dimen.calendar_widget_tallview_description, fontScale)
//                    views.applyTextViewTextSize(context, R.id.eventTime, R.dimen.calendar_widget_tallview_time, fontScale)
//                    views.applyTextViewTextSize(context, R.id.widgetDays, R.dimen.calendar_widget_tallview_days_left, fontScale)
//                    views.applyTextViewTextSize(context, R.id.widgetProgress, R.dimen.calendar_widget_tallview_progress, fontScale)
//                    views.applyTextViewTextSize(context, R.id.currentDate, R.dimen.calendar_widget_tallview_current_date, fontScale)
//                }
                "wide" -> {
                    views.applyTextViewTextSize(context, R.id.event_title, R.dimen.calendar_widget_event_title, fontScale)
                    views.applyTextViewTextSize(context, R.id.event_description, R.dimen.calendar_widget_event_description, fontScale)
                    views.applyTextViewTextSize(context, R.id.eventTime, R.dimen.calendar_widget_event_duration, fontScale)
                    views.applyTextViewTextSize(context, R.id.widgetDays, R.dimen.calendar_widget_event_days_left, fontScale)
                    views.applyTextViewTextSize(context, R.id.widgetProgress, R.dimen.calendar_widget_event_progress, fontScale)
                    views.applyTextViewTextSize(context, R.id.currentDate, R.dimen.calendar_widget_event_duration, fontScale)
                }
            }
        }

        fun emptyCalendarRemoteView(context: Context, theme: WidgetTheme): RemoteViews {
            val views = RemoteViews(context.packageName, R.layout.error_widget)
            val colors = WidgetColors.fromTheme(context, theme)
            views.setTextColor(R.id.error_text, colors.primaryColor)
            views.setTextViewText(R.id.error_text, "No Events")
            views.setInt(R.id.widgetContainer, "setColorFilter", colors.backgroundColor)
            return views
        }
    }

    data class EventDisplayData(
        val progress: Double,
        val styledProgressBar: SpannableString,
        val timeStatusText: String,
        val eventDateText: String,
        val currentDate: SpannableString
    )
}