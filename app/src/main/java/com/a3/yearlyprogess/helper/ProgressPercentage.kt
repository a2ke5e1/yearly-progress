package com.a3.yearlyprogess.helper

import android.text.Spannable
import android.text.SpannableString
import android.text.style.RelativeSizeSpan
import android.text.style.SuperscriptSpan
import java.util.*

class ProgressPercentage {
    companion object {

        fun formatProgressStyle(progress: Double): SpannableString {
            val widgetText = SpannableString("%,.2f".format(progress) +"%")
            return formatProgressStyle(widgetText)
        }

        fun formatProgressStyle(widgetText: SpannableString): SpannableString {

            var dotPos = widgetText.indexOf('.')
            if (dotPos == -1) {
                dotPos = widgetText.indexOf(',')
            }

            widgetText.setSpan(
                RelativeSizeSpan(0.7f),
                dotPos,
                widgetText.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            return widgetText
        }

        fun formatProgress(progress: Int): SpannableString {
            val spannable = SpannableString("${progress}%")
            spannable.setSpan(
                RelativeSizeSpan(0.7f),
                spannable.length - 1,
                spannable.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            return spannable
        }


    }
}