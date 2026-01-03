package com.a3.yearlyprogess.feature.home.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.a3.yearlyprogess.core.domain.model.AppSettings
import com.a3.yearlyprogess.core.ui.components.ad.AdCard
import com.a3.yearlyprogess.core.ui.components.ad.AdCardDefaults
import com.a3.yearlyprogess.core.ui.style.CardCornerStyle
import com.a3.yearlyprogess.core.util.TimePeriod
import com.a3.yearlyprogess.feature.home.HomeUiState
import com.a3.yearlyprogess.feature.home.HomeViewModel
import com.a3.yearlyprogess.feature.home.ui.components.DayNightProgressCard
import com.a3.yearlyprogess.feature.home.ui.components.LocationPermissionDialog
import com.a3.yearlyprogess.feature.home.ui.components.LocationRequiredCard
import com.a3.yearlyprogess.feature.home.ui.components.ProgressCard
import com.a3.yearlyprogess.feature.home.ui.components.ProgressCardDefaults
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val shouldShowPermissionDialog by viewModel.shouldShowPermissionDialog.collectAsState()

    val locationPermissionState = rememberPermissionState(
        permission = android.Manifest.permission.ACCESS_COARSE_LOCATION
    ) { isGranted ->
        if (isGranted) {
            viewModel.onPermissionGranted()
        } else {
            viewModel.onPermissionDenied()
        }
    }

    if (shouldShowPermissionDialog) {
        LocationPermissionDialog(
            onDismiss = {
                viewModel.onPermissionDenied()
            },
            onConfirm = {
                locationPermissionState.launchPermissionRequest()
            }
        )
    }

    BoxWithConstraints {
        // maxWidth is automatically provided in Dp
        val isLargeEnoughForGrid = maxWidth >= 600.dp
        if (isLargeEnoughForGrid) {
            HomeScreenGrid(
                state = state,
                settings = settings,
                viewModel = viewModel
            )
        } else {
            HomeScreenColumn(
                state = state,
                settings = settings,
                viewModel = viewModel
            )
        }
    }





}

@Composable
private fun HomeScreenColumn(
    state: HomeUiState,
    settings: AppSettings,
    viewModel: HomeViewModel
) {
    LazyColumn(
        modifier = Modifier
            .padding(14.dp, 0.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        // Time period cards
        item {
            ProgressCard(
                timePeriod = TimePeriod.YEAR,
                style = ProgressCardDefaults.progressCardStyle(
                    cornerStyle = CardCornerStyle.FirstInList
                ),
                settings = settings.progressSettings
            )
        }
        item {
            ProgressCard(
                timePeriod = TimePeriod.MONTH,
                style = ProgressCardDefaults.progressCardStyle(
                    cornerStyle = CardCornerStyle.MiddleInList
                ),
                settings = settings.progressSettings
            )
        }
        item {
            ProgressCard(
                timePeriod = TimePeriod.WEEK,
                style = ProgressCardDefaults.progressCardStyle(
                    cornerStyle = CardCornerStyle.MiddleInList
                ),
                settings = settings.progressSettings
            )
        }
        item {
            ProgressCard(
                timePeriod = TimePeriod.DAY,
                style = ProgressCardDefaults.progressCardStyle(
                    cornerStyle = when (state) {
                        is HomeUiState.Success -> CardCornerStyle.MiddleInList
                        is HomeUiState.LocationRequired -> CardCornerStyle.MiddleInList
                        else -> CardCornerStyle.LastInList
                    }
                ),
                settings = settings.progressSettings
            )
            AdCard(
                style = AdCardDefaults.adCardStyle(
                    cornerStyle = when (state) {
                        is HomeUiState.Success -> CardCornerStyle.MiddleInList
                        is HomeUiState.LocationRequired -> CardCornerStyle.MiddleInList
                        else -> CardCornerStyle.LastInList
                    }
                ),
                modifier = Modifier.padding(top = 4.dp)
            )
        }
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
            is HomeUiState.LocationRequired -> {
                item {
                    LocationRequiredCard(
                        onGoToSettings = { viewModel.onGoToSettings() },
                        cornerStyle = CardCornerStyle.LastInList
                    )
                    Spacer(Modifier.height(4.dp))
                }
            }
            is HomeUiState.Error -> {
                item {
                    Text(
                        text = "Error: ${state.message}",
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
            is HomeUiState.Success -> {
                val data = state.data
                item {
                    DayNightProgressCard(
                        sunriseSunsetList = data,
                        dayLight = true,
                        style = ProgressCardDefaults.progressCardStyle(
                            cornerStyle = CardCornerStyle.MiddleInList
                        ),
                        settings = settings.progressSettings
                    )
                }
                item {
                    DayNightProgressCard(
                        sunriseSunsetList = data,
                        dayLight = false,
                        style = ProgressCardDefaults.progressCardStyle(
                            cornerStyle = CardCornerStyle.LastInList
                        ),
                        settings = settings.progressSettings
                    )
                    Spacer(Modifier.height(4.dp))
                }
            }
        }
    }
}

@Composable
private fun HomeScreenGrid(
    state: HomeUiState,
    settings: AppSettings,
    viewModel: HomeViewModel
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier
            .padding(14.dp, 0.dp)
            .fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        contentPadding = PaddingValues(vertical = 0.dp)
    ) {
        // Time period cards
        item {
            ProgressCard(
                timePeriod = TimePeriod.YEAR,
                style = ProgressCardDefaults.progressCardStyle(
                    cornerStyle = CardCornerStyle.Default
                ),
                settings = settings.progressSettings
            )
        }
        item {
            ProgressCard(
                timePeriod = TimePeriod.MONTH,
                style = ProgressCardDefaults.progressCardStyle(
                    cornerStyle = CardCornerStyle.Default
                ),
                settings = settings.progressSettings
            )
        }
        item {
            ProgressCard(
                timePeriod = TimePeriod.WEEK,
                style = ProgressCardDefaults.progressCardStyle(
                    cornerStyle = CardCornerStyle.Default
                ),
                settings = settings.progressSettings
            )
        }
        item {
            ProgressCard(
                timePeriod = TimePeriod.DAY,
                style = ProgressCardDefaults.progressCardStyle(
                    cornerStyle = CardCornerStyle.Default
                ),
                settings = settings.progressSettings
            )
        }

        when (state) {
            is HomeUiState.Loading -> {
                item {
                    Box(
                        modifier = Modifier.height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
            is HomeUiState.LocationRequired -> {
                item {
                    LocationRequiredCard(
                        onGoToSettings = { viewModel.onGoToSettings() },
                        cornerStyle = CardCornerStyle.Default
                    )
                }
            }
            is HomeUiState.Error -> {
                item {
                    Text(
                        text = "Error: ${state.message}",
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
            is HomeUiState.Success -> {
                val data = state.data
                item {
                    DayNightProgressCard(
                        sunriseSunsetList = data,
                        dayLight = true,
                        style = ProgressCardDefaults.progressCardStyle(
                            cornerStyle = CardCornerStyle.Default
                        ),
                        settings = settings.progressSettings
                    )
                }
                item {
                    DayNightProgressCard(
                        sunriseSunsetList = data,
                        dayLight = false,
                        style = ProgressCardDefaults.progressCardStyle(
                            cornerStyle = CardCornerStyle.Default
                        ),
                        settings = settings.progressSettings
                    )
                }
            }
        }
    }
}