package com.a3.yearlyprogess.feature.events.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.a3.yearlyprogess.core.ui.style.CardCornerStyle
import com.a3.yearlyprogess.feature.events.presentation.EventViewModel
import com.a3.yearlyprogess.feature.events.ui.components.EventDetailCard
import com.a3.yearlyprogess.feature.events.ui.components.EventDetailCardDefaults
import com.a3.yearlyprogess.feature.events.ui.components.EventList

@Composable
fun EventListScreen(
    viewModel: EventViewModel = hiltViewModel(),
    onNavigateToEventDetail: (Int) -> Unit,
) {
    val events by viewModel.events.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val selectedIds by viewModel.selectedIds.collectAsState()

    BackHandler(enabled = selectedIds.isNotEmpty()) {
        viewModel.clearSelection()
    }

    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) {
                viewModel.clearSelection()
            }
        }
        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
        }
    }

    EventList(
        events = events,
        selectedIds = selectedIds,
        emptyText = "No events yet. Add one to get started!",
        onItemClick = { event ->
            if (selectedIds.isEmpty()) {
                onNavigateToEventDetail(event.id)
            } else {
                viewModel.toggleSelection(event.id)
            }
        },
        onItemLongPress = { viewModel.toggleSelection(it.id) },
        settings = settings.progressSettings
    )

}