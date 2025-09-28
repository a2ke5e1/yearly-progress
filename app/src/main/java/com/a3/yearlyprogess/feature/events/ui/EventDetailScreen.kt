package com.a3.yearlyprogess.feature.events.ui

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun EventDetailScreen(
     eventName: String,
) {
    Text(text = "Event Detail for $eventName")
}