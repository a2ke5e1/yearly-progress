package com.a3.yearlyprogess.app.navigation

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