package com.a3.yearlyprogess.feature.widgets.ui

import android.appwidget.AppWidgetManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.text.SpannableString
import android.util.SizeF
import android.view.View
import android.widget.RemoteViews
import com.a3.yearlyprogess.R
import com.a3.yearlyprogess.core.util.TimePeriod
import com.a3.yearlyprogess.core.util.YearlyProgressUtil
import com.a3.yearlyprogess.core.util.YearlyProgressUtil.Companion.toFormattedTimePeriod
import com.a3.yearlyprogess.core.util.styleFormatted
import com.a3.yearlyprogess.core.util.toTimePeriodText
import com.a3.yearlyprogess.feature.widgets.domain.model.WidgetColors
import com.a3.yearlyprogess.feature.widgets.domain.model.WidgetTheme
import kotlin.math.roundToInt

open class StandaloneWidget(
    private val timePeriod: TimePeriod,
) : BaseWidget() {

    override fun updateWidget(context: Context, appWidgetId: Int): RemoteViews {
        val yp = YearlyProgressUtil()
        val theme: WidgetTheme = WidgetTheme.DEFAULT
        val manager = AppWidgetManager.getInstance(context)
        val options = manager.getAppWidgetOptions(appWidgetId)

        val views = cloverRemoteView(context, yp, theme, timePeriod, options)
        return views
    }

    companion object {

        private val allProgressBarIds = listOf(
            R.id.widgetProgressBarDefault,
            R.id.widgetProgressBarGreen,
            R.id.widgetProgressBarDynamic
            // add new progress bar ids here as you create themes (e.g. R.id.widgetProgressBarBlue)
        )

        private val allShapeContainerIds = listOf(
            R.id.widgetContainerDefault,
            R.id.widgetContainerGreen,
            R.id.widgetContainerDynamic
        )

        private fun applyTheme(views: RemoteViews, colors: WidgetColors) {
            views.setTextColor(R.id.widgetProgress, colors.primaryColor)
            views.setTextColor(R.id.widgetType, colors.primaryColor)
            views.setTextColor(R.id.widgetDaysLeft, colors.secondaryColor)
            views.setTextColor(R.id.widgetCurrentValue, colors.secondaryColor)
        }

        private fun applyTexts(
            views: RemoteViews,
            progress: SpannableString,
            timePeriod: TimePeriod,
            daysLeft: Long,
            currentValue: SpannableString,
        ) {
            views.setTextViewText(R.id.widgetType, timePeriod.name)
            views.setTextViewText(R.id.widgetProgress, progress)
            views.setTextViewText(R.id.widgetDaysLeft, daysLeft.toTimePeriodText())
            views.setTextViewText(R.id.widgetCurrentValue, currentValue)
        }

        /**
         * Show only the progress bar corresponding to the provided theme and hide the rest.
         */
        private fun applyProgressForTheme(views: RemoteViews, progress: Int, theme: WidgetTheme) {
            val activeId = progressBarIdForTheme(theme)

            allProgressBarIds.forEach { id ->
                if (id == activeId) {
                    views.setViewVisibility(id, View.VISIBLE)
                    views.setProgressBar(id, 100, progress, false)
                } else {
                    views.setViewVisibility(id, View.GONE)
                }
            }
        }

        /**
         * Show only the progress bar corresponding to the provided theme and hide the rest.
         */
        private fun applyCloverProgressForTheme(views: RemoteViews, progress: Double, theme: WidgetTheme) {
            val activeId = cloverProgressBarIdForTheme(theme)

            allShapeContainerIds.forEach { id ->
                if (id == activeId) {
                    views.setViewVisibility(id, View.VISIBLE)
                    views.setImageViewResource(
                        id,
                        when (progress) {
                            in 0.0..5.0 -> R.drawable.background_clover_00
                            in 5.0..10.0 -> R.drawable.background_clover_05
                            in 10.0..20.0 -> R.drawable.background_clover_10
                            in 20.0..30.0 -> R.drawable.background_clover_20
                            in 30.0..40.0 -> R.drawable.background_clover_30
                            in 40.0..50.0 -> R.drawable.background_clover_40
                            in 50.0..60.0 -> R.drawable.background_clover_50
                            in 60.0..70.0 -> R.drawable.background_clover_60
                            in 70.0..80.0 -> R.drawable.background_clover_70
                            in 80.0..90.0 -> R.drawable.background_clover_80
                            in 90.0..95.0 -> R.drawable.background_clover_90
                            in 95.0..98.0 -> R.drawable.background_clover_95
                            else -> R.drawable.background_clover_100
                        },
                    )
                } else {
                    views.setViewVisibility(id, View.GONE)
                }
            }
        }

        /**
         * Show only the progress bar corresponding to the provided theme and hide the rest.
         */
        private fun applyPillProgressForTheme(views: RemoteViews, progress: Double, theme: WidgetTheme) {
            val activeId = cloverProgressBarIdForTheme(theme)

            allShapeContainerIds.forEach { id ->
                if (id == activeId) {
                    views.setViewVisibility(id, View.VISIBLE)
                    views.setImageViewResource(
                        id,
                        when (progress) {
                            in 0.0..5.0 -> R.drawable.background_pill_00
                            in 5.0..10.0 -> R.drawable.background_pill_05
                            in 10.0..20.0 -> R.drawable.background_pill_10
                            in 20.0..30.0 -> R.drawable.background_pill_20
                            in 30.0..40.0 -> R.drawable.background_pill_30
                            in 40.0..50.0 -> R.drawable.background_pill_40
                            in 50.0..60.0 -> R.drawable.background_pill_50
                            in 60.0..70.0 -> R.drawable.background_pill_60
                            in 70.0..80.0 -> R.drawable.background_pill_70
                            in 80.0..90.0 -> R.drawable.background_pill_80
                            in 90.0..95.0 -> R.drawable.background_pill_90
                            in 95.0..98.0 -> R.drawable.background_pill_95
                            else -> R.drawable.background_pill_100
                        },
                    )
                } else {
                    views.setViewVisibility(id, View.GONE)
                }
            }
        }

        /**
         * Map each WidgetTheme to the single progress-bar view id you want visible for that theme.
         * Extend this mapping when you add new themed progress bar variants.
         */
        private fun progressBarIdForTheme(theme: WidgetTheme): Int {
            return when (theme) {
                WidgetTheme.DEFAULT -> R.id.widgetProgressBarDefault
                WidgetTheme.GREEN -> R.id.widgetProgressBarGreen
                // add other mappings, e.g. WidgetTheme.BLUE -> R.id.widgetProgressBarBlue
                WidgetTheme.DYNAMIC -> R.id.widgetProgressBarDynamic
            }
        }

        /**
         * Returns the clover container view id for the given [WidgetTheme].
         *
         * Maps each theme to the specific clover container view that should be visible
         * when rendering the clover-style widget. Add new mappings here when new
         * themed clover containers are introduced.
         *
         * @param theme the widget theme
         * @return the resource id of the clover container view for the theme
         */
        private fun cloverProgressBarIdForTheme(theme: WidgetTheme): Int {
            return when (theme) {
                WidgetTheme.DEFAULT -> R.id.widgetContainerDefault
                WidgetTheme.GREEN -> R.id.widgetContainerGreen
                // add other mappings, e.g. WidgetTheme.BLUE -> R.id.widgetContainerBlue
                WidgetTheme.DYNAMIC -> R.id.widgetContainerDefault
            }
        }

        fun rectangularRemoteView(context: Context, yp: YearlyProgressUtil, theme: WidgetTheme, timePeriod: TimePeriod): RemoteViews {
            val views = RemoteViews(context.packageName, R.layout.standalone_widget_layout)
            val progress = yp.calculateProgress(timePeriod)
            val endTime = yp.calculateEndTime(timePeriod)
            val daysLeft = yp.calculateTimeLeft(endTime)
            val currentValue = yp.getCurrentPeriodValue(timePeriod).toFormattedTimePeriod(yp, timePeriod)

            val colors = WidgetColors.fromTheme(context, theme)

            applyTheme(views, colors)
            views.setInt(R.id.widgetContainer, "setColorFilter", colors.backgroundColor)
            applyTexts(views, progress.styleFormatted(yp.settings.decimalDigits), timePeriod, daysLeft, currentValue)
            applyProgressForTheme(views, progress.roundToInt(), theme)

            return views
        }

        fun cloverRemoteView(
            context: Context,
            yp: YearlyProgressUtil,
            theme: WidgetTheme,
            timePeriod: TimePeriod,
            options: Bundle? = null // Pass widget options if available
        ): RemoteViews {
            val large =
                RemoteViews(context.packageName, R.layout.standalone_widget_clover_layout_large)
            val square = RemoteViews(context.packageName, R.layout.standalone_widget_clover_layout)
            val small =
                RemoteViews(context.packageName, R.layout.standalone_widget_clover_layout_small)
            val xSmall = RemoteViews(
                context.packageName,
                R.layout.standalone_widget_clover_layout_extra_small
            )

            val progress = yp.calculateProgress(timePeriod)
            val endTime = yp.calculateEndTime(timePeriod)
            val daysLeft = yp.calculateTimeLeft(endTime)
            val currentValue = yp.getCurrentPeriodValue(timePeriod).toFormattedTimePeriod(yp, timePeriod)

            val colors = WidgetColors.fromTheme(context, theme)



            applyTheme(large, colors)
            applyTheme(square, colors)
            applyTheme(small, colors)
            applyTheme(xSmall, colors)


            large.setTextColor(R.id.widgetCurrentValue, colors.accentColor)
            square.setTextColor(R.id.widgetCurrentValue, colors.accentColor)
            small.setTextColor(R.id.widgetCurrentValue, colors.accentColor)
            xSmall.setTextColor(R.id.widgetCurrentValue, colors.accentColor)

            applyTexts(
                large,
                progress.styleFormatted(yp.settings.decimalDigits),
                timePeriod,
                daysLeft,
                currentValue
            )
            applyTexts(
                square,
                progress.styleFormatted(yp.settings.decimalDigits),
                timePeriod,
                daysLeft,
                currentValue
            )
            applyTexts(
                small,
                progress.styleFormatted(yp.settings.decimalDigits),
                timePeriod,
                daysLeft,
                currentValue
            )
            applyTexts(
                xSmall,
                progress.styleFormatted(yp.settings.decimalDigits),
                timePeriod,
                daysLeft,
                currentValue
            )

            applyCloverProgressForTheme(large, progress, theme)
            applyCloverProgressForTheme(square, progress, theme)
            applyCloverProgressForTheme(small, progress, theme)
            applyCloverProgressForTheme(xSmall, progress, theme)

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

        fun pillRemoteView(
            context: Context,
            yp: YearlyProgressUtil,
            theme: WidgetTheme,
            timePeriod: TimePeriod,
            options: Bundle? = null // Pass widget options if available
        ): RemoteViews {
            val large =
                RemoteViews(context.packageName, R.layout.standalone_widget_pill_layout_medium)
            val small =
                RemoteViews(context.packageName, R.layout.standalone_widget_pill_layout_small)

            val progress = yp.calculateProgress(timePeriod)
            val endTime = yp.calculateEndTime(timePeriod)
            val daysLeft = yp.calculateTimeLeft(endTime)
            val currentValue = yp.getCurrentPeriodValue(timePeriod).toFormattedTimePeriod(yp, timePeriod)

            val colors = WidgetColors.fromTheme(context, theme)



            applyTheme(large, colors)
            applyTheme(small, colors)


            large.setTextColor(R.id.widgetCurrentValue, colors.accentColor)
            small.setTextColor(R.id.widgetCurrentValue, colors.accentColor)

            applyTexts(
                large,
                progress.styleFormatted(yp.settings.decimalDigits),
                timePeriod,
                daysLeft,
                currentValue
            )

            applyTexts(
                small,
                progress.styleFormatted(yp.settings.decimalDigits),
                timePeriod,
                daysLeft,
                currentValue
            )


            applyPillProgressForTheme(large, progress, theme)
            applyPillProgressForTheme(small, progress, theme)

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

class DayWidget : StandaloneWidget(TimePeriod.DAY)
class WeekWidget : StandaloneWidget(TimePeriod.WEEK)
class MonthWidget : StandaloneWidget(TimePeriod.MONTH)
class YearWidget : StandaloneWidget(TimePeriod.YEAR)
