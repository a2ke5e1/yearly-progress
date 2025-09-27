package com.a3.yearlyprogess.app.navigation

import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Widgets
import androidx.compose.runtime.Composable
import kotlinx.serialization.Serializable

@Serializable
sealed class Destination(val route: String) {
    @Serializable
    data object Home : Destination("home")
    @Serializable
    data object Events : Destination("events")

    @Serializable
    data object EventDetail : Destination("events/detail/{eventId}") {
        fun createRoute(eventId: String) = "events/detail/$eventId"
    }

    @Serializable
    data object EventEdit : Destination("events/edit/{eventId}") {
        fun createRoute(eventId: String) = "events/edit/$eventId"
    }

    @Serializable
    data object EventCreate : Destination("events/create")

    @Serializable
    data object Settings : Destination("settings")
    @Serializable
    data object Widgets : Destination("widgets")
}



data class NavItem(
    val route: Destination,
    val label: String,
    val icon: @Composable () -> Unit,
    val selectedIcon: @Composable () -> Unit
)

val appNavItems = listOf(
    NavItem(
        Destination.Home,
        "Progress",
        { Icon(Icons.Rounded.BarChart, contentDescription = "Progress") },
        { Icon(Icons.Rounded.BarChart, contentDescription = "Progress") }
    ),
    NavItem(
        Destination.Widgets,
        "Widgets",
        { Icon(Icons.Rounded.Widgets, contentDescription = "Widgets") },
        { Icon(Icons.Rounded.Widgets, contentDescription = "Widgets") }
    ),
    NavItem(
        Destination.Events,
        "Events",
        { Icon(Icons.Filled.Event, contentDescription = "Events") },
        { Icon(Icons.Filled.Event, contentDescription = "Events") }
    )
)