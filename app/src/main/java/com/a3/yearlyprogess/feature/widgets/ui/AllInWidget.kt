package com.a3.yearlyprogess.feature.widgets.ui

import android.appwidget.AppWidgetManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.SizeF
import android.widget.RemoteViews
import com.a3.yearlyprogess.R
import com.a3.yearlyprogess.core.domain.repository.AppSettingsRepository
import com.a3.yearlyprogess.core.util.TimePeriod
import com.a3.yearlyprogess.core.util.YearlyProgressUtil
import com.a3.yearlyprogess.core.util.YearlyProgressUtil.Companion.toFormattedTimePeriod
import com.a3.yearlyprogess.core.util.styleFormatted
import com.a3.yearlyprogess.feature.widgets.domain.model.AllInWidgetOptions
import com.a3.yearlyprogess.feature.widgets.domain.model.WidgetColors
import com.a3.yearlyprogess.feature.widgets.domain.model.WidgetTheme
import com.a3.yearlyprogess.feature.widgets.domain.repository.AllInWidgetOptionsRepository
import com.a3.yearlyprogess.feature.widgets.util.WidgetRenderer
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

/** Implementation of All-In-One Widget functionality. */
@AndroidEntryPoint
class AllInWidget : BaseWidget() {

    @Inject
    lateinit var allInWidgetOptionsRepository: AllInWidgetOptionsRepository

    @Inject
    lateinit var appSettingsRepository: AppSettingsRepository

    override fun updateWidget(context: Context, appWidgetId: Int): RemoteViews {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val options = appWidgetManager.getAppWidgetOptions(appWidgetId)

        // Load user configuration
        val (userConfig, progressSettings) = runBlocking(Dispatchers.IO) {
            val options = allInWidgetOptionsRepository.getOptions(appWidgetId).first()
            val appSettings = appSettingsRepository.appSettings.first()

            // If theme is null, get it from AppSettings
            if (options.theme == null) {
                allInWidgetOptionsRepository.updateTheme(appWidgetId, appSettings.appTheme)
                Pair(options.copy(theme = appSettings.appTheme), appSettings.progressSettings)
            } else {
                Pair(options, appSettings.progressSettings)
            }
        }
        val yp = YearlyProgressUtil(progressSettings)


        return createAllInOneWidgetRemoteView(context, yp, userConfig, options)
    }

    companion object {
        /**
         * Initialize widget view with progress data by dynamically adding items to the container.
         */
        private fun initiateView(
            context: Context,
            views: RemoteViews,
            yp: YearlyProgressUtil,
            userConfig: AllInWidgetOptions,
            maxItems: Int = 4
        ) {
            val colors = WidgetColors.fromTheme(context, userConfig.theme ?: WidgetTheme.DEFAULT)
            
            // Clear existing views to ensure we only show current items and they are packed together
            views.removeAllViews(R.id.itemsContainer)

            val enabledItems = mutableListOf<Triple<CharSequence, Double, String>>()
            
            // Collect items enabled in user configuration
            if (userConfig.showDay) {
                val progress = yp.calculateProgress(TimePeriod.DAY)
                val label = yp.getCurrentPeriodValue(TimePeriod.DAY).toFormattedTimePeriod(yp, TimePeriod.DAY)
                enabledItems.add(Triple(label, progress, context.getString(R.string.day)))
            }
            if (userConfig.showWeek) {
                val progress = yp.calculateProgress(TimePeriod.WEEK)
                val label = yp.getCurrentPeriodValue(TimePeriod.WEEK).toFormattedTimePeriod(yp, TimePeriod.WEEK)
                enabledItems.add(Triple(label, progress, context.getString(R.string.week)))
            }
            if (userConfig.showMonth) {
                val progress = yp.calculateProgress(TimePeriod.MONTH)
                val label = yp.getCurrentPeriodValue(TimePeriod.MONTH).toFormattedTimePeriod(yp, TimePeriod.MONTH)
                enabledItems.add(Triple(label, progress, context.getString(R.string.month)))
            }
            if (userConfig.showYear) {
                val progress = yp.calculateProgress(TimePeriod.YEAR)
                val label = yp.getCurrentPeriodValue(TimePeriod.YEAR).toFormattedTimePeriod(yp, TimePeriod.YEAR)
                enabledItems.add(Triple(label, progress, context.getString(R.string.year)))
            }

            // Fallback: If nothing is enabled, show at least the day progress
            if (enabledItems.isEmpty()) {
                val progress = yp.calculateProgress(TimePeriod.DAY)
                val label = yp.getCurrentPeriodValue(TimePeriod.DAY).toFormattedTimePeriod(yp, TimePeriod.DAY)
                enabledItems.add(Triple(label, progress, context.getString(R.string.day)))
            }

            // Take up to maxItems from the enabled ones
            val visibleItems = enabledItems.take(maxItems)

            // Add each item to the container
            for (item in visibleItems) {
                val itemView = RemoteViews(context.packageName, R.layout.all_in_widget_item)
                
                itemView.setTextColor(R.id.progressText, colors.primaryColor)
                itemView.setTextColor(R.id.progressTitle, colors.secondaryColor)
                
                itemView.setTextViewText(R.id.progressText, item.second.styleFormatted(userConfig.decimalPlaces))
                itemView.setTextViewText(R.id.progressTitle, item.first)
                
                val progressViews = RemoteViews(context.packageName, R.layout.circular_progress_bars_container)
                WidgetRenderer.applyCircularProgressBar(progressViews, item.second.toInt(), userConfig.theme ?: WidgetTheme.DEFAULT)
                
                progressViews.setInt(R.id.circularProgressBackground, "setColorFilter", colors.backgroundColor)
                val alpha = ((userConfig.backgroundTransparency / 100.0) * 255).toInt()
                progressViews.setInt(R.id.circularProgressBackground, "setImageAlpha", alpha)

                itemView.removeAllViews(R.id.progressContainer)
                itemView.addView(R.id.progressContainer, progressViews)
                
                views.addView(R.id.itemsContainer, itemView)
            }

            // Set click action to open main activity
            WidgetRenderer.onParentTap(views, context)
        }

        fun createAllInOneWidgetRemoteView(
            context: Context,
            yp: YearlyProgressUtil,
            userConfig: AllInWidgetOptions,
            options: Bundle,
            isWidgetPreview: Boolean = false
        ): RemoteViews {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !isWidgetPreview) {
                val xlarge = RemoteViews(context.packageName, R.layout.all_in_widget)
                val large = RemoteViews(context.packageName, R.layout.all_in_widget)
                val medium = RemoteViews(context.packageName, R.layout.all_in_widget)
                val small = RemoteViews(context.packageName, R.layout.all_in_widget)
                val square = RemoteViews(context.packageName, R.layout.all_in_widget_square)
                val tall = RemoteViews(context.packageName, R.layout.all_in_widget_vertical)

                initiateView(context, xlarge, yp, userConfig, maxItems = 4)
                initiateView(context, large, yp, userConfig, maxItems = 3)
                initiateView(context, medium, yp, userConfig, maxItems = 2)
                initiateView(context, small, yp, userConfig, maxItems = 1)
                initiateView(context, square, yp, userConfig, maxItems = 4)
                initiateView(context, tall, yp, userConfig, maxItems = 3)

                val viewMapping: Map<SizeF, RemoteViews> = mapOf(
                    SizeF(300f, 80f) to xlarge,
                    SizeF(220f, 80f) to large,
                    SizeF(130f, 130f) to square,
                    SizeF(102f, 276f) to tall,
                    SizeF(160f, 80f) to medium,
                    SizeF(100f, 80f) to small,
                )

                return RemoteViews(viewMapping)
            } else {
                val minWidth = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH)
                val minHeight = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT)
                
                return when {
                    minWidth >= 300 -> {
                        val v = RemoteViews(context.packageName, R.layout.all_in_widget)
                        initiateView(context, v, yp, userConfig, maxItems = 4)
                        v
                    }
                    minWidth >= 220 -> {
                        val v = RemoteViews(context.packageName, R.layout.all_in_widget)
                        initiateView(context, v, yp, userConfig, maxItems = 3)
                        v
                    }
                    minHeight >= 200 && minWidth <= 120 -> {
                        val v = RemoteViews(context.packageName, R.layout.all_in_widget_vertical)
                        initiateView(context, v, yp, userConfig, maxItems = 3)
                        v
                    }
                    minWidth >= 130 && minHeight >= 130 -> {
                        val v = RemoteViews(context.packageName, R.layout.all_in_widget_square)
                        initiateView(context, v, yp, userConfig, maxItems = 4)
                        v
                    }
                    minWidth >= 160 -> {
                        val v = RemoteViews(context.packageName, R.layout.all_in_widget)
                        initiateView(context, v, yp, userConfig, maxItems = 2)
                        v
                    }
                    else -> {
                        val v = RemoteViews(context.packageName, R.layout.all_in_widget)
                        initiateView(context, v, yp, userConfig, maxItems = 1)
                        v
                    }
                }
            }
        }
    }
}
