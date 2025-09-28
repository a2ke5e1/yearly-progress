package com.a3.yearlyprogess.feature.home.ui.components


import android.icu.text.NumberFormat
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.a3.yearlyprogess.core.util.ProgressSettings
import com.a3.yearlyprogess.core.util.TimePeriod
import com.a3.yearlyprogess.core.util.YearlyProgressUtil
import kotlinx.coroutines.delay

// Data class for ProgressCard style
data class ProgressCardStyle(
    val cardHeight: Dp,
    val cardPadding: Dp,
    val cornerRadiusDefault: Dp,
    val cornerRadiusPressed: Dp,
    val progressBarColor: androidx.compose.ui.graphics.Color,
    val backgroundColor: androidx.compose.ui.graphics.Color,
    val labelTextStyle: androidx.compose.ui.text.TextStyle,
    val titleTextStyle: androidx.compose.ui.text.TextStyle,
    val progressTextStyle: androidx.compose.ui.text.TextStyle,
    val durationTextStyle: androidx.compose.ui.text.TextStyle,
    val cornerAnimationSpec: TweenSpec<Dp>
)

// Defaults factory, @Composable to access MaterialTheme safely
object ProgressCardDefaults {
    @Composable
    fun progressCardStyle(): ProgressCardStyle = ProgressCardStyle(
        cardHeight = 150.dp,
        cardPadding = 18.dp,
        cornerRadiusDefault = 16.dp,
        cornerRadiusPressed = 32.dp,
        progressBarColor = MaterialTheme.colorScheme.primaryContainer,
        backgroundColor = MaterialTheme.colorScheme.surfaceContainer,
        labelTextStyle = MaterialTheme.typography.labelSmall.copy(
            color = MaterialTheme.colorScheme.onSurface
        ),
        titleTextStyle = MaterialTheme.typography.titleMedium.copy(
            color = MaterialTheme.colorScheme.onSurface
        ),
        progressTextStyle = MaterialTheme.typography.displaySmall.copy(
            color = MaterialTheme.colorScheme.onPrimaryContainer
        ),
        durationTextStyle = MaterialTheme.typography.bodySmall.copy(
            color = MaterialTheme.colorScheme.onSurface
        ),
        cornerAnimationSpec = TweenSpec(
            durationMillis = 300,
            easing = FastOutSlowInEasing
        )
    )
}

@Composable
fun ProgressCard(
    modifier: Modifier = Modifier,
    timePeriod: TimePeriod,
    settings: ProgressSettings = ProgressSettings(),
    decimals: Int = 13,
    refreshInterval: Long = 1L,
    style: ProgressCardStyle = ProgressCardDefaults.progressCardStyle()
) {
    val progressUtil = remember { YearlyProgressUtil(settings) }
    val startTime = progressUtil.calculateStartTime(timePeriod)
    val endTime = progressUtil.calculateEndTime(timePeriod)
    val duration = (endTime - startTime) / 1000

    var progress by remember { mutableStateOf(progressUtil.calculateProgress(startTime, endTime)) }

    LaunchedEffect(Unit) {
        while (true) {
            progress = progressUtil.calculateProgress(startTime, endTime)
            delay(refreshInterval)
        }
    }

    val durationFormatted = remember(duration) {
        NumberFormat.getNumberInstance(settings.uLocale).format(duration)
    }

    // Press state and animated corner radius
    var pressed by remember { mutableStateOf(false) }
    val cornerRadius: Dp by animateDpAsState(
        targetValue = if (pressed) style.cornerRadiusPressed else style.cornerRadiusDefault,
        animationSpec = style.cornerAnimationSpec
    )

    Box(
        modifier = modifier
            .height(style.cardHeight)
            .fillMaxWidth()
            .clip(RoundedCornerShape(cornerRadius))
            .background(style.backgroundColor)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        pressed = true
                        tryAwaitRelease()
                        pressed = false
                    }
                )
            }
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth((progress / 100).toFloat().coerceIn(0f, 1f))
                .background(style.progressBarColor, shape = RoundedCornerShape(cornerRadius))
                .align(Alignment.CenterStart)
        )

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

            Text(
                text = "%.${decimals}f%%".format(progress),
                style = style.progressTextStyle
            )

            Text(
                text = "of ${durationFormatted}s",
                style = style.durationTextStyle
            )
        }
    }
}

