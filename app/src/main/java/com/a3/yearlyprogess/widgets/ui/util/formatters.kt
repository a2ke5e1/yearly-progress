package com.a3.yearlyprogess.widgets.ui.util

import android.content.Context
import android.text.Spannable
import android.text.SpannableString
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import android.text.style.SuperscriptSpan
import com.a3.yearlyprogess.TimePeriod
import com.a3.yearlyprogess.YearlyProgressUtil
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.Locale
import kotlin.time.DurationUnit
import kotlin.time.toDuration

fun Double.styleFormatted(
    digits: Int = 2,
    cloverMode: Boolean = false,
): SpannableString {
  val numberFormat = NumberFormat.getNumberInstance(Locale.getDefault()) as DecimalFormat
  numberFormat.maximumFractionDigits = digits
  val formattedNumber = numberFormat.format(this) + "%"
  val decimalSeparator = numberFormat.decimalFormatSymbols.decimalSeparator

  val dotPos = formattedNumber.indexOf(decimalSeparator)
  val spannable = SpannableString(formattedNumber)
  if (dotPos != -1) {
    spannable.setSpan(
        StyleSpan(android.graphics.Typeface.BOLD),
        0,
        dotPos,
        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE,
    )
    spannable.setSpan(
        RelativeSizeSpan(0.7f),
        dotPos,
        formattedNumber.length,
        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE,
    )
  } else {
    spannable.setSpan(
        StyleSpan(android.graphics.Typeface.BOLD),
        0,
        formattedNumber.length - 1,
        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE,
    )

    spannable.setSpan(
        RelativeSizeSpan(0.7f),
        formattedNumber.length - 1,
        formattedNumber.length,
        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE,
    )
  }
  return spannable
}

fun Int.formattedDay(context: Context): SpannableString {
  val yp = YearlyProgressUtil(context)
  val ordinalSuffix = yp.getOrdinalSuffix(this)

  val numberFormat = NumberFormat.getNumberInstance(Locale.getDefault()) as DecimalFormat
  numberFormat.maximumFractionDigits = 0
  val formattedNumber = numberFormat.format(this)

  val stringBuilder = StringBuilder()
  stringBuilder.append(formattedNumber)
  stringBuilder.append(ordinalSuffix)

  val spannable = SpannableString(stringBuilder.toString())
  spannable.setSpan(
      SuperscriptSpan(),
      spannable.length - 2,
      spannable.length,
      Spannable.SPAN_EXCLUSIVE_EXCLUSIVE,
  )

  spannable.setSpan(
      RelativeSizeSpan(0.5f),
      spannable.length - 2,
      spannable.length,
      Spannable.SPAN_EXCLUSIVE_EXCLUSIVE,
  )

  return spannable
}

fun Int.toFormattedTimePeriod(context: Context, timePeriod: TimePeriod): SpannableString {
  val yp = YearlyProgressUtil(context)
  return when (timePeriod) {
    TimePeriod.DAY -> this.formattedDay(context)
    TimePeriod.MONTH -> SpannableString(yp.getMonthName(this))
    TimePeriod.WEEK -> SpannableString(yp.getWeekDayName(this))
    else -> {
      val numberFormat = NumberFormat.getNumberInstance(Locale.getDefault()) as DecimalFormat
      numberFormat.maximumFractionDigits = 0

      // Don't add commas to the number
      numberFormat.isGroupingUsed = false

      val formattedNumber = numberFormat.format(this)
      SpannableString(formattedNumber)
    }
  }
}

fun Long.toTimePeriodText(dynamicTimeLeft: Boolean = false): String {
  if (dynamicTimeLeft) {
    val decimalPlaces = 0
    this.toDuration(DurationUnit.MILLISECONDS).toComponents {
        days,
        hours,
        minutes,
        seconds,
        nanoseconds,
      ->
      if (days > 0) {
        return this.toDuration(DurationUnit.MILLISECONDS)
            .toString(DurationUnit.DAYS, decimals = decimalPlaces)
      }
      if (hours > 0) {
        return this.toDuration(DurationUnit.MILLISECONDS)
            .toString(DurationUnit.HOURS, decimals = decimalPlaces)
      }
      if (minutes > 0) {
        return this.toDuration(DurationUnit.MILLISECONDS)
            .toString(DurationUnit.MINUTES, decimals = decimalPlaces)
      }

      if (seconds >= 0) {
        return this.toDuration(DurationUnit.MILLISECONDS)
            .toString(DurationUnit.SECONDS, decimals = decimalPlaces)
      }
    }
  }

  val stringBuilder = StringBuilder()
  this.toDuration(DurationUnit.MILLISECONDS).toComponents {
      days,
      hours,
      minutes,
      seconds,
      nanoseconds,
    ->
    if (days > 0) {
      stringBuilder.append(days)
      stringBuilder.append("d ")
    }

    if (days > 0 || hours > 0) {
      stringBuilder.append(hours)
      stringBuilder.append("h ")
    }

    if (days > 0 || hours > 0 || minutes > 0) {
      stringBuilder.append(minutes)
      stringBuilder.append("m ")
    }

    if (days > 0 || hours > 0 || minutes > 0 || seconds > 0) {
      stringBuilder.append(seconds)
      stringBuilder.append("s")
    }
  }

  if (stringBuilder.isEmpty()) {
    stringBuilder.append("0s")
  }

  return stringBuilder.toString()
}
