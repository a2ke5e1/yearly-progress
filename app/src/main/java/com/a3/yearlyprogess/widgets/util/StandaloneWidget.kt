package com.a3.yearlyprogess.widgets.util

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.text.SpannableString
import android.widget.RemoteViews
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.preference.PreferenceManager
import com.a3.yearlyprogess.MainActivity
import com.a3.yearlyprogess.R
import com.a3.yearlyprogess.helper.ProgressPercentage.Companion.formatProgressStyle
import com.a3.yearlyprogess.helper.ProgressPercentage
import com.a3.yearlyprogess.manager.AlarmHandler
import kotlin.math.roundToInt

abstract class StandaloneWidget(private val widgetServiceType: Int) :
    BaseWidget(widgetServiceType) {

    companion object {
        fun standaloneWidgetRemoteView(context: Context, widgetServiceType: Int): RemoteViews {
            val view = RemoteViews(context.packageName, R.layout.standalone_widget_layout)

            // Set default week and calculation mode
            ProgressPercentage(context).setDefaultWeek()
            ProgressPercentage(context).setDefaultCalculationMode()

            val progress = ProgressPercentage.getProgress(
                when (widgetServiceType) {
                    AlarmHandler.DAY_WIDGET_SERVICE -> ProgressPercentage.DAY
                    AlarmHandler.MONTH_WIDGET_SERVICE -> ProgressPercentage.MONTH
                    AlarmHandler.WEEK_WIDGET_SERVICE -> ProgressPercentage.WEEK
                    AlarmHandler.YEAR_WIDGET_SERVICE -> ProgressPercentage.YEAR
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

                AlarmHandler.DAY_WIDGET_SERVICE -> context.getString(R.string.day)
                AlarmHandler.MONTH_WIDGET_SERVICE -> context.getString(R.string.month)
                AlarmHandler.WEEK_WIDGET_SERVICE -> context.getString(R.string.week)
                AlarmHandler.YEAR_WIDGET_SERVICE -> context.getString(R.string.year)
                else -> ""
            }
            val widgetCurrentValue = when (widgetServiceType) {
                AlarmHandler.DAY_WIDGET_SERVICE -> ProgressPercentage.getDay(formatted = true)
                AlarmHandler.MONTH_WIDGET_SERVICE -> ProgressPercentage.getMonth(isLong = false)
                AlarmHandler.WEEK_WIDGET_SERVICE -> ProgressPercentage.getWeek(isLong = false)
                AlarmHandler.YEAR_WIDGET_SERVICE -> ProgressPercentage.getYear().toString()
                else -> ""
            }

            val widgetDaysLeftCounter = ProgressPercentage.getDaysLeft(
                when (widgetServiceType) {
                    AlarmHandler.DAY_WIDGET_SERVICE -> ProgressPercentage.DAY
                    AlarmHandler.MONTH_WIDGET_SERVICE -> ProgressPercentage.MONTH
                    AlarmHandler.WEEK_WIDGET_SERVICE -> ProgressPercentage.WEEK
                    AlarmHandler.YEAR_WIDGET_SERVICE -> ProgressPercentage.YEAR
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
            val widgetBackgroundAlpha = pref.getInt(
                context.getString(R.string.widget_widget_background_transparency),
                255
            )
           view.setInt(
                R.id.widgetContainer,
                "setImageAlpha",
                widgetBackgroundAlpha
            )


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