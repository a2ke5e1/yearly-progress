package com.a3.yearlyprogess.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.a3.yearlyprogess.*
import kotlinx.coroutines.delay

@Composable
fun ProgressCard(
    timePeriod: TimePeriod,
    settings: ProgressSettings = ProgressSettings(),
    modifier: Modifier = Modifier,
    decimals: Int = 6,
    refreshInterval: Long = 200L
) {
    val progressUtil = remember { YearlyProgressUtil(settings) }

    val startTime = progressUtil.calculateStartTime(timePeriod)
    val endTime = progressUtil.calculateEndTime(timePeriod)

    var progress by remember { mutableStateOf(progressUtil.calculateProgress(startTime, endTime)) }

    LaunchedEffect(Unit) {
        while (progress < 100.0) {
            progress = progressUtil.calculateProgress(startTime, endTime)
            delay(refreshInterval)
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(140.dp)
            .background(
                brush = Brush.horizontalGradient(listOf(Color(0xFF4CAF50), Color(0xFF81C784))),
                shape = RoundedCornerShape(16.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = when (settings.calculationType) {
                    CalculationType.ELAPSED -> "Elapsed"
                    CalculationType.REMAINING -> "Remaining"
                },
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "%.${decimals}f%%".format(progress), // update directly using Double
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${timePeriod.name.lowercase().replaceFirstChar { it.uppercase() }} Progress",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.9f)
            )
        }
    }
}

