package com.a3.yearlyprogess.feature.home.ui.components

import android.icu.text.NumberFormat
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.a3.yearlyprogess.R
import com.a3.yearlyprogess.core.ui.interaction.applyPressGesture
import com.a3.yearlyprogess.core.ui.interaction.rememberPressInteractionState
import com.a3.yearlyprogess.core.util.ProgressSettings
import com.a3.yearlyprogess.core.util.YearlyProgressUtil
import com.a3.yearlyprogess.data.local.getStartAndEndTime
import com.a3.yearlyprogess.domain.model.SunriseSunset
import kotlinx.coroutines.delay
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

object DayNightCardDefaults {
    @Composable
    fun dayStyle(): ProgressCardStyle = ProgressCardDefaults.progressCardStyle()

    @Composable
    fun nightStyle(): ProgressCardStyle = ProgressCardDefaults.progressCardStyle()
}

@Composable
fun DayNightProgressCard(
    modifier: Modifier = Modifier,
    settings: ProgressSettings = ProgressSettings(),
    refreshInterval: Long = 1L,
    sunriseSunsetList: List<SunriseSunset>,
    dayLight: Boolean,
    style: ProgressCardStyle = if (dayLight) DayNightCardDefaults.dayStyle() else DayNightCardDefaults.nightStyle(),
) {
    val decimals = settings.decimalDigits.coerceIn(0, 13)
    val progressUtil = remember(settings) { YearlyProgressUtil(settings) }
    val (startTime, endTime) = remember(sunriseSunsetList, dayLight) {
        getStartAndEndTime(dayLight, sunriseSunsetList)
    }

    var progress by remember { mutableDoubleStateOf(progressUtil.calculateProgress(startTime, endTime)) }

    LaunchedEffect(startTime, endTime) {
        while (true) {
            progress = progressUtil.calculateProgress(startTime, endTime)
            delay(refreshInterval)
        }
    }

    val duration = (endTime - startTime) / 1000
    val durationFormatted = remember(duration) {
        NumberFormat.getNumberInstance().format(duration)
    }

    val formatter = remember {
        DateTimeFormatter
            .ofLocalizedTime(FormatStyle.SHORT)
            .withLocale(settings.uLocale.toLocale())
            .withZone(ZoneId.systemDefault())
    }
    val startTimeFormatted = remember(startTime) { formatter.format(Instant.ofEpochMilli(startTime)) }
    val endTimeFormatted = remember(endTime) { formatter.format(Instant.ofEpochMilli(endTime)) }

    val pressState = rememberPressInteractionState(style.pressConfig)
    val animatedCorners = pressState.animateCorners(default = style.cornerStyle)

    Box(
        modifier = modifier
            .height(style.cardHeight)
            .fillMaxWidth()
            .clip(style.cornerStyle.toAnimatedShape(animatedCorners))
            .background(style.backgroundColor)
            .applyPressGesture(pressState)
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth((progress / 100).toFloat().coerceIn(0f, 1f))
                .background(
                    style.progressBarColor,
                    shape = animatedCorners.toShape() // ← Use animated shape!
                )
                .clip(animatedCorners.toShape()) // ← Also clip to match
                .align(Alignment.CenterStart)
        )

        Column(
            modifier = Modifier.padding(style.cardPadding),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = if (dayLight) stringResource(R.string.day_light) else stringResource(R.string.night_light),
                style = style.labelTextStyle
            )

            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (dayLight) Icons.Default.WbSunny else Icons.Default.Nightlight,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "$startTimeFormatted - $endTimeFormatted",
                    style = style.labelTextStyle
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            FormattedPercentage(
                value = progress,
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