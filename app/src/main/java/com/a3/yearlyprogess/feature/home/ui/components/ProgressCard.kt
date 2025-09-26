package com.a3.yearlyprogess.feature.home.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.a3.yearlyprogess.core.util.ProgressSettings
import com.a3.yearlyprogess.core.util.TimePeriod
import com.a3.yearlyprogess.core.util.YearlyProgressUtil
import kotlinx.coroutines.delay

@Composable
fun ProgressCard(
    timePeriod: TimePeriod,
    settings: ProgressSettings = ProgressSettings(),
    modifier: Modifier = Modifier, decimals: Int = 13, refreshInterval: Long = 1L
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

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(160.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
    ) {

        Box(
            modifier = modifier
                .fillMaxWidth((progress / 100).toFloat().coerceIn(0f, 1f))
                .fillMaxHeight()
                .background(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(16.dp)
                )
                .align(Alignment.CenterStart)
        )

        Column(
            modifier = Modifier.padding(18.dp), horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = timePeriod.name.lowercase().replaceFirstChar { it.uppercase() },
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = when (timePeriod) {
                    TimePeriod.MONTH -> progressUtil.getMonthName(
                        progressUtil.getCurrentPeriodValue(
                            timePeriod
                        )
                    )

                    TimePeriod.WEEK -> progressUtil.getWeekDayName(
                        progressUtil.getCurrentPeriodValue(
                            timePeriod
                        )
                    )

                    TimePeriod.DAY -> progressUtil.getCurrentPeriodValue(timePeriod)
                        .toString() + progressUtil.getOrdinalSuffix(
                        progressUtil.getCurrentPeriodValue(
                            timePeriod
                        )
                    )

                    else -> progressUtil.getCurrentPeriodValue(timePeriod).toString()
                },
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "%.${decimals}f%%".format(progress), // update directly using Double
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Text(
                text = "of ${duration}s",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

