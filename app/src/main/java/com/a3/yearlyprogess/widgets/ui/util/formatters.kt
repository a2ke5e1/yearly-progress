package com.a3.yearlyprogess.widgets.ui.util


import android.text.Spannable
import android.text.SpannableString
import android.text.SpannedString
import android.text.style.RelativeSizeSpan
import android.text.style.SuperscriptSpan
import com.a3.yearlyprogess.TimePeriod
import com.a3.yearlyprogess.YearlyProgressManager.Companion.getDay
import com.a3.yearlyprogess.getMonthName
import com.a3.yearlyprogess.getOrdinalSuffix
import com.a3.yearlyprogess.getWeekDayName
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.Locale
import kotlin.time.DurationUnit
import kotlin.time.toDuration

fun Double.styleFormatted(digits: Int = 2): SpannableString {
    val numberFormat = NumberFormat.getNumberInstance(Locale.getDefault()) as DecimalFormat
    numberFormat.maximumFractionDigits = digits
    val formattedNumber = numberFormat.format(this) + "%"
    val decimalSeparator = numberFormat.decimalFormatSymbols.decimalSeparator

    val dotPos = formattedNumber.indexOf(decimalSeparator)
    val spannable = SpannableString(formattedNumber)
    if (dotPos != -1) {
        spannable.setSpan(
            RelativeSizeSpan(0.7f),
            dotPos,
            formattedNumber.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
    } else {
        spannable.setSpan(
            RelativeSizeSpan(0.7f),
            formattedNumber.length - 1,
            formattedNumber.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
    }
    return spannable

}

fun Int.formattedDay(): SpannableString {
    val ordinalSuffix = getOrdinalSuffix(this)

    val stringBuilder = StringBuilder()
    stringBuilder.append(this)
    stringBuilder.append(ordinalSuffix)


    val spannable = SpannableString(stringBuilder.toString())
    spannable.setSpan(
        SuperscriptSpan(),
        spannable.length - 2,
        spannable.length,
        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE

    )
    spannable.setSpan(
        RelativeSizeSpan(0.5f),
        spannable.length - 2,
        spannable.length,
        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE

    )

    return spannable
}


fun Int.toFormattedTimePeriod(
    timePeriod: TimePeriod,
): SpannableString {
    return when (timePeriod) {
        TimePeriod.DAY -> this.formattedDay()
        TimePeriod.MONTH -> SpannableString(getMonthName(this))
        TimePeriod.WEEK -> SpannableString(getWeekDayName(this))
        else -> SpannableString(this.toString())
    }
}

fun Long.toTimePeriodLeftText(timePeriod: TimePeriod): String {

    val stringBuilder = StringBuilder()
    this.toDuration(DurationUnit.MILLISECONDS)
        .toComponents { days, hours, minutes, seconds, nanoseconds ->
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

    return stringBuilder.toString()


}
