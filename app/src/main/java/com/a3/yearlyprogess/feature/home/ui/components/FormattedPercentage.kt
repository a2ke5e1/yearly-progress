package com.a3.yearlyprogess.feature.home.ui.components


import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

@Composable
fun FormattedPercentage(
    progressProvider: () -> Double,
    digits: Int = 2,
    style: TextStyle,
    modifier: Modifier = Modifier
) {
    val currentProgress = progressProvider()
    val locale = Locale.getDefault()
    val formatter = remember(digits, locale) {
        val pattern = "0." + "0".repeat(digits.coerceAtLeast(0))
        DecimalFormat(pattern, DecimalFormatSymbols(locale))
    }

    val baseFontSize = style.fontSize
    val boldStyle = remember(style.fontWeight) {
        SpanStyle(fontWeight = FontWeight.Bold)
    }
    val smallStyle = remember(baseFontSize) {
        SpanStyle(fontSize = (baseFontSize.value * 0.7f).sp)
    }

    val text = buildAnnotatedString {
        val formattedNumber = formatter.format(currentProgress)
        val separator = formatter.decimalFormatSymbols.decimalSeparator
        val separatorIndex = formattedNumber.indexOf(separator)

        if (separatorIndex >= 0) {
            // Integer part (Bold)
            withStyle(boldStyle) {
                append(formattedNumber.substring(0, separatorIndex))
            }
            // Decimal part (Small) + %
            withStyle(smallStyle) {
                append(formattedNumber.substring(separatorIndex))
                append("%")
            }
        } else {
            // Whole number (Bold)
            withStyle(boldStyle) {
                append(formattedNumber)
            }
            // Percent sign (Small)
            withStyle(smallStyle) {
                append("%")
            }
        }
    }

    Text(
        text = text,
        style = style,
        modifier = modifier
    )
}
