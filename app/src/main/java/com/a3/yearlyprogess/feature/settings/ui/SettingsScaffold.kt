package com.a3.yearlyprogess.feature.settings.ui

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.a3.yearlyprogess.R
import com.a3.yearlyprogess.app.navigation.Destination
import com.a3.yearlyprogess.app.ui.AppTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScaffold(
    navController: NavHostController,
    destination: Destination,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val topBarTitle = when (destination) {
        Destination.SettingsHome -> stringResource(R.string.settings)
        Destination.SettingsLocation -> stringResource(R.string.location)
        Destination.SettingsNotification -> stringResource(R.string.progress_notification)
        else -> stringResource(R.string.settings)
    }

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            AppTopBar(
                title = topBarTitle,
                scrollBehavior = scrollBehavior,
                onNavigateUp = {
                    navController.navigateUp()
                }
            )
        },
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Modifier.padding(innerPadding).let { modifier ->
            when (destination) {
                Destination.SettingsHome -> {
                    SettingsHomeScreen(
                        viewModel = viewModel,
                        onNavigateToLocation = {
                            navController.navigate(Destination.SettingsLocation)
                        },
                        onNavigateToNotification = {
                            navController.navigate(Destination.SettingsNotification)
                        },
                        modifier = modifier
                    )
                }

                Destination.SettingsLocation -> {
                    SettingsLocationScreen(
                        viewModel = viewModel,
                        modifier = modifier
                    )
                }

                Destination.SettingsNotification -> {
                    SettingsNotificationScreen(
                        modifier = modifier
                    )
                }
                else -> {}
            }
        }
    }
}
