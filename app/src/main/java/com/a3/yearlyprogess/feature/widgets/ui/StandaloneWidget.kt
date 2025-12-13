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
import com.a3.yearlyprogess.feature.widgets.util.WidgetProgressRenderer
import kotlin.math.roundToInt

open class StandaloneWidget(
    private val timePeriod: TimePeriod,
) : BaseWidget() {

    override fun updateWidget(context: Context, appWidgetId: Int): RemoteViews {
        val yp = YearlyProgressUtil()
        val theme: WidgetTheme = WidgetTheme.DEFAULT
        val manager = AppWidgetManager.getInstance(context)
        val options = manager.getAppWidgetOptions(appWidgetId)

        val views = rectangularRemoteView(context, yp, theme, timePeriod)
        return views
    }

    companion object {
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
            WidgetProgressRenderer.applyLinearProgressBar(views, progress.roundToInt(), theme)

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

            WidgetProgressRenderer.applyCloverProgressContainer(large, progress, theme)
            WidgetProgressRenderer.applyCloverProgressContainer(square, progress, theme)
            WidgetProgressRenderer.applyCloverProgressContainer(small, progress, theme)
            WidgetProgressRenderer.applyCloverProgressContainer(xSmall, progress, theme)

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


            WidgetProgressRenderer.applyPillProgressContainer(large, progress, theme)
            WidgetProgressRenderer.applyPillProgressContainer(small, progress, theme)

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
