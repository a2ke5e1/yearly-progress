package com.a3.yearlyprogess.core.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.a3.yearlyprogess.feature.widgets.domain.model.WidgetColors
import com.a3.yearlyprogess.feature.widgets.domain.model.WidgetTheme
import com.a3.yearlyprogess.feature.widgets.ui.ThemeManager

@Composable
fun ThemeSelector(
    selectedTheme: WidgetTheme,
    onThemeSelected: (WidgetTheme) -> Unit,
    modifier: Modifier = Modifier
) {
    val themes =  remember { ThemeManager.getAvailableThemes() }
    val lastIndex = themes.lastIndex

    Column(modifier = modifier) {
        Text(
            text = "Theme",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))

        Column(
            modifier = Modifier.selectableGroup(),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            themes.forEachIndexed { index, theme ->
                ThemeOption(
                    theme = theme,
                    selected = theme == selectedTheme,
                    onClick = { onThemeSelected(theme) },
                    shape = themeItemShape(index, lastIndex)
                )
            }
        }
    }
}

@Composable
private fun themeItemShape(index: Int, lastIndex: Int): RoundedCornerShape {
     val Small = 12.dp
     val ExtraSmall = 4.dp
    return when (index) {
        0 -> RoundedCornerShape(
            topStart = Small,
            topEnd = Small,
            bottomStart = ExtraSmall,
            bottomEnd = ExtraSmall
        )
        lastIndex -> RoundedCornerShape(
            topStart = ExtraSmall,
            topEnd = ExtraSmall,
            bottomStart = Small,
            bottomEnd = Small
        )
        else -> RoundedCornerShape(
            topStart = ExtraSmall,
            topEnd = ExtraSmall,
            bottomStart = ExtraSmall,
            bottomEnd = ExtraSmall
        )
    }
}
@Composable
fun ThemeOption(
    theme: WidgetTheme,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.small
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.RadioButton,
            )
        ,
        shape = shape,
        color = if (selected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        },
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = selected,
                onClick = null
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = theme.name.replace('_', ' ').lowercase()
                    .replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.bodyLarge,
                color = if (selected) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
            Spacer(modifier = Modifier.weight(1f))
            ThemeColorPreview(ThemeManager.getWidgetColors(LocalContext.current, theme))
        }
    }
}

@Composable
fun ThemeColorPreview(colors: WidgetColors, modifier: Modifier = Modifier) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        PreviewDot(color = colors.backgroundColor)
        PreviewDot(color = colors.primaryColor)
        PreviewDot(color = colors.secondaryColor)
        PreviewDot(color = colors.accentColor)
    }
}

@Composable
private fun PreviewDot(color: Int) {
    Canvas(
        modifier = Modifier
            .width(20.dp)
            .height(20.dp)
    ) {
        drawCircle(Color(color))
    }
}
