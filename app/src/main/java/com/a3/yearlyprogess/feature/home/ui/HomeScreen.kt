package com.a3.yearlyprogess.feature.home.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.a3.yearlyprogess.core.util.TimePeriod
import com.a3.yearlyprogess.feature.home.ui.components.ProgressCard

@Composable
fun HomeScreen() {

        LazyColumn(
            modifier = Modifier.padding(8.dp, 0.dp).fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            item { ProgressCard(timePeriod = TimePeriod.YEAR) }
            item { ProgressCard(timePeriod = TimePeriod.MONTH) }
            item { ProgressCard(timePeriod = TimePeriod.WEEK) }
            item { ProgressCard(timePeriod = TimePeriod.DAY) }
            item { ProgressCard(timePeriod = TimePeriod.DAY) }
            item { ProgressCard(timePeriod = TimePeriod.DAY) }
        }

}