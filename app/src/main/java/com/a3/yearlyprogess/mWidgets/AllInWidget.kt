package com.a3.yearlyprogess.mWidgets

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.SizeF
import android.view.View
import android.widget.RemoteViews
import com.a3.yearlyprogess.MainActivity
import com.a3.yearlyprogess.R
import com.a3.yearlyprogess.helper.ProgressPercentage.Companion.formatProgress
import com.a3.yearlyprogess.helper.ProgressPercentage
import com.a3.yearlyprogess.manager.AlarmHandler
import com.a3.yearlyprogess.mWidgets.util.BaseWidget
import java.util.*
import kotlin.math.roundToInt

/**
 * Implementation of App Widget functionality.
 */
class AllInWidget : BaseWidget(AlarmHandler.ALL_IN_WIDGET_SERVICE) {
    override fun updateWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val small = RemoteViews(context.packageName, R.layout.all_in_widget)
        val medium = RemoteViews(context.packageName, R.layout.all_in_widget)
        val large = RemoteViews(context.packageName, R.layout.all_in_widget)
        val xlarge = RemoteViews(context.packageName, R.layout.all_in_widget)

        initiateView(context, small)
        initiateView(context, medium)
        initiateView(context, large)
        initiateView(context, xlarge)

        small.setViewVisibility(R.id.testWeek, View.GONE)
        small.setViewVisibility(R.id.testMonth, View.GONE)
        small.setViewVisibility(R.id.testYear, View.GONE)

        medium.setViewVisibility(R.id.testWeek, View.GONE)
        medium.setViewVisibility(R.id.testYear, View.GONE)

        large.setViewVisibility(R.id.testWeek, View.GONE)


        // Instruct the widget manager to update the widget

        var remoteViews = xlarge
        if (Build.VERSION.SDK_INT > 30) {


            val viewMapping: Map<SizeF, RemoteViews> = mapOf(
                SizeF(300f, 80f) to xlarge,
                SizeF(220f, 80f) to large,
                SizeF(160f, 80f) to medium,
                SizeF(100f, 80f) to small,
            )

            remoteViews = RemoteViews(viewMapping)
        }

        appWidgetManager.updateAppWidget(appWidgetId, remoteViews)
    }


    private fun initiateView(context: Context, views: RemoteViews) {

        val dayProgress = ProgressPercentage.getProgress(ProgressPercentage.DAY).roundToInt()
        val weekProgress = ProgressPercentage.getProgress(ProgressPercentage.WEEK).roundToInt()
        val monthProgress = ProgressPercentage.getProgress(ProgressPercentage.MONTH).roundToInt()
        val yearProgress = ProgressPercentage.getProgress(ProgressPercentage.YEAR).roundToInt()



        views.setTextViewText(R.id.progressTextDay, formatProgress(dayProgress))
        views.setTextViewText(R.id.progressTextWeek, formatProgress(weekProgress))
        views.setTextViewText(R.id.progressTextMonth, formatProgress(monthProgress))
        views.setTextViewText(R.id.progressTextYear, formatProgress(yearProgress))

        views.setProgressBar(R.id.progressBarDay, 100, dayProgress, false)
        views.setProgressBar(R.id.progressBarWeek, 100, weekProgress, false)
        views.setProgressBar(R.id.progressBarMonth, 100, monthProgress, false)
        views.setProgressBar(R.id.progressBarYear, 100, yearProgress, false)


        views.setTextViewText(R.id.progressTitle, ProgressPercentage.getDay(formatted = true))
        views.setTextViewText(
            R.id.progressWeekTitle,
            ProgressPercentage.getWeek(isLong = false)
        )
        views.setTextViewText(
            R.id.progressMonthTitle,
            ProgressPercentage.getMonth(isLong = false)
        )
        views.setTextViewText(R.id.progressYearTitle, ProgressPercentage.getYear().toString())

        views.setOnClickPendingIntent(
            R.id.gridLayout, PendingIntent.getActivity(
                context, 0, Intent(context, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE
            )
        )


        views.setViewVisibility(R.id.testDay, View.VISIBLE)
        views.setViewVisibility(R.id.testWeek, View.VISIBLE)
        views.setViewVisibility(R.id.testMonth, View.VISIBLE)
        views.setViewVisibility(R.id.testYear, View.VISIBLE)
    }

}



