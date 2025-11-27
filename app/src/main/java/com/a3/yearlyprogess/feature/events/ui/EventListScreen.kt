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

    if (events.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("No events yet. Add one to get started!")
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .padding(8.dp, 0.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            itemsIndexed(events, key = { _, event -> event.id }) { index, event ->
                val isSelected = selectedIds.contains(event.id)
                val isPreviousSelected = index > 0 && selectedIds.contains(events[index - 1].id)
                val isNextSelected =
                    index < events.size - 1 && selectedIds.contains(events[index + 1].id)
                EventDetailCard(
                    event = event,
                    isSelected = isSelected,
                    onClick = {
                        if (selectedIds.isEmpty()) {
                            onNavigateToEventDetail(event.id)
                        } else {
                            viewModel.toggleSelection(event.id)
                        }
                    },
                    onLongPress = { viewModel.toggleSelection(event.id) },
                    style = EventDetailCardDefaults.eventDetailCardStyle(
                        cornerStyle = when {
                            events.size <= 1 -> CardCornerStyle.Default

                            isPreviousSelected && isNextSelected ->
                                CardCornerStyle.Default

                            isPreviousSelected && !isNextSelected ->
                                CardCornerStyle.FirstInList

                            !isPreviousSelected && isNextSelected ->
                                if (index == 0) CardCornerStyle.Default else CardCornerStyle.LastInList

                            else ->
                                when (index) {
                                    0 -> CardCornerStyle.FirstInList
                                    events.lastIndex -> CardCornerStyle.LastInList
                                    else -> CardCornerStyle.MiddleInList
                                }
                        }
                    ),
                    settings = settings.progressSettings
                )
            }
            item {
                Spacer(Modifier.height(4.dp))
            }
        }
    }

}