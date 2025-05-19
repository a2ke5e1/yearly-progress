package com.a3.yearlyprogess.components

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Typeface
import android.icu.text.SimpleDateFormat
import android.text.format.DateFormat.is24HourFormat
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import com.a3.yearlyprogess.R
import com.a3.yearlyprogess.YearlyProgressUtil
import com.a3.yearlyprogess.data.models.SunriseSunsetResponse
import com.a3.yearlyprogess.widgets.ui.util.styleFormatted
import com.google.android.material.card.MaterialCardView
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.Date
import java.util.Locale
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("ViewConstructor", "SetTextI18n")
class DayNightLightProgressView
@JvmOverloads
constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
    defStyleRes: Int = 0,
) : LinearLayout(context, attrs, defStyle, defStyleRes), CoroutineScope {
  override val coroutineContext: CoroutineContext
    get() = Dispatchers.IO + job

  private var perTextView: TextView
  private var widgetDataInfoTextView: TextView
  private var widgetDataTextView: TextView
  private var titleTextView: TextView
  private var widgetParentCard: MaterialCardView
  private var widgetProgressCard: MaterialCardView
  private var job: Job
  private var dayLight = true

  private var startTime = 0L
  private var endTime = 0L

  init {
    LayoutInflater.from(context).inflate(R.layout.progress_card_view, this, true)
    orientation = VERTICAL

    job = Job()

    perTextView = findViewById(R.id.widget_per)
    widgetDataInfoTextView = findViewById(R.id.widget_data_info)
    widgetParentCard = findViewById(R.id.material_card_parent)
    widgetProgressCard = findViewById(R.id.material_card_progress)
    widgetDataTextView = findViewById(R.id.widget_data)
    titleTextView = findViewById(R.id.widget_title)

    if (attrs != null) {
      val obtainAttributeSet =
          context.obtainStyledAttributes(attrs, R.styleable.DayNightLightProgressView)
      if (obtainAttributeSet.hasValue(R.styleable.DayNightLightProgressView_day_light)) {
        dayLight =
            obtainAttributeSet.getBoolean(R.styleable.DayNightLightProgressView_day_light, true)
      }
      obtainAttributeSet.recycle()
    }
  }

  @SuppressLint("SetTextI18n")
  private fun updateView(progress: Double) {
    val pref = PreferenceManager.getDefaultSharedPreferences(context)
    val decimalPlace: Int = pref.getInt(context.getString(R.string.app_widget_decimal_point), 13)

    perTextView.text = progress.styleFormatted(decimalPlace)

    val params = widgetProgressCard.layoutParams
    val target = (progress * 0.01 * widgetParentCard.width).toInt()
    val valueAnimator = ValueAnimator.ofInt(params.width, target)
    valueAnimator.duration = 500
    valueAnimator.addUpdateListener {
      widgetProgressCard.layoutParams.width = it.animatedValue as Int
      widgetProgressCard.requestLayout()
    }
    valueAnimator.start()
  }

  fun loadSunriseSunset(data: SunriseSunsetResponse) {
    val (startTime, endTime) = data.getStartAndEndTime(dayLight)
    this.startTime = startTime
    this.endTime = endTime

    // data that doesn't change
    titleTextView.text =
        if (dayLight) {
          ContextCompat.getString(context, R.string.day_light)
        } else {
          ContextCompat.getString(context, R.string.night_light)
        }

    // update the progress every seconds
    launch(Dispatchers.IO) {
      while (true) {
        val yp = YearlyProgressUtil(context)
        val progress: Double = yp.calculateProgress(startTime, endTime)

        val numberFormat = NumberFormat.getNumberInstance(Locale.getDefault()) as DecimalFormat
        numberFormat.maximumFractionDigits = 0

        val totalSeconds = (endTime - startTime) / 1000
        val formattedTotalSeconds = numberFormat.format(totalSeconds)

        launch(Dispatchers.Main) {
          val currentPeriodValue =
              if (dayLight) {
                context.getString(
                    R.string.today_sunrise_at_and_sunset_at,
                    startTime.toFormattedDateText(),
                    endTime.toFormattedDateText(),
                )
              } else {
                context.getString(
                    R.string.last_night_s_sunset_was_at_and_next_sunrise_will_be_at,
                    startTime.toFormattedDateText(),
                    endTime.toFormattedDateText(),
                )
              }
          widgetDataTextView.text = currentPeriodValue
          widgetDataTextView.textSize = 12f
          widgetDataTextView.setTypeface(null, Typeface.NORMAL)
          widgetDataTextView.setTextColor(
              ContextCompat.getColor(context, R.color.widget_text_color_tertiary),
          )
          widgetDataInfoTextView.text =
              context.getString(R.string.of_seconds, formattedTotalSeconds)
          updateView(progress)
        }
        delay(1000)
      }
    }
  }

  fun Long.toFormattedDateText(): String {
    val date = Date(this)
    val yp = YearlyProgressUtil(context)
    val isSystem24Hour = is24HourFormat(context)
    val format =
        if (isSystem24Hour) SimpleDateFormat("yyyy-MM-dd HH:mm", yp.getULocale())
        else SimpleDateFormat("yyyy-MM-dd hh:mm a", yp.getULocale())
    return format.format(date)
  }
}
