package com.a3.yearlyprogess.core.ui.components

import android.view.SoundEffectConstants
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp

@Composable
fun SwitchWithOptions(
    title: String,
    summary: String,
    checked: Boolean,
    disabled: Boolean = false,
    onCheckedChange: (Boolean) -> Unit,
    onOptionClicked: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val haptic = LocalHapticFeedback.current
    val view = LocalView.current

    val triggerFeedback = {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        view.playSoundEffect(SoundEffectConstants.CLICK)
    }

    Row(
        modifier =
            Modifier.fillMaxWidth()
                .alpha(if (!disabled) 1f else 0.5f)
                .animateContentSize()
                .clickable(enabled = !disabled) { onOptionClicked() },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                modifier =
                    Modifier.padding(
                        start = 16.dp,
                        top = 16.dp,
                    ))
            AnimatedVisibility(visible = true) {
                Text(
                    summary,
                    style =
                        MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant),
                    modifier = Modifier.padding(start = 16.dp, bottom = 16.dp))
            }
        }

        VerticalDivider(modifier = Modifier.height(40.dp).padding(end = 16.dp))

        Switch(
            checked = checked,
            onCheckedChange = {
                triggerFeedback()
                onCheckedChange(it)
            },
            interactionSource = interactionSource,
            modifier = Modifier.padding(end = 16.dp),
            enabled = !disabled
        )
    }
}