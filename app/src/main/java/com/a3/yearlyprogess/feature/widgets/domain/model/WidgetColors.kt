package com.a3.yearlyprogess.feature.widgets.domain.model

import android.content.Context
import android.os.Build
import com.a3.yearlyprogess.R

data class WidgetColors(
    val backgroundColor: Int,
    val backgroundLowColor: Int,
    val primaryColor: Int,
    val secondaryColor: Int,
    val accentColor: Int
) {
    companion object {
        fun fromTheme(context: Context, theme: WidgetTheme): WidgetColors {
            val res = context.resources
            val ta = context.obtainStyledAttributes(theme.themeRes, R.styleable.WidgetTheme)

            val colors = WidgetColors(
                backgroundColor = ta.getColor(
                    R.styleable.WidgetTheme_widgetBackgroundColor,
                    res.getColor(R.color.widget_default_background_color, null)
                ),
                backgroundLowColor = ta.getColor(
                    R.styleable.WidgetTheme_widgetBackgroundLowColor,
                    res.getColor(R.color.widget_default_background_low_color, null)
                ),
                primaryColor = ta.getColor(
                    R.styleable.WidgetTheme_widgetPrimaryColor,
                    res.getColor(R.color.widget_default_primary_color, null)
                ),
                secondaryColor = ta.getColor(
                    R.styleable.WidgetTheme_widgetSecondaryColor,
                    res.getColor(R.color.widget_default_secondary_color, null)
                ),
                accentColor = ta.getColor(
                    R.styleable.WidgetTheme_widgetAccentColor,
                    res.getColor(R.color.widget_default_accent_color, null)
                )
            )

            ta.recycle()
            return colors
        }
    }
}
