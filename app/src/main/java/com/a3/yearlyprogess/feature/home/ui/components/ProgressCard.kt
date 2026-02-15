package com.a3.yearlyprogess.feature.home.ui.components

import android.icu.text.NumberFormat
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.a3.yearlyprogess.R
import com.a3.yearlyprogess.core.ui.interaction.PressAnimationConfig
import com.a3.yearlyprogess.core.ui.interaction.applyPressGesture
import com.a3.yearlyprogess.core.ui.interaction.rememberPressInteractionState
import com.a3.yearlyprogess.core.ui.style.CardCornerStyle
import com.a3.yearlyprogess.core.util.ProgressSettings
import com.a3.yearlyprogess.core.util.TimePeriod
import com.a3.yearlyprogess.core.util.YearlyProgressUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

data class ProgressCardStyle(
    val cardHeight: Dp,
    val cardPadding: Dp,
    val progressBarColor: Color,
    val backgroundColor: Color,
    val labelTextStyle: TextStyle,
    val titleTextStyle: TextStyle,
    val progressTextStyle: TextStyle,
    val durationTextStyle: TextStyle,
    val cornerStyle: CardCornerStyle,
    val pressConfig: PressAnimationConfig
)

object ProgressCardDefaults {

    @Composable
    fun progressCardStyle(
        cardHeight: Dp = 160.dp,
        cardPadding: Dp = 18.dp,
        progressBarColor: Color = MaterialTheme.colorScheme.primaryContainer,
        backgroundColor: Color = MaterialTheme.colorScheme.surfaceContainer,
        labelTextStyle: TextStyle = MaterialTheme.typography.labelSmall.copy(
            color = MaterialTheme.colorScheme.onSurface
        ),
        titleTextStyle: TextStyle = MaterialTheme.typography.titleMedium.copy(
            color = MaterialTheme.colorScheme.onSurface
        ),
        progressTextStyle: TextStyle = MaterialTheme.typography.displaySmall.copy(
            color = MaterialTheme.colorScheme.onPrimaryContainer
        ),
        durationTextStyle: TextStyle = MaterialTheme.typography.bodySmall.copy(
            color = MaterialTheme.colorScheme.onSurface
        ),
        cornerStyle: CardCornerStyle = CardCornerStyle.Default,
        pressConfig: PressAnimationConfig = PressAnimationConfig()
    ): ProgressCardStyle = ProgressCardStyle(
        cardHeight = cardHeight,
        cardPadding = cardPadding,
        progressBarColor = progressBarColor,
        backgroundColor = backgroundColor,
        labelTextStyle = labelTextStyle,
        titleTextStyle = titleTextStyle,
        progressTextStyle = progressTextStyle,
        durationTextStyle = durationTextStyle,
        cornerStyle = cornerStyle,
        pressConfig = pressConfig
    )
}

@Composable
fun ProgressCard(
    modifier: Modifier = Modifier,
    timePeriod: TimePeriod,
    settings: ProgressSettings = ProgressSettings(),
    refreshInterval: Long = 16L,
    style: ProgressCardStyle = ProgressCardDefaults.progressCardStyle(),
) {
    val decimals = settings.decimalDigits.coerceIn(0, 13)
    val progressUtil = remember(settings) { YearlyProgressUtil(settings) }
    val startTime by remember(progressUtil, timePeriod) {
        derivedStateOf { progressUtil.calculateStartTime(timePeriod) }
    }
    val endTime by remember(progressUtil, timePeriod) {
        derivedStateOf { progressUtil.calculateEndTime(timePeriod) }
    }
    val duration = (endTime - startTime) / 1000
    val progressState = produceState(
        initialValue = progressUtil.calculateProgress(startTime, endTime),
        startTime, endTime, progressUtil
    ) {
        while (true) {
            val newProgress = withContext(Dispatchers.Default) {
                progressUtil.calculateProgress(startTime, endTime)
            }
            value = newProgress
            delay(refreshInterval)
        }
    }

    val durationFormatted = remember(duration) {
        NumberFormat.getNumberInstance(settings.uLocale).format(duration)
    }

    val pressState = rememberPressInteractionState(style.pressConfig)
    val animatedCorners = pressState.animateCorners(default = style.cornerStyle)
    val animatedShape = style.cornerStyle.toAnimatedShape(animatedCorners)

    Box(
        modifier = modifier
            .height(style.cardHeight)
            .fillMaxWidth()
            .graphicsLayer {
                shape = animatedShape
                clip = true
            }
            .background(style.backgroundColor)
            .applyPressGesture(pressState)
            .drawWithContent {
                val currentProgress = progressState.value
                val progressFraction = (currentProgress / 100).toFloat().coerceIn(0f, 1f)
                drawRect(
                    color = style.progressBarColor,
                    size = size.copy(width = size.width * progressFraction)
                )
                drawContent()
            }
    ) {
        Column(
            modifier = Modifier.padding(style.cardPadding),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = timePeriod.name.lowercase().replaceFirstChar { it.uppercase() },
                style = style.labelTextStyle
            )

            Text(
                text = when (timePeriod) {
                    TimePeriod.MONTH -> progressUtil.getMonthName(progressUtil.getCurrentPeriodValue(timePeriod))
                    TimePeriod.WEEK -> progressUtil.getWeekDayName(progressUtil.getCurrentPeriodValue(timePeriod))
                    TimePeriod.DAY -> progressUtil.getCurrentPeriodValue(timePeriod)
                        .toString() + progressUtil.getOrdinalSuffix(progressUtil.getCurrentPeriodValue(timePeriod))
                    else -> progressUtil.getCurrentPeriodValue(timePeriod).toString()
                },
                style = style.titleTextStyle
            )

            Spacer(modifier = Modifier.height(16.dp))

            FormattedPercentage(
                progressProvider = { progressState.value },
                digits = decimals,
                style = style.progressTextStyle
            )

            Text(
                text = stringResource(R.string.progress_card_total_duration, durationFormatted),
                style = style.durationTextStyle
            )
        }
    }
}