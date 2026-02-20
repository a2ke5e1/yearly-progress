package com.a3.yearlyprogess.feature.widgets.ui

import android.Manifest
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.text.SpannableString
import android.util.SizeF
import android.view.View
import android.widget.RemoteViews
import com.a3.yearlyprogess.R
import com.a3.yearlyprogess.core.domain.repository.AppSettingsRepository
import com.a3.yearlyprogess.core.util.Log
import com.a3.yearlyprogess.core.util.Resource
import com.a3.yearlyprogess.core.util.TimePeriod
import com.a3.yearlyprogess.core.util.YearlyProgressUtil
import com.a3.yearlyprogess.core.util.YearlyProgressUtil.Companion.toFormattedTimePeriod
import com.a3.yearlyprogess.core.util.styleFormatted
import com.a3.yearlyprogess.core.util.toTimePeriodText
import com.a3.yearlyprogess.data.local.getStartAndEndTime
import com.a3.yearlyprogess.domain.model.SunriseSunset
import com.a3.yearlyprogess.domain.repository.LocationRepository
import com.a3.yearlyprogess.domain.repository.SunriseSunsetRepository
import com.a3.yearlyprogess.feature.widgets.domain.model.StandaloneWidgetOptions
import com.a3.yearlyprogess.feature.widgets.domain.model.StandaloneWidgetOptions.Companion.WidgetShape
import com.a3.yearlyprogess.feature.widgets.domain.model.WidgetColors
import com.a3.yearlyprogess.feature.widgets.domain.model.WidgetTheme
import com.a3.yearlyprogess.feature.widgets.domain.repository.StandaloneWidgetOptionsRepository
import com.a3.yearlyprogess.feature.widgets.util.WidgetRenderer
import com.a3.yearlyprogess.feature.widgets.util.WidgetRenderer.applyTextViewTextSize
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import javax.inject.Inject
import kotlin.math.roundToInt

enum class StandaloneWidgetType {
    DAY,
    WEEK,
    MONTH,
    YEAR,
    DAY_LIGHT,
    NIGHT_LIGHT
}

@AndroidEntryPoint
open class StandaloneWidget(
    private val widgetType: StandaloneWidgetType,
) : BaseWidget() {

    @Inject
    lateinit var standaloneWidgetOptionsRepository: StandaloneWidgetOptionsRepository

    @Inject
    lateinit var sunriseSunsetRepository: SunriseSunsetRepository

    @Inject
    lateinit var locationRepository: LocationRepository
    
    @Inject
    lateinit var appSettingsRepository: AppSettingsRepository


    override fun updateWidget(context: Context, appWidgetId: Int): RemoteViews {
        val manager = AppWidgetManager.getInstance(context)
        val bundleOptions = manager.getAppWidgetOptions(appWidgetId)

        // Load user configuration
        val (userConfig, progressSettings) = runBlocking(Dispatchers.IO) {
            standaloneWidgetOptionsRepository.updateWidgetType(appWidgetId, widgetType)
            val options = standaloneWidgetOptionsRepository.getOptions(appWidgetId).first()
            val appSettings = appSettingsRepository.appSettings.first()
            // If theme is null, get it from AppSettings
            if (options.theme == null) {
                standaloneWidgetOptionsRepository.updateTheme(appWidgetId, appSettings.appTheme)
                Pair(options.copy(theme = appSettings.appTheme), appSettings.progressSettings)
            } else {
                Pair(options, appSettings.progressSettings)
            }
        }
        val yp = YearlyProgressUtil(progressSettings)

        val sunsetData = runBlocking(Dispatchers.IO) {
            if (widgetType != StandaloneWidgetType.DAY_LIGHT && widgetType != StandaloneWidgetType.NIGHT_LIGHT) {
                return@runBlocking null
            }
            val location = locationRepository.getSavedLocation().first() ?: return@runBlocking null
            val lat = location.latitude
            val lon = location.longitude

            // Use withTimeoutOrNull to prevent the widget from hanging/blocking indefinitely
            val result = withTimeoutOrNull(1000L) { // 1 second timeout
                sunriseSunsetRepository.getSunriseSunset(lat, lon)
                    .first { it is Resource.Success } // Only take the first SUCCESS item
            }

            Log.d("StandaloneWidget", "Sunset Result after timeout/filter: $result")

            // Return the data if we got a success within 1s, otherwise null
            return@runBlocking (result as? Resource.Success)?.data
        }

        if (
            (widgetType == StandaloneWidgetType.DAY_LIGHT || widgetType == StandaloneWidgetType.NIGHT_LIGHT)
            && context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) !=
            PackageManager.PERMISSION_GRANTED
            && sunsetData == null
            ) {
            return WidgetRenderer.errorWidgetRemoteView(
                context,
                "Location Permission Required",
                userConfig.theme!!,
            )
        }


        Log.d("StandaloneWidget", "User config: $userConfig")
        Log.d("StandaloneWidget", "Sunset Data: $sunsetData")

        // Choose the appropriate view based on widget shape
        val views = when (userConfig.widgetShape) {
            WidgetShape.RECTANGULAR -> when (widgetType) {
                StandaloneWidgetType.DAY_LIGHT -> rectangularRemoteView(
                    context,
                    yp,
                    userConfig,
                    sunsetData,
                )

                StandaloneWidgetType.NIGHT_LIGHT -> rectangularRemoteView(
                    context,
                    yp,
                    userConfig,
                    sunsetData,
                )

                else -> rectangularRemoteView(context, yp, userConfig)
            }

            WidgetShape.CLOVER -> when (widgetType) {
                StandaloneWidgetType.DAY_LIGHT -> cloverRemoteView(
                    context,
                    yp,
                    userConfig,
                    sunsetData,
                    bundleOptions
                )

                StandaloneWidgetType.NIGHT_LIGHT -> cloverRemoteView(
                    context,
                    yp,
                    userConfig,
                    sunsetData,
                    bundleOptions
                )

                else -> cloverRemoteView(context, yp, userConfig, bundleOptions)
            }

            WidgetShape.PILL -> when (widgetType) {
                StandaloneWidgetType.DAY_LIGHT -> pillRemoteView(
                    context,
                    yp,
                    userConfig,
                    sunsetData,
                    bundleOptions
                )

                StandaloneWidgetType.NIGHT_LIGHT -> pillRemoteView(
                    context,
                    yp,
                    userConfig,
                    sunsetData,
                    bundleOptions
                )

                else -> pillRemoteView(context, yp, userConfig, bundleOptions)
            }
        }

        return views
    }

    companion object {

        /**
         * Apply theme colors to the widget views
         */
        private fun applyTheme(views: RemoteViews, colors: WidgetColors) {
            views.setTextColor(R.id.widgetProgress, colors.primaryColor)
            views.setTextColor(R.id.widgetType, colors.primaryColor)
            views.setTextColor(R.id.widgetDaysLeft, colors.secondaryColor)
            views.setTextColor(R.id.widgetCurrentValue, colors.secondaryColor)
        }

        /**
         * Apply text content to widget views with user configuration
         */
        private fun applyTexts(
            views: RemoteViews,
            progress: Double,
            widgetName: String,
            daysLeft: String,
            currentValue: SpannableString,
            userConfig: StandaloneWidgetOptions
        ) {
            // Set widget type/title
            views.setTextViewText(R.id.widgetType, widgetName)

            // Handle progress or days left display based on config
            if (userConfig.timeLeftCounter && userConfig.replaceProgressWithDaysLeft) {
                // Replace progress with days left
                views.setTextViewText(
                    R.id.widgetProgress,
                    daysLeft
                )
            } else {
                // Show normal progress percentage
                views.setTextViewText(
                    R.id.widgetProgress,
                    progress.styleFormatted(userConfig.decimalPlaces)
                )
            }

            // Show/hide days left counter
            if (userConfig.timeLeftCounter && !userConfig.replaceProgressWithDaysLeft) {
                views.setViewVisibility(R.id.widgetDaysLeft, View.VISIBLE)
                views.setTextViewText(
                    R.id.widgetDaysLeft,
                    daysLeft
                )
            } else {
                views.setViewVisibility(R.id.widgetDaysLeft, View.GONE)
            }

            // Set current value
            views.setTextViewText(R.id.widgetCurrentValue, currentValue)
        }

        /**
         * Apply background transparency
         */
        private fun applyBackgroundTransparency(
            views: RemoteViews,
            transparency: Int
        ) {
            // Convert transparency percentage (0-100) to alpha (0-255)
            val alpha = ((transparency / 100.0) * 255).toInt()
            views.setInt(R.id.widgetContainer, "setImageAlpha", alpha)
        }

        /**
         * Apply font scaling to rectangular widget text views
         */
        private fun applyFontScaleRectangular(
            views: RemoteViews,
            fontScale: Float,
            context: Context
        ) {
            views.applyTextViewTextSize(
                context = context,
                viewId = R.id.widgetType,
                defaultTextSize = R.dimen.standalone_rect_widget_text_size_widget_type,
                fontScale = fontScale
            )
            views.applyTextViewTextSize(
                context = context,
                viewId = R.id.widgetDaysLeft,
                defaultTextSize = R.dimen.standalone_rect_widget_text_size_widget_days_left,
                fontScale = fontScale
            )
            views.applyTextViewTextSize(
                context = context,
                viewId = R.id.widgetProgress,
                defaultTextSize = R.dimen.standalone_rect_widget_text_size_widget_progress,
                fontScale = fontScale
            )
            views.applyTextViewTextSize(
                context = context,
                viewId = R.id.widgetCurrentValue,
                defaultTextSize = R.dimen.standalone_rect_widget_text_size_widget_current_value,
                fontScale = fontScale
            )
        }

        /**
         * Apply font scaling to clover large widget text views
         */
        private fun applyFontScaleCloverLarge(
            views: RemoteViews,
            fontScale: Float,
            context: Context
        ) {
            views.applyTextViewTextSize(
                context = context,
                viewId = R.id.widgetType,
                defaultTextSize = R.dimen.standalone_clover_large_widget_text_size_widget_type,
                fontScale = fontScale
            )
            views.applyTextViewTextSize(
                context = context,
                viewId = R.id.widgetDaysLeft,
                defaultTextSize = R.dimen.standalone_clover_large_widget_text_size_widget_days_left,
                fontScale = fontScale
            )
            views.applyTextViewTextSize(
                context = context,
                viewId = R.id.widgetProgress,
                defaultTextSize = R.dimen.standalone_clover_large_widget_text_size_widget_progress,
                fontScale = fontScale
            )
            views.applyTextViewTextSize(
                context = context,
                viewId = R.id.widgetCurrentValue,
                defaultTextSize = R.dimen.standalone_clover_large_widget_text_size_widget_current_value,
                fontScale = fontScale
            )
        }

        /**
         * Apply font scaling to clover (square) widget text views
         */
        private fun applyFontScaleClover(
            views: RemoteViews,
            fontScale: Float,
            context: Context
        ) {
            views.applyTextViewTextSize(
                context = context,
                viewId = R.id.widgetType,
                defaultTextSize = R.dimen.standalone_clover_widget_text_size_widget_type,
                fontScale = fontScale
            )
            views.applyTextViewTextSize(
                context = context,
                viewId = R.id.widgetDaysLeft,
                defaultTextSize = R.dimen.standalone_clover_widget_text_size_widget_days_left,
                fontScale = fontScale
            )
            views.applyTextViewTextSize(
                context = context,
                viewId = R.id.widgetProgress,
                defaultTextSize = R.dimen.standalone_clover_widget_text_size_widget_progress,
                fontScale = fontScale
            )
            views.applyTextViewTextSize(
                context = context,
                viewId = R.id.widgetCurrentValue,
                defaultTextSize = R.dimen.standalone_clover_widget_text_size_widget_current_value,
                fontScale = fontScale
            )
        }

        /**
         * Apply font scaling to clover small widget text views
         */
        private fun applyFontScaleCloverSmall(
            views: RemoteViews,
            fontScale: Float,
            context: Context
        ) {
            views.applyTextViewTextSize(
                context = context,
                viewId = R.id.widgetType,
                defaultTextSize = R.dimen.standalone_clover_small_widget_text_size_widget_type,
                fontScale = fontScale
            )
            views.applyTextViewTextSize(
                context = context,
                viewId = R.id.widgetDaysLeft,
                defaultTextSize = R.dimen.standalone_clover_small_widget_text_size_widget_days_left,
                fontScale = fontScale
            )
            views.applyTextViewTextSize(
                context = context,
                viewId = R.id.widgetProgress,
                defaultTextSize = R.dimen.standalone_clover_small_widget_text_size_widget_progress,
                fontScale = fontScale
            )
            views.applyTextViewTextSize(
                context = context,
                viewId = R.id.widgetCurrentValue,
                defaultTextSize = R.dimen.standalone_clover_small_widget_text_size_widget_current_value,
                fontScale = fontScale
            )
        }

        /**
         * Apply font scaling to clover extra small widget text views
         */
        private fun applyFontScaleCloverExtraSmall(
            views: RemoteViews,
            fontScale: Float,
            context: Context
        ) {
            views.applyTextViewTextSize(
                context = context,
                viewId = R.id.widgetType,
                defaultTextSize = R.dimen.standalone_clover_extra_small_widget_text_size_widget_type,
                fontScale = fontScale
            )
            views.applyTextViewTextSize(
                context = context,
                viewId = R.id.widgetDaysLeft,
                defaultTextSize = R.dimen.standalone_clover_extra_small_widget_text_size_widget_days_left,
                fontScale = fontScale
            )
            views.applyTextViewTextSize(
                context = context,
                viewId = R.id.widgetProgress,
                defaultTextSize = R.dimen.standalone_clover_extra_small_widget_text_size_widget_progress,
                fontScale = fontScale
            )
            views.applyTextViewTextSize(
                context = context,
                viewId = R.id.widgetCurrentValue,
                defaultTextSize = R.dimen.standalone_clover_extra_small_widget_text_size_widget_current_value,
                fontScale = fontScale
            )
        }

        /**
         * Apply font scaling to pill medium widget text views
         */
        private fun applyFontScalePillMedium(
            views: RemoteViews,
            fontScale: Float,
            context: Context
        ) {
            views.applyTextViewTextSize(
                context = context,
                viewId = R.id.widgetType,
                defaultTextSize = R.dimen.standalone_pill_medium_widget_text_size_widget_type,
                fontScale = fontScale
            )
            views.applyTextViewTextSize(
                context = context,
                viewId = R.id.widgetDaysLeft,
                defaultTextSize = R.dimen.standalone_pill_medium_widget_text_size_widget_days_left,
                fontScale = fontScale
            )
            views.applyTextViewTextSize(
                context = context,
                viewId = R.id.widgetProgress,
                defaultTextSize = R.dimen.standalone_pill_medium_widget_text_size_widget_progress,
                fontScale = fontScale
            )
            views.applyTextViewTextSize(
                context = context,
                viewId = R.id.widgetCurrentValue,
                defaultTextSize = R.dimen.standalone_pill_medium_widget_text_size_widget_current_value,
                fontScale = fontScale
            )
        }

        /**
         * Apply font scaling to pill small widget text views
         */
        private fun applyFontScalePillSmall(
            views: RemoteViews,
            fontScale: Float,
            context: Context
        ) {
            views.applyTextViewTextSize(
                context = context,
                viewId = R.id.widgetType,
                defaultTextSize = R.dimen.standalone_pill_small_widget_text_size_widget_type,
                fontScale = fontScale
            )
            views.applyTextViewTextSize(
                context = context,
                viewId = R.id.widgetDaysLeft,
                defaultTextSize = R.dimen.standalone_pill_small_widget_text_size_widget_days_left,
                fontScale = fontScale
            )
            views.applyTextViewTextSize(
                context = context,
                viewId = R.id.widgetProgress,
                defaultTextSize = R.dimen.standalone_pill_small_widget_text_size_widget_progress,
                fontScale = fontScale
            )
            views.applyTextViewTextSize(
                context = context,
                viewId = R.id.widgetCurrentValue,
                defaultTextSize = R.dimen.standalone_pill_small_widget_text_size_widget_current_value,
                fontScale = fontScale
            )
        }

        /**
         * Create rectangular widget view with user configuration
         */
        fun rectangularRemoteView(
            context: Context,
            yp: YearlyProgressUtil,
            userConfig: StandaloneWidgetOptions,
            isWidgetClickable: Boolean = true
        ): RemoteViews {
            val timePeriod = mapWidgetTypeToTimePeriod(userConfig)
            val progress = yp.calculateProgress(timePeriod)
            val endTime = yp.calculateEndTime(timePeriod)
            val daysLeft = context.getString(
                R.string.time_left,
                yp.calculateTimeLeft(endTime).toTimePeriodText(userConfig.dynamicLeftCounter)
            )
            val currentValue = yp.getCurrentPeriodValue(timePeriod).toFormattedTimePeriod(yp, timePeriod)
            return rectangularRemoteView(
                context,
                userConfig,
                progress,
                timePeriod.name,
                daysLeft,
                currentValue,
                isWidgetClickable
            )
        }

        fun rectangularRemoteView(
            context: Context,
            yp: YearlyProgressUtil,
            userConfig: StandaloneWidgetOptions,
            sunriseSunsetData: List<SunriseSunset>?,
            isWidgetClickable: Boolean = true
        ): RemoteViews {
            val dayLight = userConfig.widgetType === StandaloneWidgetType.DAY_LIGHT
            if (sunriseSunsetData == null) {
                return WidgetRenderer.errorWidgetRemoteView(context, "Failed to load sunset data")
            }
            val (startTime, endTime) = getStartAndEndTime(dayLight, sunriseSunsetData)
            val progress = yp.calculateProgress(startTime, endTime)
            val daysLeft = context.getString(
                R.string.time_left,
                yp.calculateTimeLeft(endTime).toTimePeriodText(userConfig.dynamicLeftCounter)
            )


            val format =
                DateTimeFormatter
                    .ofLocalizedTime(FormatStyle.SHORT)
                    .withLocale(yp.settings.uLocale.toLocale())
                    .withZone(ZoneId.systemDefault())

            val currentValue =
                if (dayLight) {
                    "🌇 ${format.format(Instant.ofEpochMilli(endTime))}"
                } else {
                    "🌅 ${format.format(Instant.ofEpochMilli(endTime))}"
                }

            val widgetName =
                if (dayLight) context.getString(R.string.day_light) else context.getString(
                    R.string.night_light
                )



            return rectangularRemoteView(
                context,
                userConfig,
                progress,
                widgetName,
                daysLeft,
                SpannableString(currentValue),
                isWidgetClickable
            )
        }

        private fun rectangularRemoteView(
            context: Context,
            userConfig: StandaloneWidgetOptions,
            progress: Double,
            widgetName: String,
            daysLeft: String,
            currentValue: SpannableString,
            isWidgetClickable: Boolean
        ): RemoteViews {
            val views = RemoteViews(context.packageName, R.layout.standalone_widget_layout)

            if (isWidgetClickable) {
                WidgetRenderer.onParentTap(views, context)
            }

            // Apply theme from user config
            val colors = WidgetColors.fromTheme(context, userConfig.theme ?: WidgetTheme.DEFAULT)
            applyTheme(views, colors)

            // Apply background color with transparency
            views.setInt(R.id.widgetContainer, "setColorFilter", colors.backgroundColor)
            applyBackgroundTransparency(views, userConfig.backgroundTransparency)

            // Apply text content with user config
            applyTexts(views, progress, widgetName, daysLeft, currentValue, userConfig)

            // Apply progress bar
            WidgetRenderer.applyLinearProgressBar(views, progress.roundToInt(), userConfig.theme ?: WidgetTheme.DEFAULT)

            // Apply font scale
            applyFontScaleRectangular(views, userConfig.fontScale, context)

            return views
        }

        /**
         * Create clover widget view with user configuration
         */
        fun cloverRemoteView(
            context: Context,
            yp: YearlyProgressUtil,
            userConfig: StandaloneWidgetOptions,
            options: Bundle? = null,
            isWidgetClickable: Boolean = true
        ): RemoteViews {
            val timePeriod = mapWidgetTypeToTimePeriod(userConfig)
            val progress = yp.calculateProgress(timePeriod)
            val endTime = yp.calculateEndTime(timePeriod)
            val daysLeft = context.getString(
                R.string.time_left,
                yp.calculateTimeLeft(endTime).toTimePeriodText(userConfig.dynamicLeftCounter)
            )
            val currentValue =
                yp.getCurrentPeriodValue(timePeriod).toFormattedTimePeriod(yp, timePeriod)
            val widgetName = timePeriod.name

            return cloverRemoteView(
                context,
                yp,
                userConfig,
                progress,
                widgetName,
                daysLeft,
                currentValue,
                options,
                isWidgetClickable
            )
        }

        fun cloverRemoteView(
            context: Context,
            yp: YearlyProgressUtil,
            userConfig: StandaloneWidgetOptions,
            sunriseSunsetData: List<SunriseSunset>?,
            options: Bundle? = null,
            isWidgetClickable: Boolean = true
        ): RemoteViews {
            val dayLight = userConfig.widgetType === StandaloneWidgetType.DAY_LIGHT
            if (sunriseSunsetData == null) {
                return WidgetRenderer.errorWidgetRemoteView(context, "Failed to load sunset data")
            }
            val (startTime, endTime) = getStartAndEndTime(dayLight, sunriseSunsetData)
            val progress = yp.calculateProgress(startTime, endTime)
            val daysLeft = context.getString(
                R.string.time_left,
                yp.calculateTimeLeft(endTime).toTimePeriodText(userConfig.dynamicLeftCounter)
            )

            val format =
                DateTimeFormatter
                    .ofLocalizedTime(FormatStyle.SHORT)
                    .withLocale(yp.settings.uLocale.toLocale())
                    .withZone(ZoneId.systemDefault())

            val currentValue =
                if (dayLight) {
                    "🌇 ${format.format(Instant.ofEpochMilli(endTime))}"
                } else {
                    "🌅 ${format.format(Instant.ofEpochMilli(endTime))}"
                }

            val widgetName =
                if (dayLight) context.getString(R.string.day_light) else context.getString(
                    R.string.night_light
                )

            return cloverRemoteView(
                context,
                yp,
                userConfig,
                progress,
                widgetName,
                daysLeft,
                SpannableString(currentValue),
                options,
                isWidgetClickable
            )
        }

        private fun cloverRemoteView(
            context: Context,
            yp: YearlyProgressUtil,
            userConfig: StandaloneWidgetOptions,
            progress: Double,
            widgetName: String,
            daysLeft: String,
            currentValue: SpannableString,
            options: Bundle? = null,
            isWidgetClickable: Boolean
        ): RemoteViews {
            val large = RemoteViews(context.packageName, R.layout.standalone_widget_clover_layout_large)
            val square = RemoteViews(context.packageName, R.layout.standalone_widget_clover_layout)
            val small = RemoteViews(context.packageName, R.layout.standalone_widget_clover_layout_small)
            val xSmall = RemoteViews(context.packageName, R.layout.standalone_widget_clover_layout_extra_small)

            if (isWidgetClickable) {
                WidgetRenderer.onParentTap(large, context)
                WidgetRenderer.onParentTap(square, context)
                WidgetRenderer.onParentTap(small, context)
                WidgetRenderer.onParentTap(xSmall, context)
            }

            // Apply theme from user config
            val colors = WidgetColors.fromTheme(context, userConfig.theme ?: WidgetTheme.DEFAULT)

            // Apply to large view
            applyTheme(large, colors)
            large.setTextColor(R.id.widgetCurrentValue, colors.accentColor)
            applyTexts(large, progress, widgetName, daysLeft, currentValue, userConfig)
            WidgetRenderer.applyCloverProgressContainer(large, progress, userConfig.theme ?: WidgetTheme.DEFAULT)
            applyFontScaleCloverLarge(large, userConfig.fontScale, context)

            // Apply to square view
            applyTheme(square, colors)
            square.setTextColor(R.id.widgetCurrentValue, colors.accentColor)
            applyTexts(square, progress, widgetName, daysLeft, currentValue, userConfig)
            WidgetRenderer.applyCloverProgressContainer(square, progress, userConfig.theme ?: WidgetTheme.DEFAULT)
            applyFontScaleClover(square, userConfig.fontScale, context)

            // Apply to small view
            applyTheme(small, colors)
            small.setTextColor(R.id.widgetCurrentValue, colors.accentColor)
            applyTexts(small, progress, widgetName, daysLeft, currentValue, userConfig)
            WidgetRenderer.applyCloverProgressContainer(small, progress, userConfig.theme ?: WidgetTheme.DEFAULT)
            applyFontScaleCloverSmall(small, userConfig.fontScale, context)

            // Apply to extra small view
            applyTheme(xSmall, colors)
            xSmall.setTextColor(R.id.widgetCurrentValue, colors.accentColor)
            applyTexts(xSmall, progress, widgetName, daysLeft, currentValue, userConfig)
            WidgetRenderer.applyCloverProgressContainer(xSmall, progress, userConfig.theme ?: WidgetTheme.DEFAULT)
            applyFontScaleCloverExtraSmall(xSmall, userConfig.fontScale, context)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                return RemoteViews(
                    mapOf(
                        SizeF(57f, 102f) to xSmall,
                        SizeF(130f, 102f) to small,
                        SizeF(130f, 220f) to square,
                        SizeF(203f, 220f) to large
                    ),
                )
            } else {
                val minWidth = options?.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH) ?: 0
                val minHeight = options?.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT) ?: 0

                return when {
                    minWidth >= 180 && minHeight >= 200 -> large
                    minWidth >= 130 && minHeight >= 120 -> square
                    minWidth >= 90 -> small
                    else -> xSmall
                }
            }
        }

        /**
         * Create pill widget view with user configuration
         */
        fun pillRemoteView(
            context: Context,
            yp: YearlyProgressUtil,
            userConfig: StandaloneWidgetOptions,
            options: Bundle? = null,
            isWidgetClickable: Boolean = true
        ): RemoteViews {
            val timePeriod = mapWidgetTypeToTimePeriod(userConfig)
            val progress = yp.calculateProgress(timePeriod)
            val endTime = yp.calculateEndTime(timePeriod)
            val daysLeft = context.getString(
                R.string.time_left,
                yp.calculateTimeLeft(endTime).toTimePeriodText(userConfig.dynamicLeftCounter)
            )
            val currentValue = yp.getCurrentPeriodValue(timePeriod).toFormattedTimePeriod(yp, timePeriod)
            val widgetName = timePeriod.name

            return pillRemoteView(
                context,
                yp,
                userConfig,
                progress,
                widgetName,
                daysLeft,
                currentValue,
                options,
                isWidgetClickable
            )
        }

        private fun mapWidgetTypeToTimePeriod(userConfig: StandaloneWidgetOptions): TimePeriod =
            when (userConfig.widgetType) {
                StandaloneWidgetType.DAY -> TimePeriod.DAY
                StandaloneWidgetType.WEEK -> TimePeriod.WEEK
                StandaloneWidgetType.MONTH -> TimePeriod.MONTH
                StandaloneWidgetType.YEAR -> TimePeriod.YEAR
                else -> throw Error("Cound not find valid TimePeriod for Widget Type ${userConfig.widgetType}")
            }

        fun pillRemoteView(
            context: Context,
            yp: YearlyProgressUtil,
            userConfig: StandaloneWidgetOptions,
            sunriseSunsetData: List<SunriseSunset>?,
            options: Bundle? = null,
            isWidgetClickable: Boolean = true
        ): RemoteViews {
            val dayLight = userConfig.widgetType === StandaloneWidgetType.DAY_LIGHT
            if (sunriseSunsetData == null) {
                return WidgetRenderer.errorWidgetRemoteView(context, "Failed to load sunset data")
            }
            val (startTime, endTime) = getStartAndEndTime(dayLight, sunriseSunsetData)
            val progress = yp.calculateProgress(startTime, endTime)
            val daysLeft = context.getString(
                R.string.time_left,
                yp.calculateTimeLeft(endTime).toTimePeriodText(userConfig.dynamicLeftCounter)
            )

            val format =
                DateTimeFormatter
                    .ofLocalizedTime(FormatStyle.SHORT)
                    .withLocale(yp.settings.uLocale.toLocale())
                    .withZone(ZoneId.systemDefault())

            val currentValue =
                if (dayLight) {
                    "🌇 ${format.format(Instant.ofEpochMilli(endTime))}"
                } else {
                    "🌅 ${format.format(Instant.ofEpochMilli(endTime))}"
                }

            val widgetName =
                if (dayLight) context.getString(R.string.day_light) else context.getString(
                    R.string.night_light
                )

            return pillRemoteView(
                context,
                yp,
                userConfig,
                progress,
                widgetName,
                daysLeft,
                SpannableString(currentValue),
                options,
                isWidgetClickable
            )
        }

        private fun pillRemoteView(
            context: Context,
            yp: YearlyProgressUtil,
            userConfig: StandaloneWidgetOptions,
            progress: Double,
            widgetName: String,
            daysLeft: String,
            currentValue: SpannableString,
            options: Bundle? = null,
            isWidgetClickable: Boolean
        ): RemoteViews {
            val large =
                RemoteViews(context.packageName, R.layout.standalone_widget_pill_layout_medium)
            val small =
                RemoteViews(context.packageName, R.layout.standalone_widget_pill_layout_small)

            if (isWidgetClickable) {
                WidgetRenderer.onParentTap(large, context)
                WidgetRenderer.onParentTap(small, context)
            }

            // Apply theme from user config
            val colors = WidgetColors.fromTheme(context, userConfig.theme ?: WidgetTheme.DEFAULT)

            // Apply to large/medium view
            applyTheme(large, colors)
            large.setTextColor(R.id.widgetCurrentValue, colors.accentColor)
            applyTexts(large, progress, widgetName, daysLeft, currentValue, userConfig)
            WidgetRenderer.applyPillProgressContainer(large, progress, userConfig.theme ?: WidgetTheme.DEFAULT)
            applyFontScalePillMedium(large, userConfig.fontScale, context)

            // Apply to small view
            applyTheme(small, colors)
            small.setTextColor(R.id.widgetCurrentValue, colors.accentColor)
            applyTexts(small, progress, widgetName, daysLeft, currentValue, userConfig)
            WidgetRenderer.applyPillProgressContainer(small, progress, userConfig.theme ?: WidgetTheme.DEFAULT)
            applyFontScalePillSmall(small, userConfig.fontScale, context)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                return RemoteViews(
                    mapOf(
                        SizeF(160f, 160f) to large,
                        SizeF(100f, 100f) to small,
                    ),
                )
            } else {
                val minWidth = options?.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH) ?: 0
                val minHeight = options?.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT) ?: 0

                return when {
                    minWidth >= 160 && minHeight >= 160 -> large
                    else -> small
                }
            }
        }
    }
}

class DayWidget : StandaloneWidget(StandaloneWidgetType.DAY)
class WeekWidget : StandaloneWidget(StandaloneWidgetType.WEEK)
class MonthWidget : StandaloneWidget(StandaloneWidgetType.MONTH)
class YearWidget : StandaloneWidget(StandaloneWidgetType.YEAR)
class DayLightWidget : StandaloneWidget(StandaloneWidgetType.DAY_LIGHT)
class NightLightWidget : StandaloneWidget(StandaloneWidgetType.NIGHT_LIGHT)