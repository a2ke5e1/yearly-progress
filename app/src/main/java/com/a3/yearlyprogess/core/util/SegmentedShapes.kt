package com.a3.yearlyprogess.core.util

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Shape

@Composable
fun segmentedShapes(
    index: Int,
    count: Int,
): Shape {
    val defaultShapes = MaterialTheme.shapes.extraSmall
    val overrideShape = MaterialTheme.shapes.largeIncreased
    return remember(index, count, defaultShapes, overrideShape) {
        when {
            count == 1 -> defaultShapes
            index == 0 -> {
                defaultShapes.copy(
                    topStart = overrideShape.topStart,
                    topEnd = overrideShape.topEnd
                )
            }

            index == count - 1 -> {
                defaultShapes.copy(
                    bottomStart = overrideShape.bottomStart,
                    bottomEnd = overrideShape.bottomEnd,
                )
            }

            else -> defaultShapes
        }
    }
}