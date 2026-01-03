package com.a3.yearlyprogess.feature.widgets.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.a3.yearlyprogess.R
import com.a3.yearlyprogess.core.ui.components.Slider
import com.a3.yearlyprogess.core.ui.components.Switch
import com.a3.yearlyprogess.core.ui.components.ThemeSelector
import com.a3.yearlyprogess.feature.widgets.domain.model.WidgetTheme
import kotlin.math.roundToInt

/**
 * Shared settings component used across all widget configuration screens.
 *
 * This component provides a consistent UI for common widget settings:
 * - Theme selection
 * - Time status counter toggles
 * - Decimal precision
 * - Background transparency
 * - Font scaling
 *
 * @param theme Current theme selection
 * @param timeStatusCounter Whether time status counter is enabled
 * @param dynamicTimeStatusCounter Whether dynamic time status counter is enabled
 * @param replaceProgressWithTimeLeft Whether to replace progress with time left
 * @param decimalDigits Number of decimal digits to display
 * @param backgroundTransparency Background transparency value (0-100)
 * @param fontScale Font scale value (0.5-2.0)
 * @param onThemeChange Callback when theme changes
 * @param onTimeStatusCounterChange Callback when time status counter changes
 * @param onDynamicTimeStatusCounterChange Callback when dynamic time status counter changes
 * @param onReplaceProgressChange Callback when replace progress setting changes
 * @param onDecimalDigitsChange Callback when decimal digits changes
 * @param onBackgroundTransparencyChange Callback when background transparency changes
 * @param onFontScaleChange Callback when font scale changes
 * @param modifier Modifier for the root Column
 * @param showTimeStatusCounter Whether to show time status related toggles (default: true)
 * @param timeStatusCounterTitle Title for the time status counter switch (default: "Time Status Counter")
 * @param timeStatusCounterDescription Description for the time status counter switch
 * @param replaceProgressTitle Title for the replace progress switch (default: "Replace Progress with Time Left")
 * @param replaceProgressDescription Description for the replace progress switch
 * @param decimalDigitsTitle Title for decimal digits slider (default: "Decimal Digits")
 */
@Composable
fun SharedWidgetSettings(
    theme: WidgetTheme,
    timeStatusCounter: Boolean,
    dynamicTimeStatusCounter: Boolean,
    replaceProgressWithTimeLeft: Boolean,
    decimalDigits: Int,
    backgroundTransparency: Int,
    fontScale: Float,
    onThemeChange: (WidgetTheme) -> Unit,
    onTimeStatusCounterChange: (Boolean) -> Unit,
    onDynamicTimeStatusCounterChange: (Boolean) -> Unit,
    onReplaceProgressChange: (Boolean) -> Unit,
    onDecimalDigitsChange: (Int) -> Unit,
    onBackgroundTransparencyChange: (Int) -> Unit,
    onFontScaleChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    showTimeStatusCounter: Boolean = true,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // Theme Selector
        ThemeSelector(
            selectedTheme = theme,
            onThemeSelected = onThemeChange,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(Modifier.height(8.dp))

        if (showTimeStatusCounter) {
            // Time Status Counter
            Switch(
                title = stringResource(R.string.settings_time_status_title),
                description = stringResource(R.string.settings_time_status_description),
                checked = timeStatusCounter,
                onCheckedChange = onTimeStatusCounterChange
            )

            // Dynamic Time Status Counter
            Switch(
                title = stringResource(R.string.settings_dynamic_time_title),
                description = stringResource(R.string.settings_dynamic_time_description),
                checked = dynamicTimeStatusCounter,
                onCheckedChange = onDynamicTimeStatusCounterChange,
                disabled = !timeStatusCounter
            )

            // Replace Progress with Time Left
            Switch(
                title = stringResource(R.string.settings_replace_progress_title),
                description = stringResource(R.string.settings_replace_progress_description),
                checked = replaceProgressWithTimeLeft,
                onCheckedChange = onReplaceProgressChange,
                disabled = !timeStatusCounter
            )
        }

        // Decimal Digits Slider
        Slider(
            title = stringResource(R.string.settings_decimal_digits),
            value = decimalDigits.toFloat(),
            valueRange = 0f..5f,
            steps = 4,
            onValueChange = { value ->
                onDecimalDigitsChange(value.roundToInt())
            },
            modifier = Modifier.padding(top = 8.dp)
        )

        // Background Transparency Slider
        Slider(
            title = stringResource(R.string.settings_background_transparency),
            value = backgroundTransparency.toFloat(),
            valueRange = 0f..100f,
            onValueChange = { value ->
                onBackgroundTransparencyChange(value.roundToInt())
            }
        )

        // Font Scale Slider
        Slider(
            title = stringResource(R.string.settings_font_scale),
            value = fontScale,
            valueRange = 0.5f..2.0f,
            onValueChange = onFontScaleChange
        )
    }
}