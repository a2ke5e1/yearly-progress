package com.a3.yearlyprogess.core.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.a3.yearlyprogess.feature.widgets.domain.model.WidgetTheme
import com.a3.yearlyprogess.feature.widgets.ui.ThemeManager
import com.materialkolor.DynamicMaterialTheme
import com.materialkolor.PaletteStyle

@Immutable
data class ColorFamily(
    val color: Color, val onColor: Color, val colorContainer: Color, val onColorContainer: Color
)

val unspecified_scheme =
    ColorFamily(Color.Unspecified, Color.Unspecified, Color.Unspecified, Color.Unspecified)

@Composable
fun YearlyProgressTheme(
    appTheme: WidgetTheme = WidgetTheme.DEFAULT,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    
    if (appTheme == WidgetTheme.DYNAMIC && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val colorScheme = if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    } else {
        val widgetColors = ThemeManager.getWidgetColors(context, appTheme)
        val seedColor = Color(widgetColors.primaryColor)
        
        DynamicMaterialTheme(
            seedColor = seedColor,
            useDarkTheme = darkTheme,
            style = PaletteStyle.TonalSpot,
            typography = Typography,
            content = content
        )
    }
}
