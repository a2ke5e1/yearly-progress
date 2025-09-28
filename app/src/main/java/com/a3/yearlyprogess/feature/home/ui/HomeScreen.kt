package com.a3.yearlyprogess.feature.home.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.a3.yearlyprogess.core.util.TimePeriod
import com.a3.yearlyprogess.feature.home.HomeUiState
import com.a3.yearlyprogess.feature.home.HomeViewModel
import com.a3.yearlyprogess.feature.home.ui.components.ProgressCard

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    LazyColumn(
        modifier = Modifier
            .padding(8.dp, 0.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        item { ProgressCard(timePeriod = TimePeriod.YEAR) }
        item { ProgressCard(timePeriod = TimePeriod.MONTH) }
        item { ProgressCard(timePeriod = TimePeriod.WEEK) }
        item { ProgressCard(timePeriod = TimePeriod.DAY) }
        when (state) {
            is HomeUiState.Loading -> item { CircularProgressIndicator() }
            is HomeUiState.Error -> item { Text("Error: ${(state as HomeUiState.Error).message}") }
            is HomeUiState.Success -> {
                val data = (state as HomeUiState.Success).data
                    item {
                        Text("$data")
                    }

            }
        }
    }

}