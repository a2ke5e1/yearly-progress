package com.a3.yearlyprogess.core.ui.components

import androidx.annotation.IntRange
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp


@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun Slider(
    modifier: Modifier = Modifier,
    title: String,
    description: String? = null,
    disabled: Boolean = false,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    @IntRange(from = 0) steps: Int = 0,
    onValueChange: (Float) -> Unit,
) {

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .alpha(if (!disabled) 1f else 0.5f)
            .animateContentSize(),
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium)

        AnimatedVisibility(visible = description != null) {
            Text(
                description ?: "", style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }

        Spacer(Modifier.height(2.dp))

        Slider(
            enabled = !disabled,
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            steps = steps,
            track = { sliderState ->
                SliderDefaults.Track(
                    sliderState = sliderState,
                    modifier = Modifier.height(32.dp),
                        trackCornerSize = 8.dp
                )
            }
        )

    }
}
