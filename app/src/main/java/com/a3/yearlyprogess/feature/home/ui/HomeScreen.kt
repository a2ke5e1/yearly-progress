package com.a3.yearlyprogess.feature.home.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.a3.yearlyprogess.core.util.TimePeriod
import com.a3.yearlyprogess.feature.home.HomeUiState
import com.a3.yearlyprogess.feature.home.HomeViewModel
import com.a3.yearlyprogess.feature.home.LocationState
import com.a3.yearlyprogess.feature.home.ui.components.DayNightProgressCard
import com.a3.yearlyprogess.feature.home.ui.components.LocationPermissionDialog
import com.a3.yearlyprogess.feature.home.ui.components.ProgressCard
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val locationState by viewModel.locationState.collectAsState()

    var showPermissionDialog by remember { mutableStateOf(false) }

    // Only request coarse location permission
    val locationPermissionState = rememberPermissionState(
        permission = android.Manifest.permission.ACCESS_COARSE_LOCATION
    ) { isGranted ->
        if (isGranted) {
            viewModel.onPermissionGranted()
        } else {
            viewModel.onPermissionDenied()
        }
    }

    // Show dialog when permission is required
    LaunchedEffect(locationState) {
        if (locationState is LocationState.PermissionRequired &&
            !locationPermissionState.status.isGranted) {
            showPermissionDialog = true
        }
    }

    if (showPermissionDialog) {
        LocationPermissionDialog(
            onDismiss = {
                showPermissionDialog = false
                viewModel.onPermissionDenied()
            },
            onConfirm = {
                showPermissionDialog = false
                locationPermissionState.launchPermissionRequest()
            }
        )
    }

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
            is HomeUiState.Loading -> {
                item {
                    Box(
                        modifier = Modifier
                            .fillParentMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
            is HomeUiState.Error -> {
                item {
                    Text(
                        text = "Error: ${(state as HomeUiState.Error).message}",
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
            is HomeUiState.Success -> {
                val data = (state as HomeUiState.Success).data
                item {
                    DayNightProgressCard(
                        sunriseSunsetList = data,
                        dayLight = true
                    )
                }
                item {
                    DayNightProgressCard(
                        sunriseSunsetList = data,
                        dayLight = false
                    )
                    Spacer(Modifier.height(4.dp))
                }
            }
        }
    }
}