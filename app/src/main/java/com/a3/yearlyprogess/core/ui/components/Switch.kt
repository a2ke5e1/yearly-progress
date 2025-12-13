package com.a3.yearlyprogess.core.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp

@Composable
fun Switch(
    modifier: Modifier = Modifier,
    title: String,
    description: String? = null,
    checked: Boolean,
    disabled: Boolean = false,
    onCheckedChange: (Boolean) -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }

    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clickable(
                    enabled = !disabled, interactionSource = interactionSource, indication = null) {
                    onCheckedChange(!checked)
                }
                .animateContentSize(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Column(
            modifier = Modifier.weight(1f).fillMaxHeight()
                .alpha(if (!disabled) 1f else 0.5f)
            ,
            verticalArrangement = Arrangement.Center) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            if (description != null) {
                AnimatedVisibility(visible = true) {
                    Text(
                        description,
                        style =
                            MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant))
                }
            }
        }

        Switch(
            checked = checked,
            onCheckedChange = { onCheckedChange(it) },
            interactionSource = interactionSource,
            enabled = !disabled
        )
    }
}