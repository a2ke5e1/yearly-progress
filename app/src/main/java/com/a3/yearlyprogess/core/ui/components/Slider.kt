package com.a3.yearlyprogess.core.ui.components

import androidx.annotation.IntRange
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

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
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isDragging by interactionSource.collectIsDraggedAsState()
    val isPressed by interactionSource.collectIsPressedAsState()
    val showLabel = isDragging || isPressed
    val lastStep = remember { mutableStateOf<Int?>(null) }


    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .alpha(if (!disabled) 1f else 0.5f)
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium)

        if (description != null) {
            Text(
                description, style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }

        Spacer(Modifier.height(2.dp))
            Slider(
                enabled = !disabled,
                value = value,
                onValueChange = { newValue ->
                    if (steps > 0) {
                        val stepSize =
                            (valueRange.endInclusive - valueRange.start) / (steps + 1)

                        val currentStep =
                            ((newValue - valueRange.start) / stepSize).roundToInt()

                        if (currentStep != lastStep.value) {
                            lastStep.value = currentStep
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        }
                    }
                    onValueChange(newValue)
                },
                onValueChangeFinished = {
                    if (steps == 0) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    }
                },
                valueRange = valueRange,
                steps = steps,
                interactionSource = interactionSource,
                thumb = { sliderState ->
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        if (showLabel) {
                            TooltipLabel(
                                text = formatValue(value, valueRange, steps),
                                visible = true,
                                modifier = Modifier
                                    .offset(y = (-26).dp)
                                    .layout { measurable, constraints ->
                                        val placeable = measurable.measure(constraints)
                                        layout(0, 0) {
                                            placeable.placeRelative(
                                                x = -placeable.width / 2,
                                                y = -placeable.height
                                            )
                                        }
                                    }
                            )
                        }
                        SliderDefaults.Thumb(
                            interactionSource = interactionSource,
                            enabled = !disabled
                        )
                    }
                },
                track = { sliderState ->
                    SliderDefaults.Track(
                        sliderState = sliderState,
                        modifier = Modifier.height(40.dp),
                        trackCornerSize = 12.dp
                    )
                }
            )
        }

}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun TooltipLabel(
    text: String, visible: Boolean, modifier: Modifier = Modifier
) {
    // Animate alpha and scale
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = if (visible) 83 else 117),
        label = "label_alpha"
    )

    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.8f,
        animationSpec = tween(durationMillis = if (visible) 83 else 117),
        label = "label_scale"
    )

    Surface(
        shape = MaterialTheme.shapes.extraLargeIncreased,
        color = MaterialTheme.colorScheme.inverseSurface,
        shadowElevation = 0.dp,
        tonalElevation = 0.dp,
        modifier = modifier
            .widthIn(min = 48.dp)
            .heightIn(min = 44.dp)
            .alpha(alpha)
            .scale(scale)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.inverseOnSurface,
            modifier = Modifier
                .wrapContentSize(Alignment.Center)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        )
    }
}

private fun formatValue(
    value: Float, valueRange: ClosedFloatingPointRange<Float>, steps: Int
): String {
    return if (steps > 0) {
        value.roundToInt().toString()
    } else {
        "%.1f".format(value)
    }
}