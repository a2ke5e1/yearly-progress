package com.a3.yearlyprogess.core.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp

@Composable
fun ExpandableSection(
    title: String,
    modifier: Modifier = Modifier,
    collapsible: Boolean = true,
    initiallyExpanded: Boolean = true,
    content: @Composable () -> Unit
) {
    var expanded by remember { mutableStateOf(initiallyExpanded) }

    val rotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        label = ""
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize()
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (collapsible) Modifier.clickable { expanded = !expanded }
                    else Modifier    // when not collapsible, clicking does nothing
                )
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            // Show arrow only if collapsible
            if (collapsible) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    modifier = Modifier.rotate(rotation),
                    contentDescription = null
                )
            }
        }

        if (expanded || !collapsible) {
            content()
        }
    }
}
