// GENERATED FILE - DO NOT EDIT. Regenerate with the app `generateWidgetThemeResources` task.
package com.a3.yearlyprogess.feature.widgets.domain.model

import android.content.Context
import com.a3.yearlyprogess.R

data class WidgetColors(
    val backgroundColor: Int,
    val backgroundLowColor: Int,
    val primaryColor: Int,
    val secondaryColor: Int,
    val accentColor: Int,
    val primary: Int,
    val onPrimary: Int,
    val primaryContainer: Int,
    val onPrimaryContainer: Int,
    val secondary: Int,
    val onSecondary: Int,
    val secondaryContainer: Int,
    val onSecondaryContainer: Int,
    val tertiary: Int,
    val onTertiary: Int,
    val tertiaryContainer: Int,
    val onTertiaryContainer: Int,
    val error: Int,
    val onError: Int,
    val errorContainer: Int,
    val onErrorContainer: Int,
    val background: Int,
    val onBackground: Int,
    val surface: Int,
    val onSurface: Int,
    val surfaceVariant: Int,
    val onSurfaceVariant: Int,
    val outline: Int,
    val outlineVariant: Int,
    val scrim: Int,
    val surfaceTint: Int,
    val inverseSurface: Int,
    val inverseOnSurface: Int,
    val inversePrimary: Int,
    val surfaceDim: Int,
    val surfaceBright: Int,
    val surfaceContainerLowest: Int,
    val surfaceContainerLow: Int,
    val surfaceContainer: Int,
    val surfaceContainerHigh: Int,
    val surfaceContainerHighest: Int,
    val primaryFixed: Int,
    val primaryFixedDim: Int,
    val onPrimaryFixed: Int,
    val onPrimaryFixedVariant: Int,
    val secondaryFixed: Int,
    val secondaryFixedDim: Int,
    val onSecondaryFixed: Int,
    val onSecondaryFixedVariant: Int,
    val tertiaryFixed: Int,
    val tertiaryFixedDim: Int,
    val onTertiaryFixed: Int,
    val onTertiaryFixedVariant: Int,
) {
    companion object {
        fun fromTheme(context: Context, theme: WidgetTheme): WidgetColors {
            val res = context.resources
            val ta = context.obtainStyledAttributes(theme.themeRes, R.styleable.WidgetTheme)
            val colors = WidgetColors(
                backgroundColor = ta.getColor(
                    R.styleable.WidgetTheme_widgetBackgroundColor, res.getColor(R.color.widget_default_background_color, null)
                ),
                backgroundLowColor = ta.getColor(
                    R.styleable.WidgetTheme_widgetBackgroundLowColor, res.getColor(R.color.widget_default_background_low_color, null)
                ),
                primaryColor = ta.getColor(
                    R.styleable.WidgetTheme_widgetPrimaryColor, res.getColor(R.color.widget_default_primary_color, null)
                ),
                secondaryColor = ta.getColor(
                    R.styleable.WidgetTheme_widgetSecondaryColor, res.getColor(R.color.widget_default_secondary_color, null)
                ),
                accentColor = ta.getColor(
                    R.styleable.WidgetTheme_widgetAccentColor, res.getColor(R.color.widget_default_accent_color, null)
                ),
                primary = ta.getColor(
                    R.styleable.WidgetTheme_widgetPrimary, res.getColor(R.color.widget_default_primary, null)
                ),
                onPrimary = ta.getColor(
                    R.styleable.WidgetTheme_widgetOnPrimary, res.getColor(R.color.widget_default_on_primary, null)
                ),
                primaryContainer = ta.getColor(
                    R.styleable.WidgetTheme_widgetPrimaryContainer, res.getColor(R.color.widget_default_primary_container, null)
                ),
                onPrimaryContainer = ta.getColor(
                    R.styleable.WidgetTheme_widgetOnPrimaryContainer, res.getColor(R.color.widget_default_on_primary_container, null)
                ),
                secondary = ta.getColor(
                    R.styleable.WidgetTheme_widgetSecondary, res.getColor(R.color.widget_default_secondary, null)
                ),
                onSecondary = ta.getColor(
                    R.styleable.WidgetTheme_widgetOnSecondary, res.getColor(R.color.widget_default_on_secondary, null)
                ),
                secondaryContainer = ta.getColor(
                    R.styleable.WidgetTheme_widgetSecondaryContainer, res.getColor(R.color.widget_default_secondary_container, null)
                ),
                onSecondaryContainer = ta.getColor(
                    R.styleable.WidgetTheme_widgetOnSecondaryContainer, res.getColor(R.color.widget_default_on_secondary_container, null)
                ),
                tertiary = ta.getColor(
                    R.styleable.WidgetTheme_widgetTertiary, res.getColor(R.color.widget_default_tertiary, null)
                ),
                onTertiary = ta.getColor(
                    R.styleable.WidgetTheme_widgetOnTertiary, res.getColor(R.color.widget_default_on_tertiary, null)
                ),
                tertiaryContainer = ta.getColor(
                    R.styleable.WidgetTheme_widgetTertiaryContainer, res.getColor(R.color.widget_default_tertiary_container, null)
                ),
                onTertiaryContainer = ta.getColor(
                    R.styleable.WidgetTheme_widgetOnTertiaryContainer, res.getColor(R.color.widget_default_on_tertiary_container, null)
                ),
                error = ta.getColor(
                    R.styleable.WidgetTheme_widgetError, res.getColor(R.color.widget_default_error, null)
                ),
                onError = ta.getColor(
                    R.styleable.WidgetTheme_widgetOnError, res.getColor(R.color.widget_default_on_error, null)
                ),
                errorContainer = ta.getColor(
                    R.styleable.WidgetTheme_widgetErrorContainer, res.getColor(R.color.widget_default_error_container, null)
                ),
                onErrorContainer = ta.getColor(
                    R.styleable.WidgetTheme_widgetOnErrorContainer, res.getColor(R.color.widget_default_on_error_container, null)
                ),
                background = ta.getColor(
                    R.styleable.WidgetTheme_widgetBackground, res.getColor(R.color.widget_default_background, null)
                ),
                onBackground = ta.getColor(
                    R.styleable.WidgetTheme_widgetOnBackground, res.getColor(R.color.widget_default_on_background, null)
                ),
                surface = ta.getColor(
                    R.styleable.WidgetTheme_widgetSurface, res.getColor(R.color.widget_default_surface, null)
                ),
                onSurface = ta.getColor(
                    R.styleable.WidgetTheme_widgetOnSurface, res.getColor(R.color.widget_default_on_surface, null)
                ),
                surfaceVariant = ta.getColor(
                    R.styleable.WidgetTheme_widgetSurfaceVariant, res.getColor(R.color.widget_default_surface_variant, null)
                ),
                onSurfaceVariant = ta.getColor(
                    R.styleable.WidgetTheme_widgetOnSurfaceVariant, res.getColor(R.color.widget_default_on_surface_variant, null)
                ),
                outline = ta.getColor(
                    R.styleable.WidgetTheme_widgetOutline, res.getColor(R.color.widget_default_outline, null)
                ),
                outlineVariant = ta.getColor(
                    R.styleable.WidgetTheme_widgetOutlineVariant, res.getColor(R.color.widget_default_outline_variant, null)
                ),
                scrim = ta.getColor(
                    R.styleable.WidgetTheme_widgetScrim, res.getColor(R.color.widget_default_scrim, null)
                ),
                surfaceTint = ta.getColor(
                    R.styleable.WidgetTheme_widgetSurfaceTint, res.getColor(R.color.widget_default_surface_tint, null)
                ),
                inverseSurface = ta.getColor(
                    R.styleable.WidgetTheme_widgetInverseSurface, res.getColor(R.color.widget_default_inverse_surface, null)
                ),
                inverseOnSurface = ta.getColor(
                    R.styleable.WidgetTheme_widgetInverseOnSurface, res.getColor(R.color.widget_default_inverse_on_surface, null)
                ),
                inversePrimary = ta.getColor(
                    R.styleable.WidgetTheme_widgetInversePrimary, res.getColor(R.color.widget_default_inverse_primary, null)
                ),
                surfaceDim = ta.getColor(
                    R.styleable.WidgetTheme_widgetSurfaceDim, res.getColor(R.color.widget_default_surface_dim, null)
                ),
                surfaceBright = ta.getColor(
                    R.styleable.WidgetTheme_widgetSurfaceBright, res.getColor(R.color.widget_default_surface_bright, null)
                ),
                surfaceContainerLowest = ta.getColor(
                    R.styleable.WidgetTheme_widgetSurfaceContainerLowest, res.getColor(R.color.widget_default_surface_container_lowest, null)
                ),
                surfaceContainerLow = ta.getColor(
                    R.styleable.WidgetTheme_widgetSurfaceContainerLow, res.getColor(R.color.widget_default_surface_container_low, null)
                ),
                surfaceContainer = ta.getColor(
                    R.styleable.WidgetTheme_widgetSurfaceContainer, res.getColor(R.color.widget_default_surface_container, null)
                ),
                surfaceContainerHigh = ta.getColor(
                    R.styleable.WidgetTheme_widgetSurfaceContainerHigh, res.getColor(R.color.widget_default_surface_container_high, null)
                ),
                surfaceContainerHighest = ta.getColor(
                    R.styleable.WidgetTheme_widgetSurfaceContainerHighest, res.getColor(R.color.widget_default_surface_container_highest, null)
                ),
                primaryFixed = ta.getColor(
                    R.styleable.WidgetTheme_widgetPrimaryFixed, res.getColor(R.color.widget_default_primary_fixed, null)
                ),
                primaryFixedDim = ta.getColor(
                    R.styleable.WidgetTheme_widgetPrimaryFixedDim, res.getColor(R.color.widget_default_primary_fixed_dim, null)
                ),
                onPrimaryFixed = ta.getColor(
                    R.styleable.WidgetTheme_widgetOnPrimaryFixed, res.getColor(R.color.widget_default_on_primary_fixed, null)
                ),
                onPrimaryFixedVariant = ta.getColor(
                    R.styleable.WidgetTheme_widgetOnPrimaryFixedVariant, res.getColor(R.color.widget_default_on_primary_fixed_variant, null)
                ),
                secondaryFixed = ta.getColor(
                    R.styleable.WidgetTheme_widgetSecondaryFixed, res.getColor(R.color.widget_default_secondary_fixed, null)
                ),
                secondaryFixedDim = ta.getColor(
                    R.styleable.WidgetTheme_widgetSecondaryFixedDim, res.getColor(R.color.widget_default_secondary_fixed_dim, null)
                ),
                onSecondaryFixed = ta.getColor(
                    R.styleable.WidgetTheme_widgetOnSecondaryFixed, res.getColor(R.color.widget_default_on_secondary_fixed, null)
                ),
                onSecondaryFixedVariant = ta.getColor(
                    R.styleable.WidgetTheme_widgetOnSecondaryFixedVariant, res.getColor(R.color.widget_default_on_secondary_fixed_variant, null)
                ),
                tertiaryFixed = ta.getColor(
                    R.styleable.WidgetTheme_widgetTertiaryFixed, res.getColor(R.color.widget_default_tertiary_fixed, null)
                ),
                tertiaryFixedDim = ta.getColor(
                    R.styleable.WidgetTheme_widgetTertiaryFixedDim, res.getColor(R.color.widget_default_tertiary_fixed_dim, null)
                ),
                onTertiaryFixed = ta.getColor(
                    R.styleable.WidgetTheme_widgetOnTertiaryFixed, res.getColor(R.color.widget_default_on_tertiary_fixed, null)
                ),
                onTertiaryFixedVariant = ta.getColor(
                    R.styleable.WidgetTheme_widgetOnTertiaryFixedVariant, res.getColor(R.color.widget_default_on_tertiary_fixed_variant, null)
                ),
            )
            ta.recycle()
            return colors
        }
    }
}
