package com.a3.yearlyprogess.app.navigation


import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.a3.yearlyprogess.feature.home.ui.HomeScreen

//import com.a3.yearlyprogess.feature.events.ui.EventListScreen
//import com.a3.yearlyprogess.feature.events.ui.EventDetailScreen
//import com.a3.yearlyprogess.feature.settings.ui.SettingsScreen
//import com.a3.yearlyprogess.feature.widgets.ui.WidgetShowcaseScreen

@Composable
fun AppNavGraph(
    navController: NavHostController, modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Destination.Home,
        modifier = modifier
    ) {
        // Home
        composable<Destination.Home> {
            HomeScreen(
//                onNavigateEvents = { navController.navigate(Destination.Events.route) },
//                onNavigateSettings = { navController.navigate(Destination.Settings.route) },
//                onNavigateWidgets = { navController.navigate(Destination.Widgets.route) }
            )
        }

        // Events
        composable<Destination.Events> {
            Text("EventListScreen")
//            EventListScreen(
//                onEventClick = { eventId ->
//                    navController.navigate(Destination.EventDetail.createRoute(eventId))
//                }
//            )
        }
//
//        // Event Detail with argument
//        composable(
//            route = Destination.EventDetail.route,
//            arguments = listOf(
//                androidx.navigation.navArgument("eventId") {
//                    type = androidx.navigation.NavType.StringType
//                }
//            )
//        ) { backStackEntry ->
//            val eventId = backStackEntry.arguments?.getString("eventId") ?: return@composable
//            EventDetailScreen(eventId = eventId)
//        }

        // Widgets
        composable<Destination.Widgets> {
            Text("WidgetShowcaseScreen")
//            WidgetShowcaseScreen()
        }

        // Settings
        composable<Destination.Settings> {
            Text("SettingsScreen")
//            SettingsScreen()
        }
    }
}
