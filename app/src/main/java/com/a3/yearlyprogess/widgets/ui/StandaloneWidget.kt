package com.a3.yearlyprogess.widgets.ui

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.text.SpannableString
import android.util.SizeF
import android.util.TypedValue.COMPLEX_UNIT_DIP
import android.util.TypedValue.COMPLEX_UNIT_SP
import android.view.View
import android.widget.RemoteViews
import android.widget.RemoteViews.MARGIN_TOP
import androidx.annotation.IntRange
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import com.a3.yearlyprogess.MainActivity
import com.a3.yearlyprogess.R
import com.a3.yearlyprogess.TimePeriod
import com.a3.yearlyprogess.calculateEndTime
import com.a3.yearlyprogess.calculateProgress
import com.a3.yearlyprogess.calculateStartTime
import com.a3.yearlyprogess.calculateTimeLeft
import com.a3.yearlyprogess.getCurrentPeriodValue
import com.a3.yearlyprogess.loadSunriseSunset
import com.a3.yearlyprogess.widgets.ui.util.styleFormatted
import com.a3.yearlyprogess.widgets.ui.util.toFormattedTimePeriod
import com.a3.yearlyprogess.widgets.ui.util.toTimePeriodLeftText
import kotlin.math.roundToInt

object WidgetUtils {

  fun createRemoteView(
      context: Context,
      widgetType: String,
      startTime: Long,
      endTime: Long,
      currentValue: SpannableString,
      errorMessage: String? = null,
      options: StandaloneWidgetOptions? = null
  ): RemoteViews {

    if (errorMessage != null) {
      val errorView = RemoteViews(context.packageName, R.layout.error_widget)
      errorView.setTextViewText(R.id.error_text, errorMessage)
      errorView.setOnClickPendingIntent(
          R.id.background,
          PendingIntent.getActivity(
              context, 0, Intent(context, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE))
      return errorView
    }

    val decimalPlace: Int = options?.decimalPlaces ?: 2
    val timeLeftCounter = options?.timeLeftCounter == true
    val replaceProgressWithDaysLeft = options?.replaceProgressWithDaysLeft == true
    var widgetBackgroundAlpha = options?.backgroundTransparency ?: 100
    widgetBackgroundAlpha = ((widgetBackgroundAlpha / 100.0) * 255).toInt()

    val progress = calculateProgress(context, startTime, endTime)
    val widgetDaysLeftCounter = calculateTimeLeft(endTime).toTimePeriodLeftText(context) + " left"

    fun rectangularRemoteView(): RemoteViews {
      val view = RemoteViews(context.packageName, R.layout.standalone_widget_layout)
      val widgetProgressText = progress.styleFormatted(decimalPlace)
      val widgetProgressBarValue = progress.roundToInt()

      // Set text and progress bar values
      view.setTextViewText(R.id.widgetType, widgetType)
      view.setTextViewText(R.id.widgetCurrentValue, currentValue)
      view.setTextViewText(R.id.widgetDaysLeft, widgetDaysLeftCounter)
      view.setTextViewText(R.id.widgetProgress, widgetProgressText)
      view.setProgressBar(R.id.widgetProgressBar, 100, widgetProgressBarValue, false)
      view.setFloat(R.id.widgetCurrentValue, "setTextSize", 8f)

      view.setOnClickPendingIntent(
          R.id.background,
          PendingIntent.getActivity(
              context, 0, Intent(context, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE))

      view.setInt(R.id.widgetContainer, "setImageAlpha", widgetBackgroundAlpha)
      view.setViewVisibility(
          R.id.widgetDaysLeft,
          if (timeLeftCounter && !replaceProgressWithDaysLeft) View.VISIBLE else View.GONE)
      if (timeLeftCounter && replaceProgressWithDaysLeft) {
        view.setTextViewText(R.id.widgetProgress, widgetDaysLeftCounter)
        view.setTextViewTextSize(R.id.widgetProgress, 0, 35f)
      }
      return view
    }

    fun cloverRemoteView(): RemoteViews {
      val view = RemoteViews(context.packageName, R.layout.standalone_widget_layout_clover)

      // Calculate progress
      // Apply styles to the text
      val widgetProgressText =
          progress.styleFormatted(decimalPlace.coerceIn(0, 2), cloverMode = true)

      // Set text and progress bar values
      view.setTextViewText(R.id.widgetType, widgetType)
      view.setTextViewText(R.id.widgetCurrentValue, currentValue)
      view.setTextViewText(R.id.widgetDaysLeft, widgetDaysLeftCounter)
      view.setTextViewText(R.id.widgetProgress, widgetProgressText)

      // map progress to the background_clover_progress drawable

      val progressDrawable =
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
            in 95.0..100.0 -> R.drawable.background_clover_95
            else -> R.drawable.background_clover_100
          }

      view.setImageViewResource(R.id.widgetContainer, progressDrawable)

      view.setOnClickPendingIntent(
          R.id.background,
          PendingIntent.getActivity(
              context, 0, Intent(context, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE))

      view.setViewVisibility(
          R.id.widgetDaysLeft,
          if (timeLeftCounter && !replaceProgressWithDaysLeft) View.VISIBLE else View.GONE)
      if (timeLeftCounter && replaceProgressWithDaysLeft) {
        view.setTextViewText(R.id.widgetProgress, widgetDaysLeftCounter)
        view.setTextViewTextSize(R.id.widgetProgress, 0, 35f)
      }

      if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
        return view
      }
      view.setInt(R.id.widgetContainer, "setBackgroundColor", Color.TRANSPARENT)

      return view
    }

    return when (options?.shape) {
      WidgetShape.RECTANGLE -> rectangularRemoteView()
      WidgetShape.CLOVER -> {
        val large = cloverRemoteView()
        val square = cloverRemoteView()
        val small = cloverRemoteView()
        large.apply {
          // adjust the space between the topContainer and bottomContainer
          // such that content stays inside the shape.
          //
          // Also adjust the margin between the progress text and days left
          setViewLayoutMargin(R.id.widgetDaysLeft, MARGIN_TOP, -8f, COMPLEX_UNIT_DIP)
          setViewLayoutHeight(R.id.widget_spacer, 8f, COMPLEX_UNIT_DIP)

          // adjust text size
          setTextViewTextSize(R.id.widgetType, COMPLEX_UNIT_SP, 13f)
          setTextViewTextSize(R.id.widgetCurrentValue, COMPLEX_UNIT_SP, 24f)
          setTextViewTextSize(R.id.widgetProgress, COMPLEX_UNIT_SP, 38f)
          setTextViewTextSize(R.id.widgetDaysLeft, COMPLEX_UNIT_SP, 11f)
        }
        square.apply {

          // adjust the space between the topContainer and bottomContainer
          // such that content stays inside the shape.
          //
          // Also adjust the margin between the progress text and days left
          setViewLayoutHeight(R.id.widget_spacer, 16f, COMPLEX_UNIT_DIP)
          setViewLayoutMargin(R.id.widgetDaysLeft, MARGIN_TOP, -8f, COMPLEX_UNIT_DIP)

          // adjust text size
          setTextViewTextSize(R.id.widgetType, COMPLEX_UNIT_SP, 10f)
          setTextViewTextSize(R.id.widgetCurrentValue, COMPLEX_UNIT_SP, 20f)
          setTextViewTextSize(R.id.widgetProgress, COMPLEX_UNIT_SP, 28f)
          setTextViewTextSize(R.id.widgetDaysLeft, COMPLEX_UNIT_SP, 8f)
        }
        small.apply {

          // adjust the space between the topContainer and bottomContainer
          // such that content stays inside the shape.
          //
          // Also adjust the margin between the progress text and days left
          setViewLayoutHeight(R.id.widget_spacer, 2f, COMPLEX_UNIT_DIP)
          setViewLayoutMargin(R.id.widgetDaysLeft, MARGIN_TOP, -4f, COMPLEX_UNIT_DIP)

          // adjust text size
          setTextViewTextSize(R.id.widgetType, COMPLEX_UNIT_SP, 6f)
          setTextViewTextSize(R.id.widgetCurrentValue, COMPLEX_UNIT_SP, 8f)
          setTextViewTextSize(R.id.widgetProgress, COMPLEX_UNIT_SP, 16f)
          setTextViewTextSize(R.id.widgetDaysLeft, COMPLEX_UNIT_SP, 4f)
        }

        val viewMapping: Map<SizeF, RemoteViews> =
            mapOf(
                SizeF(220f, 220f) to large,
                SizeF(160f, 160f) to square,
                SizeF(100f, 100f) to small,
            )
        RemoteViews(viewMapping)
      }
      WidgetShape.PILL -> rectangularRemoteView()
      else -> rectangularRemoteView()
    }
  }
}

enum class WidgetShape {
  RECTANGLE,
  CLOVER,
  PILL
}

data class StandaloneWidgetOptions(
    val widgetId: Int,
    val decimalPlaces: Int,
    val timeLeftCounter: Boolean,
    val dynamicLeftCounter: Boolean,
    val replaceProgressWithDaysLeft: Boolean,
    @IntRange(from = 0, to = 100) val backgroundTransparency: Int,
    val widgetType: TimePeriod?,
    val shape: WidgetShape
) {
  companion object {
    private const val WIDGET_TYPE = "widget_type_"
    private const val WIDGET_SHAPE = "widget_shape_"

    fun load(context: Context, widgetId: Int): StandaloneWidgetOptions {
      val pref = PreferenceManager.getDefaultSharedPreferences(context)
      val decimalPlaces = pref.getInt(context.getString(R.string.widget_widget_decimal_point), 2)
      val timeLeftCounter =
          pref.getBoolean(context.getString(R.string.widget_widget_time_left), false)
      val dynamicLeftCounter =
          pref.getBoolean(context.getString(R.string.widget_widget_use_dynamic_time_left), false)
      val replaceProgressWithDaysLeft =
          pref.getBoolean(
              context.getString(R.string.widget_widget_event_replace_progress_with_days_counter),
              false)
      val backgroundTransparency =
          pref.getInt(context.getString(R.string.widget_widget_background_transparency), 100)
      val widgetType =
          pref.getString("$WIDGET_TYPE$widgetId", TimePeriod.DAY.name)?.let {
            TimePeriod.valueOf(it)
          }
      val shape =
          pref.getString("$WIDGET_SHAPE$widgetId", WidgetShape.RECTANGLE.name)?.let {
            WidgetShape.valueOf(it)
          } ?: WidgetShape.RECTANGLE
      return StandaloneWidgetOptions(
          widgetId,
          decimalPlaces,
          timeLeftCounter,
          dynamicLeftCounter,
          replaceProgressWithDaysLeft,
          backgroundTransparency,
          widgetType,
          shape)
    }
  }

  fun save(context: Context) {
    val pref = PreferenceManager.getDefaultSharedPreferences(context)
    pref
        .edit()
        .putInt(context.getString(R.string.widget_widget_decimal_point), decimalPlaces)
        .putBoolean(context.getString(R.string.widget_widget_time_left), timeLeftCounter)
        .putBoolean(
            context.getString(R.string.widget_widget_use_dynamic_time_left), dynamicLeftCounter)
        .putBoolean(
            context.getString(R.string.widget_widget_event_replace_progress_with_days_counter),
            replaceProgressWithDaysLeft)
        .putInt(
            context.getString(R.string.widget_widget_background_transparency),
            backgroundTransparency)
        .putString("$WIDGET_TYPE$widgetId", widgetType?.name)
        .putString("$WIDGET_SHAPE$widgetId", shape.name)
        .apply()
  }
}

abstract class StandaloneWidget(private val widgetType: TimePeriod) : BaseWidget() {

  companion object {

    fun standaloneWidgetRemoteView(
        context: Context,
        options: StandaloneWidgetOptions
    ): RemoteViews {
      val widgetType = options.widgetType ?: TimePeriod.DAY
      val startTime = calculateStartTime(context, widgetType)
      val endTime = calculateEndTime(context, widgetType)
      val currentValue = getCurrentPeriodValue(widgetType).toFormattedTimePeriod(widgetType)

      val widgetTitleText =
          when (widgetType) {
            TimePeriod.DAY -> context.getString(R.string.day)
            TimePeriod.WEEK -> context.getString(R.string.week)
            TimePeriod.MONTH -> context.getString(R.string.month)
            TimePeriod.YEAR -> context.getString(R.string.year)
          }

      val remoteView =
          WidgetUtils.createRemoteView(
              context,
              widgetTitleText,
              startTime,
              endTime,
              SpannableString(currentValue),
              options = options)

      return remoteView
    }
  }

  override fun updateWidget(
      context: Context,
      appWidgetManager: AppWidgetManager,
      appWidgetId: Int
  ) {
    val options =
        StandaloneWidgetOptions.load(context, appWidgetId)
            .copy(
                widgetType = widgetType,
            )
    appWidgetManager.updateAppWidget(appWidgetId, standaloneWidgetRemoteView(context, options))
  }
}

abstract class DayNightWidget(private val dayLight: Boolean) : BaseWidget() {

  companion object {
    fun dayNightLightWidgetRemoteView(
        context: Context,
        dayLight: Boolean,
        options: StandaloneWidgetOptions
    ): RemoteViews {
      if (ContextCompat.checkSelfPermission(
          context, android.Manifest.permission.ACCESS_COARSE_LOCATION) !=
          PackageManager.PERMISSION_GRANTED) {

        val errorView =
            WidgetUtils.createRemoteView(
                context,
                if (dayLight) ContextCompat.getString(context, R.string.day_light)
                else ContextCompat.getString(context, R.string.night_light),
                0,
                0,
                SpannableString(""),
                ContextCompat.getString(context, R.string.no_location_permission))
        return errorView
      }

      val sunriseSunset = loadSunriseSunset(context)

      if (sunriseSunset == null) {
        val errorView =
            WidgetUtils.createRemoteView(
                context,
                if (dayLight) ContextCompat.getString(context, R.string.day_light)
                else ContextCompat.getString(context, R.string.night_light),
                0,
                0,
                SpannableString(""),
                "No data, Tap to retry")
        return errorView
      }

      val (startTime, endTime) = sunriseSunset.getStartAndEndTime(dayLight)
      val currentValue =
          if (dayLight) "🌇 ${sunriseSunset.results[1].sunset}"
          else "🌅 ${sunriseSunset.results[1].sunrise}"

      val remoteView =
          WidgetUtils.createRemoteView(
              context,
              if (dayLight) ContextCompat.getString(context, R.string.day_light)
              else ContextCompat.getString(context, R.string.night_light),
              startTime,
              endTime,
              SpannableString(currentValue),
              options = options)

      return remoteView
    }
  }

  override fun updateWidget(
      context: Context,
      appWidgetManager: AppWidgetManager,
      appWidgetId: Int
  ) {
    val options =
        StandaloneWidgetOptions.load(context, appWidgetId)
            .copy(
                widgetType = null,
            )
    appWidgetManager.updateAppWidget(
        appWidgetId, dayNightLightWidgetRemoteView(context, dayLight, options))
  }
}

class DayLightWidget : DayNightWidget(true)

class NightLightWidget : DayNightWidget(false)

class DayWidget : StandaloneWidget(TimePeriod.DAY)

class MonthWidget : StandaloneWidget(TimePeriod.MONTH)

class WeekWidget : StandaloneWidget(TimePeriod.WEEK)

class YearWidget : StandaloneWidget(TimePeriod.YEAR)
