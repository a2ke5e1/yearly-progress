package com.a3.yearlyprogess.app.navigation

import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Widgets
import androidx.compose.runtime.Composable
import kotlinx.serialization.Serializable


sealed class Destination {
    @Serializable
    data object Home : Destination()
    @Serializable
    data object Events : Destination()

    @Serializable
    data class EventDetail(val editId: String) : Destination()

    @Serializable
    data class EventEdit(val editId: String) : Destination()

    @Serializable
    data object EventCreate : Destination()

    @Serializable
    data object Settings : Destination()
    @Serializable
    data object Widgets : Destination()
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