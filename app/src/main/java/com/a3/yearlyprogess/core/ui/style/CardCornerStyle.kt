package com.a3.yearlyprogess.core.ui.style

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.a3.yearlyprogess.core.ui.interaction.AnimatedCorners

data class CardCornerStyle(
    val topStart: Dp,
    val topEnd: Dp,
    val bottomStart: Dp,
    val bottomEnd: Dp
) {
    fun toShape(): Shape = RoundedCornerShape(
        topStart = topStart,
        topEnd = topEnd,
        bottomStart = bottomStart,
        bottomEnd = bottomEnd
    )

    // Create shape with animated corners
    fun toAnimatedShape(animatedCorners: AnimatedCorners): Shape =
        animatedCorners.toShape()


    companion object {
        val Default = CardCornerStyle(
            topStart = 16.dp,
            topEnd = 16.dp,
            bottomStart = 16.dp,
            bottomEnd = 16.dp
        )

        val FirstInList = CardCornerStyle(
            topStart = 16.dp,
            topEnd = 16.dp,
            bottomStart = 8.dp,
            bottomEnd = 8.dp
        )

        val MiddleInList = CardCornerStyle(
            topStart = 8.dp,
            topEnd = 8.dp,
            bottomStart = 8.dp,
            bottomEnd = 8.dp
        )

        val LastInList = CardCornerStyle(
            topStart = 8.dp,
            topEnd = 8.dp,
            bottomStart = 16.dp,
            bottomEnd = 16.dp
        )

        val SingleItem = CardCornerStyle(
            topStart = 16.dp,
            topEnd = 16.dp,
            bottomStart = 16.dp,
            bottomEnd = 16.dp
        )

        fun forPosition(
            isFirst: Boolean,
            isLast: Boolean,
            isSingle: Boolean = false
        ): CardCornerStyle = when {
            isSingle -> SingleItem
            isFirst -> FirstInList
            isLast -> LastInList
            else -> MiddleInList
        }
    }
}