package com.a3.yearlyprogess.mwidgets.util

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.text.Spannable
import android.text.SpannableString
import android.text.style.RelativeSizeSpan
import android.widget.RemoteViews
import com.a3.yearlyprogess.MainActivity
import com.a3.yearlyprogess.R
import com.a3.yearlyprogess.helper.ProgressPercentage
import com.a3.yearlyprogess.helper.ProgressPercentage.Companion.format
import com.a3.yearlyprogess.manager.AlarmHandler
import kotlin.math.roundToInt

abstract class StandaloneWidget(private val widgetServiceType: Int) : BaseWidget(widgetServiceType) {

    private lateinit var  widgetType: String
    private lateinit var widgetCurrentValue: String
    private lateinit var widgetProgressText: SpannableString
    private var  widgetProgressBarValue: Int = 0


    override fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val progressPercentage = ProgressPercentage()
        val progress = progressPercentage.getPercent(
            when (widgetServiceType) {
                AlarmHandler.DAY_WIDGET_SERVICE -> ProgressPercentage.DAY
                AlarmHandler.MONTH_WIDGET_SERVICE -> ProgressPercentage.MONTH
                AlarmHandler.WEEK_WIDGET_SERVICE -> ProgressPercentage.WEEK
                AlarmHandler.YEAR_WIDGET_SERVICE -> ProgressPercentage.YEAR
                else -> -1
            }
        )

        widgetProgressText = formatProgressStyle(progress)
        widgetProgressBarValue = progress.roundToInt()

        when (widgetServiceType) {

            AlarmHandler.DAY_WIDGET_SERVICE -> {
                widgetType = context.getString(R.string.day)
                widgetCurrentValue = progressPercentage.getDay(custom = true)
            }
            AlarmHandler.MONTH_WIDGET_SERVICE -> {
                widgetType = context.getString(R.string.month)
                widgetCurrentValue = progressPercentage.getMonth(str = true)
            }
            AlarmHandler.WEEK_WIDGET_SERVICE -> {
                widgetType = context.getString(R.string.week)
                widgetCurrentValue = progressPercentage.getWeek(str = true)
            }
            AlarmHandler.YEAR_WIDGET_SERVICE -> {
                widgetType = context.getString(R.string.year)
                widgetCurrentValue = progressPercentage.getYear()
            }
            else -> {
                widgetType = ""
                widgetCurrentValue = ""
                widgetProgressText = SpannableString("")
                widgetProgressBarValue = 0
            }
        }

        super.updateAppWidget(context, appWidgetManager, appWidgetId)
    }

    override fun updateWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {

        val view = RemoteViews(context.packageName, R.layout.standalone_widget_layout)

        view.setTextViewText(R.id.widgetType, widgetType)
        view.setTextViewText(R.id.widgetCurrentValue, widgetCurrentValue)
        view.setTextViewText(R.id.widgetProgress, widgetProgressText)
        view.setProgressBar(R.id.widgetProgressBar, 100,widgetProgressBarValue, false)

        view.setOnClickPendingIntent(android.R.id.background, PendingIntent.getActivity(
            context, 0, Intent(context, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE
        ))

        appWidgetManager.updateAppWidget(appWidgetId, view)

    }

    companion object {
        fun formatProgressStyle(progress: Double): SpannableString {
            val widgetText = SpannableString("${progress.format(2)}%")
            widgetText.setSpan(
                RelativeSizeSpan(0.7f),
                widgetText.indexOf('.'),
                widgetText.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            return widgetText
        }
    }
}