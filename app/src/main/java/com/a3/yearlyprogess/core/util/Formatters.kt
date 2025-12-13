package com.a3.yearlyprogess.core.util


import android.content.Context
import android.icu.text.SimpleDateFormat
import android.text.Spannable
import android.text.SpannableString
import android.text.format.DateFormat
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import android.text.style.SuperscriptSpan
import androidx.core.content.ContextCompat
import com.a3.yearlyprogess.R
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt
import kotlin.time.DurationUnit
import kotlin.time.toDuration

fun Double.styleFormatted(
    digits: Int = 2,
): SpannableString {
    val numberFormat = NumberFormat.getNumberInstance(Locale.getDefault()) as DecimalFormat
    if (this.roundToInt() != 0 && this.roundToInt() != 100) {
        numberFormat.maximumFractionDigits = digits
        numberFormat.minimumFractionDigits = digits
    }
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

 fun formatEventDateTime(date: Date, isAllDay: Boolean): String {
    return if (isAllDay) {
        SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(date)
    } else {
        SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.getDefault()).format(date)
    }
}

fun formatEventDateTime(
    context: Context,
    startTime: Long,
    endTime: Long,
    allDayEvent: Boolean,
): String {
    val startDay = SimpleDateFormat.getDateInstance().format(startTime)
    val endDay = SimpleDateFormat.getDateInstance().format(endTime)

    val startTimeString =
        DateFormat.format(
            if (DateFormat.is24HourFormat(context)) "HH:mm" else "hh:mm a",
            startTime,
        )
            .toString()
            .uppercase()
    val endTimeString =
        DateFormat.format(if (DateFormat.is24HourFormat(context)) "HH:mm" else "hh:mm a", endTime)
            .toString()
            .uppercase()

    // long dash unicode is \u2014
    return if (allDayEvent) {
        "${DateFormat.format(
            "MMM dd, yyyy",
            startTime,
        )} \u2014 ${DateFormat.format("MMM dd, yyyy", endTime)} \u00B7 ${ContextCompat.getString(context, R.string.all_day)}"
    } else {
        if (startDay == endDay) {
            "${DateFormat.format("MMM dd, yyyy", startTime)} \u00B7 $startTimeString \u2014 $endTimeString"
        } else {
            "${DateFormat.format(
                "MMM dd, yyyy ",
                startTime,
            )} $startTimeString \u2014 ${DateFormat.format("MMM dd, yyyy", endTime)} $endTimeString"
        }
    }
}

fun formatEventTimeStatus(
    context: Context,
    start: Long,
    end: Long,
    dynamic: Boolean = false,
): String {
    val now = System.currentTimeMillis()

    return if (now < start) {
        val timeIn = (start - now).toTimePeriodText(dynamic)
        context.getString(R.string.time_in, timeIn)
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    } else {
        val timeLeft = (end - now).toTimePeriodText(dynamic)
        context.getString(R.string.time_left, timeLeft)
    }
}