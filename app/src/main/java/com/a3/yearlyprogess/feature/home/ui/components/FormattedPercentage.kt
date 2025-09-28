package com.a3.yearlyprogess.feature.home.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun FormattedPercentage(
    value: Double,
    digits: Int = 2,
    style: TextStyle
) {
    val numberFormat = NumberFormat.getNumberInstance(Locale.getDefault()) as DecimalFormat
    if (value.roundToInt() != 0 && value.roundToInt() != 100) {
        numberFormat.maximumFractionDigits = digits
        numberFormat.minimumFractionDigits = digits
    }
    val formattedNumber = numberFormat.format(value) + "%"
    val decimalSeparator = numberFormat.decimalFormatSymbols.decimalSeparator
    val dotPos = formattedNumber.indexOf(decimalSeparator)

    val annotatedString = buildAnnotatedString {
        if (dotPos != -1) {
            // Integer part bold
            append(formattedNumber.substring(0, dotPos))
            addStyle(
                style = SpanStyle(fontWeight = FontWeight.Bold),
                start = 0,
                end = dotPos
            )
            // Decimal + % smaller
            append(formattedNumber.substring(dotPos))
            addStyle(
                style = SpanStyle(fontSize = (style.fontSize.value * 0.7f).sp),
                start = dotPos,
                end = formattedNumber.length
            )
        } else {
            // Whole number bold
            append(formattedNumber.substring(0, formattedNumber.length - 1))
            addStyle(
                style = SpanStyle(fontWeight = FontWeight.Bold),
                start = 0,
                end = formattedNumber.length - 1
            )
            // % smaller
            append(formattedNumber.last())
            addStyle(
                style = SpanStyle(fontSize = (style.fontSize.value * 0.7f).sp),
                start = formattedNumber.length - 1,
                end = formattedNumber.length
            )
        }
    }

    Text(text = annotatedString, style = style)
}
