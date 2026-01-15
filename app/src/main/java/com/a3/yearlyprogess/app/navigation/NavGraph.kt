package com.a3.yearlyprogess.app.navigation

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
        // Latest native Activity launch animation (Scale + Fade)
        enterTransition = {
            fadeIn(animationSpec = tween(300)) + 
            scaleIn(initialScale = 0.92f, animationSpec = tween(300))
        },
        exitTransition = {
            fadeOut(animationSpec = tween(150)) + 
            scaleOut(targetScale = 1.08f, animationSpec = tween(150))
        },
        popEnterTransition = {
            fadeIn(animationSpec = tween(300)) + 
            scaleIn(initialScale = 1.08f, animationSpec = tween(300))
        },
        popExitTransition = {
            fadeOut(animationSpec = tween(150)) + 
            scaleOut(targetScale = 0.92f, animationSpec = tween(150))
        }
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
