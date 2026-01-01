package com.a3.yearlyprogess.feature.widgets.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.a3.yearlyprogess.R
import com.a3.yearlyprogess.feature.widgets.domain.model.StandaloneWidgetOptions

@Composable
fun WidgetShapeSelector(
    selectedShape: StandaloneWidgetOptions.Companion.WidgetShape,
    onShapeSelected: (StandaloneWidgetOptions.Companion.WidgetShape) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = stringResource(R.string.widget_shape),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            StandaloneWidgetOptions.Companion.WidgetShape.entries.forEach { shape ->
                ShapeOption(
                    shape = shape,
                    isSelected = shape == selectedShape,
                    onSelected = { onShapeSelected(shape) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun ShapeOption(
    shape: StandaloneWidgetOptions.Companion.WidgetShape,
    isSelected: Boolean,
    onSelected: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    val contentColor = if (isSelected) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    val borderColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        Color.Transparent
    }

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .border(
                width = 2.dp,
                color = borderColor,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onSelected)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Visual representation of the shape
        ShapePreview(
            shape = shape,
            color = contentColor,
            modifier = Modifier.size(64.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ShapePreview(
    shape: StandaloneWidgetOptions.Companion.WidgetShape,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        when (shape) {
            StandaloneWidgetOptions.Companion.WidgetShape.RECTANGULAR -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize(0.8f)
                        .background(
                            color = color.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .border(
                            width = 2.dp,
                            color = color,
                            shape = RoundedCornerShape(8.dp)
                        )
                )
            }

            StandaloneWidgetOptions.Companion.WidgetShape.CLOVER -> {
                // Clover shape using four circles
                Box(
                    modifier = Modifier
                        .fillMaxSize(0.8f)
                        .background(
                            color = color.copy(alpha = 0.3f),
                            shape = MaterialShapes.Clover8Leaf.toShape()
                        )
                        .border(
                            width = 2.dp,
                            color = color,
                            shape = MaterialShapes.Clover8Leaf.toShape()
                        )
                )
            }

            StandaloneWidgetOptions.Companion.WidgetShape.PILL -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize(0.8f)
                        .background(
                            color = color.copy(alpha = 0.3f),
                            shape = MaterialShapes.Pill.toShape()
                        )
                        .border(
                            width = 2.dp,
                            color = color,
                            shape = MaterialShapes.Pill.toShape()
                        )
                )
            }
        }
    }
}