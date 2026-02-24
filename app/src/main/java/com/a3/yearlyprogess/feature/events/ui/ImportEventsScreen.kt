package com.a3.yearlyprogess.feature.events.ui

import android.Manifest
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Deselect
import androidx.compose.material.icons.outlined.EditCalendar
import androidx.compose.material.icons.outlined.FilterAlt
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.SelectAll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.a3.yearlyprogess.R
import com.a3.yearlyprogess.core.ui.components.DateRangePickerModal
import com.a3.yearlyprogess.core.ui.components.SelectDialog
import com.a3.yearlyprogess.core.ui.style.CardCornerStyle
import com.a3.yearlyprogess.core.util.Log
import com.a3.yearlyprogess.feature.events.presentation.CalendarUiState
import com.a3.yearlyprogess.feature.events.presentation.ImportEventsViewModel
import com.a3.yearlyprogess.feature.events.ui.components.CalendarPermissionDialog
import com.a3.yearlyprogess.feature.events.ui.components.CalendarRequiredCard
import com.a3.yearlyprogess.feature.events.ui.components.EventDetailCard
import com.a3.yearlyprogess.feature.events.ui.components.EventDetailCardDefaults
import com.a3.yearlyprogess.feature.events.ui.components.EventList

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ImportEventsScreen(
    navController: NavHostController,
    importEventsViewModel: ImportEventsViewModel = hiltViewModel()
) {
    val importTopbarScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    var lastClickTime by remember { mutableLongStateOf(0L) }

    val isAllSelected = importEventsViewModel.isAllSelected.collectAsState()


    val events by importEventsViewModel.events.collectAsState()
    val settings by importEventsViewModel.settings.collectAsState()
    val selectedIds by importEventsViewModel.selectedIds.collectAsState()
    val uiState by importEventsViewModel.uiState.collectAsState()
    val shouldShowPermissionDialog by importEventsViewModel.shouldShowPermissionDialog.collectAsState()
    val showDateFilter by importEventsViewModel.showDateFilter.collectAsState()
    val dateFilter by importEventsViewModel.dateFilter.collectAsState()
    val availableCalendars by importEventsViewModel.availableCalendars.collectAsState()
    val selectedCalendars by importEventsViewModel.selectedCalendars.collectAsState()

    var isCalendarDialogOpen by remember { mutableStateOf(false) }

    Log.d("selectedCalendars", selectedCalendars.toString())



    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            importEventsViewModel.onPermissionGranted()
        } else {
            importEventsViewModel.onPermissionDenied()
        }
    }


    BackHandler(enabled = selectedIds.isNotEmpty()) {
        importEventsViewModel.clearSelection()
    }

    val fabContent: @Composable () -> Unit = {
        AnimatedVisibility(
            visible = events.isNotEmpty() && selectedIds.isNotEmpty(),
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = tween(250)
            ) + fadeIn(tween(250)),
            exit = slideOutVertically(
                targetOffsetY = { it },
                animationSpec = tween(200)
            ) + fadeOut(tween(200))
        ) {
            FloatingActionButton(
                onClick = {
                    val now = System.currentTimeMillis()
                    if (now - lastClickTime > 600) { // debounce 600ms
                        lastClickTime = now
                        importEventsViewModel.importSelectedEvents(
                            onFinish = {
                                navController.navigateUp()
                            }
                        )
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = stringResource(R.string.import_events)
                )
            }
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(importTopbarScrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.import_events)
                    )
                },
                navigationIcon = {

                        IconButton(onClick = {
                            navController.navigateUp()
                        }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                },
                actions = {

                    IconButton(onClick = {
                        isCalendarDialogOpen = true
                    }) {
                        Icon(Icons.Outlined.EditCalendar, contentDescription = "Choose Calendars")
                    }

                    IconButton(onClick = {
                        importEventsViewModel.showDateFilter(true)
                    }) {
                        Icon(Icons.Outlined.FilterList, contentDescription = "Filter")
                    }

                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn() + scaleIn(),
                        exit = fadeOut() + scaleOut()
                    ) {
                        IconButton(onClick = {
                            importEventsViewModel.toggleAllSelections()
                        }) {
                            Icon(
                                if (isAllSelected.value)
                                    Icons.Outlined.Deselect
                                else
                                    Icons.Outlined.SelectAll, contentDescription = "Toggle selection"
                            )
                        }
                    }

                }, scrollBehavior = importTopbarScrollBehavior,
            )
        },
        contentWindowInsets = WindowInsets.safeDrawing,
        floatingActionButton = fabContent
    ) { innerPadding ->

        DateRangePickerModal(
            open = showDateFilter,
            onDateRangeSelected = {
                importEventsViewModel.clearSelection()
                importEventsViewModel.setDateFilter(it)
                importEventsViewModel.showDateFilter(false)
            },
            onDismiss = {
                importEventsViewModel.showDateFilter(false)
            },
            initialStartDateMillis = dateFilter.first,
            initialEndDateMillis = dateFilter.second
        )

        SelectDialog(
            title = "Your Calendars",
            items = availableCalendars,
            selectedItems = selectedCalendars.toSet(),
            onDismiss = {
                isCalendarDialogOpen = false
            },
            renderItem = { it, _ ->
                Text(it.displayName)
            },
            isOpen = isCalendarDialogOpen,
            onConfirm = { selectedCalendars ->
                importEventsViewModel.setSelectedCalendars(selectedCalendars)
                isCalendarDialogOpen = false
            }
        )

        when (uiState) {
            is CalendarUiState.Initial -> {
                // Initial state - permission check in progress
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    LoadingIndicator()
                }
            }

            is CalendarUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    LoadingIndicator()
                }
            }

            is CalendarUiState.PermissionRequired -> {
                LazyColumn(
                    modifier = Modifier
                        .padding(8.dp, 0.dp)
                        .fillMaxSize(),
                    contentPadding = innerPadding,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    item {
                        CalendarRequiredCard(
                            onGoToSettings = { importEventsViewModel.onGoToSettings() },
                            cornerStyle = CardCornerStyle.Default
                        )
                    }
                }

            }

            is CalendarUiState.Success -> {
                EventList(
                    events = events,
                    selectedIds = selectedIds,
                    showPinOption = false,
                    emptyText = "No events found",
                    contentPadding = innerPadding,
                    onItemClick = { importEventsViewModel.toggleSelection(it.id) },
                    onItemLongPress = { importEventsViewModel.toggleSelection(it.id) },
                    settings = settings
                )
            }

            is CalendarUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Error: ${(uiState as CalendarUiState.Error).message}")
                }
            }
        }

        // Show permission dialog
        if (shouldShowPermissionDialog) {
            CalendarPermissionDialog(
                onDismiss = {
                    importEventsViewModel.onPermissionDenied()
                },
                onConfirm = {
                    permissionLauncher.launch(Manifest.permission.READ_CALENDAR)
                }
            )
        }
    }
}
