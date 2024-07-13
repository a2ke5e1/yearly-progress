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
import com.a3.yearlyprogess.YearlyProgressManager.Companion.formatProgressStyle
import com.a3.yearlyprogess.YearlyProgressManager
import com.a3.yearlyprogess.widgets.manager.updateManager.WidgetUpdateAlarmHandler
import com.a3.yearlyprogess.widgets.ui.util.BaseWidget
import kotlin.math.roundToInt

abstract class StandaloneWidget(private val widgetServiceType: Int) :
    BaseWidget() {

    companion object {
        fun standaloneWidgetRemoteView(context: Context, widgetServiceType: Int): RemoteViews {
            val view = RemoteViews(context.packageName, R.layout.standalone_widget_layout)

            // Set default week and calculation mode
            YearlyProgressManager(context).setDefaultWeek()
            YearlyProgressManager(context).setDefaultCalculationMode()

            val progress = YearlyProgressManager.getProgress(
                when (widgetServiceType) {
                    WidgetUpdateAlarmHandler.DAY_WIDGET_SERVICE -> YearlyProgressManager.DAY
                    WidgetUpdateAlarmHandler.MONTH_WIDGET_SERVICE -> YearlyProgressManager.MONTH
                    WidgetUpdateAlarmHandler.WEEK_WIDGET_SERVICE -> YearlyProgressManager.WEEK
                    WidgetUpdateAlarmHandler.YEAR_WIDGET_SERVICE -> YearlyProgressManager.YEAR
                    else -> -1
                }
            )

            val pref = PreferenceManager.getDefaultSharedPreferences(context)
            val decimalPlace: Int =
                pref.getInt(context.getString(R.string.widget_widget_decimal_point), 2)

            val widgetProgressText = formatProgressStyle(
                SpannableString(
                    "%,.${decimalPlace}f".format(progress) + "%"
                )
            )
            val widgetProgressBarValue = progress.roundToInt()

            val widgetType: String = when (widgetServiceType) {

                WidgetUpdateAlarmHandler.DAY_WIDGET_SERVICE -> context.getString(R.string.day)
                WidgetUpdateAlarmHandler.MONTH_WIDGET_SERVICE -> context.getString(R.string.month)
                WidgetUpdateAlarmHandler.WEEK_WIDGET_SERVICE -> context.getString(R.string.week)
                WidgetUpdateAlarmHandler.YEAR_WIDGET_SERVICE -> context.getString(R.string.year)
                else -> ""
            }
            val widgetCurrentValue = when (widgetServiceType) {
                WidgetUpdateAlarmHandler.DAY_WIDGET_SERVICE -> YearlyProgressManager.getDay(formatted = true)
                WidgetUpdateAlarmHandler.MONTH_WIDGET_SERVICE -> YearlyProgressManager.getMonth(isLong = false)
                WidgetUpdateAlarmHandler.WEEK_WIDGET_SERVICE -> YearlyProgressManager.getWeek(isLong = false)
                WidgetUpdateAlarmHandler.YEAR_WIDGET_SERVICE -> YearlyProgressManager.getYear().toString()
                else -> ""
            }

            val widgetDaysLeftCounter = YearlyProgressManager.getDaysLeft(
                when (widgetServiceType) {
                    WidgetUpdateAlarmHandler.DAY_WIDGET_SERVICE -> YearlyProgressManager.DAY
                    WidgetUpdateAlarmHandler.MONTH_WIDGET_SERVICE -> YearlyProgressManager.MONTH
                    WidgetUpdateAlarmHandler.WEEK_WIDGET_SERVICE -> YearlyProgressManager.WEEK
                    WidgetUpdateAlarmHandler.YEAR_WIDGET_SERVICE -> YearlyProgressManager.YEAR
                    else -> -1
                }
            ) + " left"


            view.setTextViewText(R.id.widgetType, widgetType)
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


            // Loads user preference if widgets needs to be transparent or
            // not. Other prefs might be loaded the same way.

            // TODO: Make a better way to load user prefs that
            //  applies to kind of the widgets.
            var widgetBackgroundAlpha = pref.getInt(
                context.getString(R.string.widget_widget_background_transparency),
                100
            )

            widgetBackgroundAlpha = ((widgetBackgroundAlpha / 100.0) * 255).toInt()

            val timeLeftCounter =
                pref.getBoolean(context.getString(R.string.widget_widget_time_left), false)

            val replaceProgressWithDaysLeft =
                pref.getBoolean(
                    context.getString(R.string.widget_widget_event_replace_progress_with_days_counter),
                    false
                )

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
            standaloneWidgetRemoteView(context, widgetServiceType)
        )
    }

}

class DayWidget : StandaloneWidget(WidgetUpdateAlarmHandler.DAY_WIDGET_SERVICE)
class MonthWidget : StandaloneWidget(WidgetUpdateAlarmHandler.MONTH_WIDGET_SERVICE)
class WeekWidget : StandaloneWidget(WidgetUpdateAlarmHandler.WEEK_WIDGET_SERVICE)
class YearWidget : StandaloneWidget(WidgetUpdateAlarmHandler.YEAR_WIDGET_SERVICE)
