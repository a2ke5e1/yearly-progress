package com.a3.yearlyprogess.app.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import androidx.navigation.toRoute
import com.a3.yearlyprogess.app.ui.MainScaffold
import com.a3.yearlyprogess.app.ui.WelcomeScreen
import com.a3.yearlyprogess.feature.events.ui.EventCreateScreen
import com.a3.yearlyprogess.feature.events.ui.ImportEventsScreen
import com.a3.yearlyprogess.feature.settings.ui.SettingsScaffold

@Composable
fun AppNavGraph(
    navController: NavHostController,
    windowWidthSizeClass: WindowWidthSizeClass,
    startDestination: Destination,
    onWelcomeCompleted: () -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(700)) },
        exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(700)) },
        popEnterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(700)) },
        popExitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(700)) }

    ) {
        composable<Destination.Welcome> {
            WelcomeScreen(
                onStartClicked = onWelcomeCompleted
            )
        }

        composable<Destination.MainFlow> {
            MainScaffold(
                parentNavController = navController,
                windowWidthSizeClass = windowWidthSizeClass
            )
        }

        composable<Destination.EventDetail> { backStackEntry ->
            val args = backStackEntry.toRoute<Destination.EventDetail>()
//            EventDetailScreen(eventName = args.editId)
            EventCreateScreen(
                eventId = args.editId,  onNavigateUp = {
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
