package com.a3.yearlyprogess.widgets.ui

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
import com.a3.yearlyprogess.TimePeriod
import com.a3.yearlyprogess.calculateEndTime
import com.a3.yearlyprogess.calculateProgress
import com.a3.yearlyprogess.calculateStartTime
import com.a3.yearlyprogess.getCurrentPeriodValue
import com.a3.yearlyprogess.widgets.ui.util.styleFormatted
import com.a3.yearlyprogess.widgets.ui.util.toFormattedTimePeriod

/** Implementation of App Widget functionality. */
class AllInWidget : BaseWidget() {

  companion object {

    private fun calculateProgress(context: Context, timePeriod: TimePeriod): Double {
      val startTime = calculateStartTime(context, timePeriod)
      val endTime = calculateEndTime(context, timePeriod)
      val progress = calculateProgress(context, startTime, endTime)
      return progress
    }

    private fun initiateView(context: Context, views: RemoteViews) {

      val dayProgress = calculateProgress(context, TimePeriod.DAY)
      val weekProgress = calculateProgress(context, TimePeriod.WEEK)
      val monthProgress = calculateProgress(context, TimePeriod.MONTH)
      val yearProgress = calculateProgress(context, TimePeriod.YEAR)

      val dayCurrentValue =
          getCurrentPeriodValue(TimePeriod.DAY).toFormattedTimePeriod(TimePeriod.DAY)
      val weekCurrentValue =
          getCurrentPeriodValue(TimePeriod.WEEK).toFormattedTimePeriod(TimePeriod.WEEK)
      val monthCurrentValue =
          getCurrentPeriodValue(TimePeriod.MONTH).toFormattedTimePeriod(TimePeriod.MONTH)
      val yearCurrentValue =
          getCurrentPeriodValue(TimePeriod.YEAR).toFormattedTimePeriod(TimePeriod.YEAR)

      views.setTextViewText(R.id.progressTextDay, dayProgress.styleFormatted(0))
      views.setTextViewText(R.id.progressTextWeek, weekProgress.styleFormatted(0))
      views.setTextViewText(R.id.progressTextMonth, monthProgress.styleFormatted(0))
      views.setTextViewText(R.id.progressTextYear, yearProgress.styleFormatted(0))

      views.setProgressBar(R.id.progressBarDay, 100, dayProgress.toInt(), false)
      views.setProgressBar(R.id.progressBarWeek, 100, weekProgress.toInt(), false)
      views.setProgressBar(R.id.progressBarMonth, 100, monthProgress.toInt(), false)
      views.setProgressBar(R.id.progressBarYear, 100, yearProgress.toInt(), false)

      views.setTextViewText(R.id.progressTitle, dayCurrentValue)
      views.setTextViewText(R.id.progressWeekTitle, weekCurrentValue)
      views.setTextViewText(R.id.progressMonthTitle, monthCurrentValue)
      views.setTextViewText(R.id.progressYearTitle, yearCurrentValue)

      views.setOnClickPendingIntent(
          R.id.gridLayout,
          PendingIntent.getActivity(
              context, 0, Intent(context, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE))

      views.setViewVisibility(R.id.testDay, View.VISIBLE)
      views.setViewVisibility(R.id.testWeek, View.VISIBLE)
      views.setViewVisibility(R.id.testMonth, View.VISIBLE)
      views.setViewVisibility(R.id.testYear, View.VISIBLE)

      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        views.setInt(R.id.gridLayout, "setColumnCount", 4)
      }
    }

    fun AllInOneWidgetRemoteView(context: Context): RemoteViews {
      val xlarge = RemoteViews(context.packageName, R.layout.all_in_widget)
      initiateView(context, xlarge)

      if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
        return xlarge
      }

      val small = RemoteViews(context.packageName, R.layout.all_in_widget)
      val medium = RemoteViews(context.packageName, R.layout.all_in_widget)
      val large = RemoteViews(context.packageName, R.layout.all_in_widget)
      val square = RemoteViews(context.packageName, R.layout.all_in_widget)
      val tall = RemoteViews(context.packageName, R.layout.all_in_widget)

      initiateView(context, small)
      initiateView(context, medium)
      initiateView(context, large)
      initiateView(context, square)
      initiateView(context, tall)

      small.setViewVisibility(R.id.testWeek, View.GONE)
      small.setViewVisibility(R.id.testMonth, View.GONE)
      small.setViewVisibility(R.id.testYear, View.GONE)

      medium.setViewVisibility(R.id.testWeek, View.GONE)
      medium.setViewVisibility(R.id.testYear, View.GONE)

      large.setViewVisibility(R.id.testWeek, View.GONE)

      square.setInt(R.id.gridLayout, "setColumnCount", 2)
      tall.setInt(R.id.gridLayout, "setColumnCount", 1)

      // Instruct the widget manager to update the widget

      val viewMapping: Map<SizeF, RemoteViews> =
          mapOf(
              SizeF(300f, 80f) to xlarge,
              SizeF(220f, 80f) to large,
              SizeF(130f, 130f) to square,
              SizeF(102f, 276f) to tall,
              SizeF(160f, 80f) to medium,
              SizeF(100f, 80f) to small,
          )

      return RemoteViews(viewMapping)
    }
  }

  override fun updateWidget(
      context: Context,
      appWidgetManager: AppWidgetManager,
      appWidgetId: Int
  ) {
    appWidgetManager.updateAppWidget(appWidgetId, AllInOneWidgetRemoteView(context))
  }
}
