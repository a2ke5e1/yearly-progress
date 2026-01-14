package com.a3.yearlyprogess.app.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.a3.yearlyprogess.R
import com.a3.yearlyprogess.app.MainViewModel
import com.a3.yearlyprogess.app.navigation.AppNavigationRail
import com.a3.yearlyprogess.app.navigation.BottomNavigationBar
import com.a3.yearlyprogess.app.navigation.Destination
import com.a3.yearlyprogess.app.navigation.appNavItems
import com.a3.yearlyprogess.core.backup.BackupManager
import com.a3.yearlyprogess.feature.events.presentation.EventViewModel
import com.a3.yearlyprogess.feature.events.ui.EventListScreen
import com.a3.yearlyprogess.feature.home.HomeViewModel
import com.a3.yearlyprogess.feature.home.ui.HomeScreen
import de.raphaelebner.roomdatabasebackup.core.RoomBackup
import com.a3.yearlyprogess.feature.widgets.ui.WidgetPreviewScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScaffold(
    parentNavController: NavHostController, // parent for cross-graph navigation
    windowWidthSizeClass: WindowWidthSizeClass,
    backupManager: BackupManager,
    mainViewModel: MainViewModel,
    eventViewModel: EventViewModel = hiltViewModel(),
    homeViewModel: HomeViewModel = hiltViewModel(),
) {
    val mainFlowNavController = rememberNavController() // local controller for this flow


    val navBackStackEntry by mainFlowNavController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val topBarTitle = appNavItems.firstOrNull { item ->
        currentDestination?.hierarchy?.any { it.hasRoute(item.route::class) } == true
    }?.label ?: currentDestination?.label?.toString() ?: "Yearly Progress"
    val showFab = currentDestination?.hasRoute(Destination.Events::class) == true


    val bottomBarContent: @Composable () -> Unit = {
        AnimatedVisibility(
            visible = true,
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = tween(250)
            ) + fadeIn(tween(250)),
            exit = slideOutVertically(
                targetOffsetY = { it },
                animationSpec = tween(200)
            ) + fadeOut(tween(200))
        ) {
            BottomNavigationBar(mainFlowNavController)
        }
    }

    var lastClickTime by remember { mutableLongStateOf(0L) }
    val fabContent: @Composable () -> Unit = {
        AnimatedVisibility(
            visible = showFab,
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = tween(250)
            ) + fadeIn(tween(250)),
            exit = slideOutVertically(
                targetOffsetY = { it },
                animationSpec = tween(200)
            ) + fadeOut(tween(200))
        ) {
            FloatingActionButton(
                onClick = {
                    val now = System.currentTimeMillis()
                    if (now - lastClickTime > 600) { // debounce 600ms
                        lastClickTime = now
                        parentNavController.navigate(Destination.EventCreate) {
                            launchSingleTop = true
                        }
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.add_event)
                )
            }
        }
    }

    val homeScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val eventsScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val widgetsScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    val currentScrollBehavior = when {
        currentDestination?.hasRoute(Destination.Home::class) == true -> homeScrollBehavior
        currentDestination?.hasRoute(Destination.Events::class) == true -> eventsScrollBehavior
        currentDestination?.hasRoute(Destination.Widgets::class) == true -> widgetsScrollBehavior
        else -> homeScrollBehavior
    }

    val topBar: @Composable () -> Unit = {
        AppTopBar(
            title = topBarTitle,
            scrollBehavior = currentScrollBehavior,
            onSettingsClick = {
                // use parentNavController to open global screens
                parentNavController.navigate(Destination.SettingsGraph)
            },
            onImportEvents = {
                parentNavController.navigate(Destination.ImportEvents)
            },
            eventViewModel = eventViewModel,
            backupManager = backupManager,
            showShareButton = true,
            showAboutButton = true,
            showBackAndRestore = true
        )
    }

    val content: @Composable (innerPadding: PaddingValues) -> Unit = { innerPadding ->
        NavHost(
            navController = mainFlowNavController,
            startDestination = Destination.Home,
            modifier = Modifier.padding(innerPadding),
            enterTransition = { fadeIn(tween(200)) },
            exitTransition = { fadeOut(tween(200)) },
            popEnterTransition = { fadeIn(tween(200)) },
            popExitTransition = { fadeOut(tween(200)) }
        ) {
            composable<Destination.Home> {
                HomeScreen(
                    viewModel = homeViewModel,
                    mainViewModel = mainViewModel,
                    onNavigateToSettingsLocation = {
                        parentNavController.navigate(Destination.SettingsLocation)
                    }
                )
            }

            composable<Destination.Events> {
                EventListScreen(
                    viewModel = eventViewModel,
                    mainViewModel = mainViewModel,
                    onNavigateToEventDetail = {
                        parentNavController.navigate(Destination.EventDetail(it))  {
                            launchSingleTop = true
                        }
                    },
                )
            }

            composable<Destination.Widgets> {
                WidgetPreviewScreen(
                    homeViewModel = homeViewModel,
                )
            }
        }
    }

    when (windowWidthSizeClass) {
        WindowWidthSizeClass.Compact -> {
            Scaffold(
                modifier = Modifier.nestedScroll(currentScrollBehavior.nestedScrollConnection),
                topBar = topBar,
                bottomBar = bottomBarContent,
                contentWindowInsets = WindowInsets.safeDrawing,
                floatingActionButton = fabContent
            ) { innerPadding ->
                content(innerPadding)
            }
        }

        else -> {
            // Foldable / Tablet
            Row(
                modifier = Modifier.fillMaxSize(),
            ) {
                AppNavigationRail(navController = mainFlowNavController)
                Scaffold(
                    modifier = Modifier.nestedScroll(currentScrollBehavior.nestedScrollConnection),
                    topBar = topBar,
                    contentWindowInsets = WindowInsets.safeDrawing,
                    floatingActionButton = fabContent
                ) { innerPadding ->
                    content(
                        PaddingValues(
                            top = innerPadding.calculateTopPadding(),
                            bottom = innerPadding.calculateBottomPadding()
                        )

                    )
                }
            }
        }
    }
}
