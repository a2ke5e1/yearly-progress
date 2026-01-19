package com.a3.yearlyprogess.feature.widgets.ui

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.SpannableString
import android.util.SizeF
import android.view.View
import android.widget.RemoteViews
import androidx.core.graphics.ColorUtils
import com.a3.yearlyprogess.R
import com.a3.yearlyprogess.core.util.Log
import com.a3.yearlyprogess.core.util.TimePeriod
import com.a3.yearlyprogess.core.util.YearlyProgressUtil
import com.a3.yearlyprogess.core.util.YearlyProgressUtil.Companion.toFormattedTimePeriod
import com.a3.yearlyprogess.core.util.formatEventDateTime
import com.a3.yearlyprogess.core.util.formatEventTimeStatus
import com.a3.yearlyprogess.core.util.styleFormatted
import com.a3.yearlyprogess.feature.events.domain.model.Event
import com.a3.yearlyprogess.feature.events.domain.repository.EventRepository
import com.a3.yearlyprogess.core.util.loadBitmapOptimizedForWidget
import com.a3.yearlyprogess.feature.widgets.domain.model.EventWidgetOptions
import com.a3.yearlyprogess.feature.widgets.domain.model.WidgetColors
import com.a3.yearlyprogess.feature.widgets.domain.model.WidgetTheme
import com.a3.yearlyprogess.feature.widgets.domain.repository.EventWidgetOptionsRepository
import com.a3.yearlyprogess.feature.widgets.util.WidgetRenderer
import com.a3.yearlyprogess.feature.widgets.util.WidgetRenderer.applyTextViewTextSize
import com.a3.yearlyprogess.feature.widgets.util.WidgetSwiper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.math.roundToInt
import androidx.core.graphics.get
import com.a3.yearlyprogess.core.domain.repository.AppSettingsRepository


@AndroidEntryPoint
class EventWidget : BaseWidget() {

    @Inject
    lateinit var eventWidgetOptionsRepository: EventWidgetOptionsRepository

    @Inject
    lateinit var eventRepository: EventRepository

    @Inject
    lateinit var appSettingsRepository: AppSettingsRepository

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        val appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1)
        if (appWidgetId == -1) return

        when (intent.action) {
            ACTION_NEXT, ACTION_PREV -> {
                Log.d("EventWidget", "Received swiper action: ${intent.action}")

                val events = runBlocking(Dispatchers.IO) {
                    val userConfig = eventWidgetOptionsRepository.getOptions(appWidgetId).first()
                    userConfig.selectedEventIds.mapNotNull {
                        eventRepository.getEvent(it)
                    }
                }

                if (events.isNotEmpty()) {
                    val swiper = WidgetSwiper.forEvents(context, events, appWidgetId)

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
        val yp = YearlyProgressUtil()
        val manager = AppWidgetManager.getInstance(context)
        val options = manager.getAppWidgetOptions(appWidgetId)

        val (userConfig, events) = runBlocking {
            withContext(Dispatchers.IO) {
                var config = eventWidgetOptionsRepository
                    .getOptions(appWidgetId)
                    .first()


                config = if (config.theme == null) {
                    val appSettings = appSettingsRepository.appSettings.first()
                    eventWidgetOptionsRepository.updateTheme(appWidgetId, appSettings.appTheme)
                    config.copy(theme = appSettings.appTheme)
                } else {
                    config
                }


                val events = config.selectedEventIds.mapNotNull { id ->
                    eventRepository.getEvent(id)
                }

                config to events
            }
        }

        Log.d("EventWidget", "Selected Event Ids ${userConfig.selectedEventIds}")
        Log.d("EventWidget", "Selected Events ${events}")

        // Use swiper to get current event
        val swiper = WidgetSwiper.forEvents(
            context = context,
            events = events,
            widgetId = appWidgetId,
            widgetTheme = userConfig.theme ?: WidgetTheme.DEFAULT
        )
        val event = swiper.current()

        val theme: WidgetTheme = userConfig.theme ?: WidgetTheme.DEFAULT
        val indicator = swiper.indicator()

        if (event == null) {
            return emptyEventRemoteView(context, theme)
        }

        val views = responsiveRemoteView(
            context,
            event,
            yp,
            theme,
            userConfig,
            options,
            appWidgetId,
            indicator
        )
        return views
    }

    companion object {
        const val ACTION_NEXT = "com.a3.yearlyprogess.feature.widgets.ui.EventWidget.ACTION_NEXT"
        const val ACTION_PREV = "com.a3.yearlyprogess.feature.widgets.ui.EventWidget.ACTION_PREV"

        private fun applyTheme(
            views: RemoteViews,
            colors: WidgetColors,
            backgroundLuminance: Double?,
            transparency: Int
        ) {
            views.setTextColor(R.id.eventTitle, colors.primaryColor)
            views.setTextColor(R.id.eventProgressText, colors.primaryColor)
            views.setTextColor(R.id.widgetDaysLeft, colors.accentColor)

            // Apply background color with transparency
            val alpha = ((transparency / 100.0) * 255).toInt().coerceIn(0, 255)

            if (backgroundLuminance == null) {
                views.setTextColor(R.id.eventDesc, colors.secondaryColor)
                views.setTextColor(R.id.currentDate, colors.secondaryColor)
                views.setTextColor(R.id.eventTime, colors.secondaryColor)
                views.setInt(R.id.widgetContainer, "setImageAlpha", alpha)
                views.setViewVisibility(R.id.widgetContainer, View.VISIBLE)
                views.setViewVisibility(R.id.imageContainer, View.GONE)
                return
            }


            // For RemoteViews, we use setInt with "setImageAlpha" or setInt with "setBackgroundColor"
            // But since widgetContainer is an ImageView with a src, we need to set the image alpha or tint

            views.setInt(R.id.imageContainer, "setImageAlpha", alpha)
            views.setViewVisibility(R.id.widgetContainer, View.GONE)
            views.setViewVisibility(R.id.imageContainer, View.VISIBLE)

            val blendedColor =
                if (backgroundLuminance < 0.45) {
                    // Dark image → brighten text
                    ColorUtils.blendARGB(colors.secondaryColor, Color.WHITE, 0.65f)
                } else {
                    // Light image → darken text
                    ColorUtils.blendARGB(colors.secondaryColor, Color.BLACK, 0.45f)
                }

            views.setTextColor(R.id.eventDesc, blendedColor)
            views.setTextColor(R.id.currentDate, blendedColor)
            views.setTextColor(R.id.eventTime, blendedColor)
        }


        private fun applyText(
            views: RemoteViews,
            userConfig: EventWidgetOptions,
            event: Event,
            styledProgressBar: SpannableString,
            timeStatusText: String,
            eventDateText: String,
            currentDate: SpannableString,
            indicator: SpannableString
        ) {
            views.setTextViewText(R.id.eventTitle, event.eventTitle)
            views.setTextViewText(R.id.eventDesc, event.eventDescription)
            views.setTextViewText(R.id.eventProgressText, styledProgressBar)
            views.setTextViewText(R.id.widgetDaysLeft, timeStatusText)
            views.setTextViewText(R.id.eventTime, eventDateText)
            views.setTextViewText(R.id.currentDate, currentDate)
            views.setTextViewText(R.id.indicator, indicator)

            if (event.eventDescription.isEmpty()) {
                views.setViewVisibility(R.id.eventDesc, View.GONE)
            } else {
                views.setViewVisibility(R.id.eventDesc, View.VISIBLE)
            }

            if (userConfig.timeStatusCounter && userConfig.replaceProgressWithTimeLeft) {
                views.setViewVisibility(R.id.widgetDaysLeft, View.GONE)
                views.setTextViewText(R.id.eventProgressText, timeStatusText)
            } else {
                views.setViewVisibility(R.id.widgetDaysLeft, View.VISIBLE)
            }

        }

        private fun calculateAverageLuminance(
            bitmap: Bitmap,
            step: Int = 8
        ): Double {
            var sum = 0.0
            var count = 0

            var x = 0
            while (x < bitmap.width) {
                var y = 0
                while (y < bitmap.height) {
                    sum += ColorUtils.calculateLuminance(bitmap[x, y])
                    count++
                    y += step
                }
                x += step
            }
            return if (count == 0) 0.0 else sum / count
        }

        /**
        *  Returns bitmaps average luminance
        */
        private fun applyEventImage(
            context: Context,
            views: RemoteViews,
            event: Event
        ): Double? {

            val bitmap = if (event.backgroundImageUri != null) {
                try {
                    loadBitmapOptimizedForWidget(context, event.backgroundImageUri)
                } catch (e: Exception) {
                    null
                }
            } else null

            if (bitmap != null) {
                views.setBitmap(R.id.imageContainer, "setImageBitmap", bitmap)
                views.setViewVisibility(R.id.imageContainer, View.VISIBLE)
                views.setViewVisibility(R.id.widgetContainer, View.GONE)

                return calculateAverageLuminance(bitmap)
            } else {
                views.setViewVisibility(R.id.imageContainer, View.GONE)
                views.setViewVisibility(R.id.widgetContainer, View.VISIBLE)
                return null
            }
        }

        private fun applySwiperActions(
            views: RemoteViews,
            context: Context,
            appWidgetId: Int
        ) {
            val nextIntent = Intent(context, EventWidget::class.java).apply {
                action = ACTION_NEXT
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            }
            val prevIntent = Intent(context, EventWidget::class.java).apply {
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
            userConfig: EventWidgetOptions
        ): EventDisplayData {
            val (startTime, endTime) = event.nextStartAndEndTime()
            val progress = yp.calculateProgress(startTime, endTime)
            val styledProgressBar = progress.styleFormatted(userConfig.decimalDigits)
            val timeStatusText = formatEventTimeStatus(
                context,
                startTime,
                endTime,
                userConfig.dynamicTimeStatusCounter
            )
            val eventDateText = formatEventDateTime(
                context,
                startTime,
                endTime,
                event.allDayEvent
            )
            val currentDate = yp.getCurrentPeriodValue(TimePeriod.DAY)
                .toFormattedTimePeriod(yp, TimePeriod.DAY)

            return EventDisplayData(
                progress = progress,
                styledProgressBar = styledProgressBar,
                timeStatusText = timeStatusText,
                eventDateText = eventDateText,
                currentDate = currentDate
            )
        }

        /**
         * Creates a responsive event widget that adapts to different sizes
         * Uses Android 12+ size-based rendering or falls back to dimension-based logic
         */
        fun responsiveRemoteView(
            context: Context,
            event: Event,
            yp: YearlyProgressUtil,
            theme: WidgetTheme,
            userConfig: EventWidgetOptions,
            options: Bundle? = null,
            appWidgetId: Int = -1,
            indicator: SpannableString = SpannableString("")
        ): RemoteViews {
            val small = RemoteViews(context.packageName, R.layout.event_widget_small)
            val tall = RemoteViews(context.packageName, R.layout.event_widget_tallview)
            val wide = RemoteViews(context.packageName, R.layout.event_widget_wideview)

            val eventData = calculateEventData(context, event, yp, userConfig)
            val colors = WidgetColors.fromTheme(context, theme)


            // Apply progress bars
            WidgetRenderer.applyLinearProgressBar(small, eventData.progress.roundToInt(), theme)
            WidgetRenderer.applyLinearProgressBar(tall, eventData.progress.roundToInt(), theme)
            WidgetRenderer.applyLinearProgressBar(wide, eventData.progress.roundToInt(), theme)

            // Apply text content to all layouts
            applyText(small, userConfig, event, eventData.styledProgressBar, eventData.timeStatusText, eventData.eventDateText, eventData.currentDate, indicator)
            applyText(tall,userConfig, event, eventData.styledProgressBar, eventData.timeStatusText, eventData.eventDateText, eventData.currentDate, indicator)
            applyText(wide,userConfig, event, eventData.styledProgressBar, eventData.timeStatusText, eventData.eventDateText, eventData.currentDate, indicator)


            // event images and Apply theme to all layouts
            applyTheme(small, colors, applyEventImage(context, small, event), userConfig.backgroundTransparency)
            applyTheme(tall, colors, applyEventImage(context, tall, event), userConfig.backgroundTransparency)
            applyTheme(wide, colors, applyEventImage(context, wide, event), userConfig.backgroundTransparency)

            // Apply swiper actions if appWidgetId is valid
            if (appWidgetId != -1) {
                applySwiperActions(small, context, appWidgetId)
                applySwiperActions(tall, context, appWidgetId)
                applySwiperActions(wide, context, appWidgetId)
            }

            // Apply font scaling for small layout
            small.applyTextViewTextSize(
                context,
                R.id.eventTitle,
                R.dimen.event_widget_small_title,
                userConfig.fontScale
            )
            small.applyTextViewTextSize(
                context,
                R.id.widgetDaysLeft,
                R.dimen.event_widget_small_days_left,
                userConfig.fontScale
            )
            small.applyTextViewTextSize(
                context,
                R.id.eventProgressText,
                R.dimen.event_widget_small_progress,
                userConfig.fontScale
            )
            small.applyTextViewTextSize(
                context,
                R.id.currentDate,
                R.dimen.event_widget_small_current_date,
                userConfig.fontScale
            )

            // Apply font scaling for tall layout
            tall.applyTextViewTextSize(
                context,
                R.id.eventTitle,
                R.dimen.event_widget_tallview_title,
                userConfig.fontScale
            )
            tall.applyTextViewTextSize(
                context,
                R.id.eventDesc,
                R.dimen.event_widget_tallview_description,
                userConfig.fontScale
            )
            tall.applyTextViewTextSize(
                context,
                R.id.eventTime,
                R.dimen.event_widget_tallview_time,
                userConfig.fontScale
            )
            tall.applyTextViewTextSize(
                context,
                R.id.widgetDaysLeft,
                R.dimen.event_widget_tallview_days_left,
                userConfig.fontScale
            )
            tall.applyTextViewTextSize(
                context,
                R.id.eventProgressText,
                R.dimen.event_widget_tallview_progress,
                userConfig.fontScale
            )
            tall.applyTextViewTextSize(
                context,
                R.id.currentDate,
                R.dimen.event_widget_tallview_current_date,
                userConfig.fontScale
            )

            // Apply font scaling for wide layout
            wide.applyTextViewTextSize(
                context,
                R.id.eventTitle,
                R.dimen.event_widget_wideview_title,
                userConfig.fontScale
            )
            wide.applyTextViewTextSize(
                context,
                R.id.eventDesc,
                R.dimen.event_widget_wideview_description,
                userConfig.fontScale
            )
            wide.applyTextViewTextSize(
                context,
                R.id.eventTime,
                R.dimen.event_widget_wideview_time,
                userConfig.fontScale
            )
            wide.applyTextViewTextSize(
                context,
                R.id.widgetDaysLeft,
                R.dimen.event_widget_wideview_days_left,
                userConfig.fontScale
            )
            wide.applyTextViewTextSize(
                context,
                R.id.eventProgressText,
                R.dimen.event_widget_wideview_progress,
                userConfig.fontScale
            )
            wide.applyTextViewTextSize(
                context,
                R.id.currentDate,
                R.dimen.event_widget_wideview_current_date,
                userConfig.fontScale
            )

            // Return appropriate layout based on Android version
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                return RemoteViews(
                    mapOf(
                        SizeF(60f, 140f) to small,      // Small square/portrait
                        SizeF(130f, 140f) to tall,      // Tall portrait
                        SizeF(200f, 140f) to wide       // Wide landscape
                    )
                )
            } else {
                // Fallback for older Android versions
                val minWidth = options?.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH) ?: 0
                val minHeight = options?.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT) ?: 0

                return when {
                    minWidth >= 200 && minHeight >= 100 -> wide
                    minWidth >= 130 && minHeight >= 140 -> tall
                    else -> small
                }
            }
        }

        fun emptyEventRemoteView(context: Context, theme: WidgetTheme): RemoteViews {
            val views = RemoteViews(context.packageName, R.layout.event_widget_no_events)
            val colors = WidgetColors.fromTheme(context, theme)
            views.setTextColor(R.id.eventTitle, colors.primaryColor)
            views.setInt(R.id.widgetContainer, "setColorFilter", colors.backgroundColor)
            return views
        }
    }

    /**
     * Data class to hold calculated event display data
     */
    data class EventDisplayData(
        val progress: Double,
        val styledProgressBar: SpannableString,
        val timeStatusText: String,
        val eventDateText: String,
        val currentDate: SpannableString
    )
}