package com.a3.yearlyprogess.feature.widgets.ui

import android.content.Context
import android.os.Build
import com.a3.yearlyprogess.feature.widgets.domain.model.WidgetColors
import com.a3.yearlyprogess.feature.widgets.domain.model.WidgetTheme
import com.a3.yearlyprogess.feature.widgets.domain.repository.WidgetThemeRepository
import kotlinx.coroutines.flow.first

object ThemeManager {

    fun getWidgetColors(
        context: Context,
        theme: WidgetTheme
    ): WidgetColors {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && theme === WidgetTheme.DYNAMIC) {
            WidgetColors.fromTheme(context, WidgetTheme.DYNAMIC)
        } else {
            WidgetColors.fromTheme(context, theme)
        }
    }

    fun getAvailableThemes(): List<WidgetTheme> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            WidgetTheme.entries
        } else {
            WidgetTheme.entries.filter { it != WidgetTheme.DYNAMIC }
        }
    }
}
