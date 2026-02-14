package com.a3.yearlyprogess.core.ui.components

import android.os.Build
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.a3.yearlyprogess.R
import com.a3.yearlyprogess.feature.widgets.domain.model.WidgetColors
import com.a3.yearlyprogess.feature.widgets.domain.model.WidgetTheme
import com.a3.yearlyprogess.feature.widgets.ui.ThemeManager

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ThemeSelector(
    selectedTheme: WidgetTheme,
    onThemeSelected: (WidgetTheme) -> Unit,
    modifier: Modifier = Modifier,
    initialThemesToShow: Int = 5
) {
    var showDialog by remember { mutableStateOf(false) }
    val themes =  remember { ThemeManager.getAvailableThemes() }
    val initialThemes = remember(selectedTheme, initialThemesToShow) {
        val isAndroid12AndUp = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
        buildList {
            addAll(themes.take(initialThemesToShow))
            if (isAndroid12AndUp) {
                if (WidgetTheme.DYNAMIC !in this) add(WidgetTheme.DYNAMIC)
            }
            if (selectedTheme !in this) {
                add(selectedTheme)
            }
        }.distinct()
    }
    val lastIndex = initialThemes.lastIndex

    Column(modifier = modifier) {
        Text(
            text = stringResource(R.string.settings_theme),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))

        Column(
            modifier = Modifier.selectableGroup(),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            initialThemes.forEachIndexed { index, theme ->
                ThemeOption(
                    theme = theme,
                    selected = theme == selectedTheme,
                    onClick = { onThemeSelected(theme) },
                    shape = themeItemShape(index, lastIndex)
                )
            }
        }

        // More button
        if (themes.size > initialThemesToShow) {
            Button(
                onClick = { showDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                shapes = ButtonDefaults.shapes(
                    pressedShape = RoundedCornerShape(24.dp),
                    shape = RoundedCornerShape(12.dp)
                )
            ) {
                Text(
                    text = "More themes",
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    }

    // Theme selection dialog
    if (showDialog) {
        ThemeSelectionDialog(
            themes = themes,
            selectedTheme = selectedTheme,
            onThemeSelected = { theme ->
                onThemeSelected(theme)
                showDialog = false
            },
            onDismiss = { showDialog = false }
        )
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
fun ThemeSelectionDialog(
    themes: List<WidgetTheme>,
    selectedTheme: WidgetTheme,
    onThemeSelected: (WidgetTheme) -> Unit,
    onDismiss: () -> Unit
) {
    val lastIndex = themes.lastIndex
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Select Theme",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
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
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        },
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier
            .fillMaxWidth(0.92f)
            .padding(16.dp)
    )
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
            ),
        shape = shape,
        color = if (selected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainerLow
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
                text = theme.displayName,
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
            .size(20.dp)
    ) {
        drawCircle(Color(color))
    }
}