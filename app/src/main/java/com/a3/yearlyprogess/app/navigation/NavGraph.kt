package com.a3.yearlyprogess.app.navigation


import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.a3.yearlyprogess.feature.events.ui.EventDetailScreen
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
            Column {
                Text("EventListScreen")
                Button(onClick = {
                    navController.navigate(Destination.EventDetail("test route"))
                }) { Text("test route") }

                Button(onClick = {
                    navController.navigate(Destination.EventDetail("test route 2"))
                }) { Text("test route 2") }
            }
//            EventListScreen(
//                onEventClick = { eventId ->
//                    navController.navigate(Destination.EventDetail.createRoute(eventId))
//                }
//            )
        }

        // Event Detail with argument
        composable<Destination.EventDetail> { backStackEntry ->
            val args = backStackEntry.toRoute<Destination.EventDetail>()
            EventDetailScreen(eventName = args.editId)
        }

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
