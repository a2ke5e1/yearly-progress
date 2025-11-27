package com.a3.yearlyprogess.app.navigation

import kotlinx.serialization.Serializable


sealed class Destination {
    // Parent graph
    @Serializable
    data object MainFlow : Destination()

    // Nested routes inside MainFlow
    @Serializable
    data object Home : Destination()
    @Serializable
    data object Events : Destination()
    @Serializable
    data object Widgets : Destination()

    // Other screens outside nested graph
    @Serializable
    data object Settings : Destination()
    @Serializable
    data class EventDetail(val editId: Int) : Destination()

    @Serializable
    data class EventEdit(val editId: Int) : Destination()
    @Serializable
    data object EventCreate : Destination()

    @Serializable
    data object ImportEvents: Destination()

    @Serializable
    data object SelectCalendars: Destination()

}





data class NavItem(
    val route: Destination,
    val label: String,
)

val appNavItems = listOf(
    NavItem(
        Destination.Home,
        "Progress",

        ),
    NavItem(
        Destination.Widgets,
        "Widgets",
    ),
    NavItem(
        Destination.Events,
        "Events",

        ),
    NavItem(
        Destination.Settings,
        "Settings"
    )
)