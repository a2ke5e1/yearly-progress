package com.a3.yearlyprogess.app.navigation

import androidx.annotation.StringRes
import com.a3.yearlyprogess.R
import kotlinx.serialization.Serializable


sealed class Destination {
    // Welcome screen
    @Serializable
    data object Welcome : Destination()

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


    @Serializable
    data object SettingsGraph : Destination()

    @Serializable
    data object SettingsHome : Destination()

    @Serializable
    data object SettingsLocation : Destination()

    @Serializable
    data object SettingsNotification : Destination()

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
    @StringRes val labelRes: Int,
)

val appNavItems = listOf(
    NavItem(
        Destination.Home,
        R.string.progress,
    ),
    NavItem(
        Destination.Widgets,
        R.string.widgets,
    ),
    NavItem(
        Destination.Events,
        R.string.events,
    ),
    NavItem(
        Destination.SettingsGraph,
        R.string.settings
    )
)
