package com.a3.yearlyprogess.feature.events.ui

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.a3.yearlyprogess.R
import com.a3.yearlyprogess.app.MainViewModel
import com.a3.yearlyprogess.feature.events.presentation.EventViewModel
import com.a3.yearlyprogess.feature.events.ui.components.EventList

@Composable
fun EventListScreen(
    viewModel: EventViewModel = hiltViewModel(),
    mainViewModel: MainViewModel,
    onNavigateToEventDetail: (Int) -> Unit,
) {
    val events by viewModel.events.collectAsState()
    val settings by mainViewModel.appSettings.collectAsState()
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

    settings?.let { settings ->
        EventList(
            events = events,
            selectedIds = selectedIds,
            emptyText = stringResource(R.string.no_events_message),
            onItemClick = { event ->
                if (selectedIds.isEmpty()) {
                    onNavigateToEventDetail(event.id)
                } else {
                    viewModel.toggleSelection(event.id)
                }
            },
            onItemLongPress = { viewModel.toggleSelection(it.id) },
            settings = settings
        )
    }

}