package com.a3.yearlyprogess.widgets.ui

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.text.SpannableString
import android.view.View
import android.widget.RemoteViews
import androidx.preference.PreferenceManager
import com.a3.yearlyprogess.MainActivity
import com.a3.yearlyprogess.R
import com.a3.yearlyprogess.TimePeriod
import com.a3.yearlyprogess.YearlyProgressManager
import com.a3.yearlyprogess.YearlyProgressManager.Companion.formatProgressStyle
import com.a3.yearlyprogess.calculateEndTime
import com.a3.yearlyprogess.calculateProgress
import com.a3.yearlyprogess.calculateStartTime
import com.a3.yearlyprogess.calculateTimeLeft
import com.a3.yearlyprogess.getCurrentPeriodValue
import com.a3.yearlyprogess.widgets.ui.util.styleFormatted
import com.a3.yearlyprogess.widgets.ui.util.toFormattedTimePeriod
import com.a3.yearlyprogess.widgets.ui.util.toTimePeriodLeftText
import kotlin.math.roundToInt

abstract class StandaloneWidget(private val widgetType: TimePeriod) :
    BaseWidget() {

    companion object {
        fun standaloneWidgetRemoteView(context: Context, widgetType: TimePeriod): RemoteViews {
            val view = RemoteViews(context.packageName, R.layout.standalone_widget_layout)
            val pref = PreferenceManager.getDefaultSharedPreferences(context)

            // Load user preferences
            val decimalPlace: Int =
                pref.getInt(context.getString(R.string.widget_widget_decimal_point), 2)
            val timeLeftCounter =
                pref.getBoolean(context.getString(R.string.widget_widget_time_left), false)
            val replaceProgressWithDaysLeft =
                pref.getBoolean(
                    context.getString(R.string.widget_widget_event_replace_progress_with_days_counter),
                    false
                )

            // Calculate progress
            val startTime = calculateStartTime(context, widgetType)
            val endTime = calculateEndTime(context, widgetType)
            val progress = calculateProgress(context, startTime, endTime)


            // Apply styles to the text
            val widgetProgressText = progress.styleFormatted(decimalPlace)
            val widgetProgressBarValue = progress.roundToInt()
            val widgetCurrentValue =
                getCurrentPeriodValue(widgetType).toFormattedTimePeriod(widgetType)
            val widgetDaysLeftCounter = calculateTimeLeft(endTime).toTimePeriodLeftText(widgetType)

            // Set text and progress bar values
            view.setTextViewText(R.id.widgetType, widgetType.name.uppercase())
            view.setTextViewText(R.id.widgetCurrentValue, widgetCurrentValue)
            view.setTextViewText(R.id.widgetDaysLeft, widgetDaysLeftCounter)
            view.setTextViewText(R.id.widgetProgress, widgetProgressText)
            view.setProgressBar(R.id.widgetProgressBar, 100, widgetProgressBarValue, false)

            view.setOnClickPendingIntent(
                R.id.background, PendingIntent.getActivity(
                    context,
                    0,
                    Intent(context, MainActivity::class.java),
                    PendingIntent.FLAG_IMMUTABLE
                )
            )


            var widgetBackgroundAlpha = pref.getInt(
                context.getString(R.string.widget_widget_background_transparency),
                100
            )
            widgetBackgroundAlpha = ((widgetBackgroundAlpha / 100.0) * 255).toInt()
            view.setInt(
                R.id.widgetContainer,
                "setImageAlpha",
                widgetBackgroundAlpha
            )
            view.setViewVisibility(
                R.id.widgetDaysLeft,
                if (timeLeftCounter && !replaceProgressWithDaysLeft) View.VISIBLE else View.GONE
            )
            if (timeLeftCounter && replaceProgressWithDaysLeft) {
                view.setTextViewText(R.id.widgetProgress, widgetDaysLeftCounter)
                view.setTextViewTextSize(R.id.widgetProgress, 0, 35f)
            }

            return view
        }
    }


    override fun updateWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        appWidgetManager.updateAppWidget(
            appWidgetId,
            standaloneWidgetRemoteView(context, widgetType)
        )
    }

}

class DayWidget : StandaloneWidget(TimePeriod.DAY)
class MonthWidget : StandaloneWidget(TimePeriod.MONTH)
class WeekWidget : StandaloneWidget(TimePeriod.WEEK)
class YearWidget : StandaloneWidget(TimePeriod.YEAR)
