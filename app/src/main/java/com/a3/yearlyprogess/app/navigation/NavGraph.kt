package com.a3.yearlyprogess.app.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.runtime.Composable
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import androidx.navigation.toRoute
import com.a3.yearlyprogess.app.MainViewModel
import com.a3.yearlyprogess.app.ui.MainScaffold
import com.a3.yearlyprogess.core.backup.BackupManager
import com.a3.yearlyprogess.app.ui.WelcomeScreen
import com.a3.yearlyprogess.feature.events.ui.EventCreateScreen
import com.a3.yearlyprogess.feature.events.ui.ImportEventsScreen
import com.a3.yearlyprogess.feature.settings.ui.SettingsScaffold
import de.raphaelebner.roomdatabasebackup.core.RoomBackup

@Composable
fun AppNavGraph(
    navController: NavHostController,
    windowWidthSizeClass: WindowWidthSizeClass,
    backupManager: BackupManager,
    mainViewModel: MainViewModel,
    startDestination: Destination,
    onWelcomeCompleted: () -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = spring(
                    dampingRatio = 1.0f,
                    stiffness = Spring.StiffnessMediumLow,
                ),
            )
        },
        exitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = spring(
                    dampingRatio = 1.0f,
                    stiffness = Spring.StiffnessMediumLow,
                ),
            )
        },
        popEnterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = spring(
                    dampingRatio = 1.0f,
                    stiffness = Spring.StiffnessMediumLow,
                ),
            )
        },
        popExitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = spring(
                    dampingRatio = 1.0f,
                    stiffness = Spring.StiffnessMediumLow,
                ),
            )
        },
    ) {
        composable<Destination.Welcome> {
            WelcomeScreen(
                onStartClicked = onWelcomeCompleted
            )
        }

        composable<Destination.MainFlow> {
            MainScaffold(
                parentNavController = navController,
                windowWidthSizeClass = windowWidthSizeClass,
                backupManager = backupManager,
                mainViewModel = mainViewModel
            )
        }

        composable<Destination.EventDetail> { backStackEntry ->
            val args = backStackEntry.toRoute<Destination.EventDetail>()
            EventCreateScreen(
                eventId = args.editId,  
                onNavigateUp = {
                    navController.navigateUp()
                }
            )
        }

        composable<Destination.EventCreate> {
            EventCreateScreen(
                onNavigateUp = {
                    navController.navigateUp()
                }
            )
        }

        navigation<Destination.SettingsGraph>(
            startDestination = Destination.SettingsHome
        ) {
            composable<Destination.SettingsHome> {
                SettingsScaffold(
                    navController = navController,
                    destination = Destination.SettingsHome
                )
            }
            composable<Destination.SettingsLocation> {
                SettingsScaffold(
                    navController = navController,
                    destination = Destination.SettingsLocation
                )
            }
            composable<Destination.SettingsNotification> {
                SettingsScaffold(
                    navController = navController,
                    destination = Destination.SettingsNotification
                )
            }
        }

        composable<Destination.ImportEvents> {
            ImportEventsScreen(
                navController =  navController,
            )
        }
    }
}
