package com.a3.yearlyprogess.feature.settings.ui

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.a3.yearlyprogess.R
import com.a3.yearlyprogess.app.navigation.SettingsDestination
import com.a3.yearlyprogess.app.ui.AppTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScaffold(
    parentNavController: NavHostController, // parent for navigating back to main app
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val settingsNavController = rememberNavController() // local controller for settings flow

    val navBackStackEntry by settingsNavController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // Determine the title based on current destination
    val topBarTitle = when (currentDestination?.route) {
        SettingsDestination.SettingsHome::class.qualifiedName ->
            stringResource(R.string.settings)
        SettingsDestination.SettingsLocation::class.qualifiedName ->
            stringResource(R.string.location)
        SettingsDestination.SettingsNotification::class.qualifiedName ->
            stringResource(R.string.progress_notification)
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
                    // If we're on a nested settings page, go back within settings
                    if (settingsNavController.previousBackStackEntry != null) {
                        settingsNavController.navigateUp()
                    } else {
                        // Otherwise, go back to main app
                        parentNavController.navigateUp()
                    }
                }
            )
        },
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        SettingsNavHost(
            navController = settingsNavController,
            viewModel = viewModel,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

@Composable
private fun SettingsNavHost(
    navController: NavHostController,
    viewModel: SettingsViewModel,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = SettingsDestination.SettingsHome,
        modifier = modifier,
        enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(700)) },
        exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(700)) },
        popEnterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(700)) },
        popExitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(700)) }

    ) {
        composable<SettingsDestination.SettingsHome> {
            SettingsHomeScreen(
                viewModel = viewModel,
                onNavigateToLocation = {
                    navController.navigate(SettingsDestination.SettingsLocation)
                },
                onNavigateToNotification = {
                    navController.navigate(SettingsDestination.SettingsNotification)
                }
            )
        }

        composable<SettingsDestination.SettingsLocation> {
            SettingsLocationScreen(
                viewModel = viewModel
            )
        }

        composable<SettingsDestination.SettingsNotification> {
            SettingsNotificationScreen()
        }
    }
}