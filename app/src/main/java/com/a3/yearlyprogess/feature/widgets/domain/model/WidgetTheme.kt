package com.a3.yearlyprogess.feature.widgets.domain.model
import androidx.annotation.StyleRes
import com.a3.yearlyprogess.R

enum class WidgetTheme(
    @param:StyleRes val themeRes: Int,
    val displayName: String
) {
    DEFAULT(R.style.WidgetTheme_Default, "Default"),
    GREEN(R.style.WidgetTheme_Green, "Green"),
    DYNAMIC(R.style.WidgetTheme_Dynamic, "Dynamic Colors"),
}
