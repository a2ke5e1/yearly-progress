package com.a3.yearlyprogess.feature.home.ui.components

import android.icu.text.NumberFormat
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
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
    decimals: Int = 13,
    refreshInterval: Long = 1L,
    sunriseSunsetList: List<SunriseSunset>,
    dayLight: Boolean, // true = day, false = night
    style: ProgressCardStyle = if (dayLight) DayNightCardDefaults.dayStyle() else DayNightCardDefaults.nightStyle()
) {
    val progressUtil = remember { YearlyProgressUtil(settings) }
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

    // ✅ Formatter scoped inside card
    val formatter = remember {
        DateTimeFormatter
            .ofLocalizedTime(FormatStyle.SHORT)
            .withLocale(settings.uLocale.toLocale())
            .withZone(ZoneId.systemDefault())
    }
    val startTimeFormatted = remember(startTime) { formatter.format(Instant.ofEpochMilli(startTime)) }
    val endTimeFormatted = remember(endTime) { formatter.format(Instant.ofEpochMilli(endTime)) }

    var pressed by remember { mutableStateOf(false) }
    val cornerRadius by animateDpAsState(
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
        // Progress background
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth((progress / 100).toFloat().coerceIn(0f, 1f))
                .background(style.progressBarColor, shape = RoundedCornerShape(style.cornerRadiusDefault))
                .align(Alignment.CenterStart)
        )

        Column(
            modifier = Modifier.padding(style.cardPadding),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = if (dayLight) "Day Light" else "Night Light",
                style = style.labelTextStyle
            )

            Spacer(Modifier.height(4.dp))
            // 🌅 Sunrise → Sunset row
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
                text = "of ${durationFormatted}s",
                style = style.durationTextStyle
            )
        }
    }
}
