package com.a3.yearlyprogess.feature.widgets.ui.config_screens

import android.Manifest
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.a3.yearlyprogess.R
import com.a3.yearlyprogess.app.MainViewModel
import com.a3.yearlyprogess.core.ui.theme.YearlyProgressTheme
import com.a3.yearlyprogess.feature.events.presentation.CalendarUiState
import com.a3.yearlyprogess.feature.events.presentation.ImportEventsViewModel
import com.a3.yearlyprogess.feature.events.ui.components.CalendarPermissionDialog
import com.a3.yearlyprogess.feature.events.ui.components.CalendarRequiredCard
import com.a3.yearlyprogess.feature.widgets.domain.model.WidgetTheme
import com.a3.yearlyprogess.feature.widgets.ui.components.CalendarSelectionList
import com.a3.yearlyprogess.feature.widgets.ui.components.SharedWidgetSettings
import com.a3.yearlyprogess.feature.widgets.update.WidgetUpdateBroadcastReceiver
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CalendarConfigActivity : ComponentActivity() {

    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    // Permission launcher
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Permission granted - the ViewModel will handle loading calendars
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        enableEdgeToEdge()

        // 1. Set the result to CANCELED. This way if the user backs out, the widget is not created.
        setResult(RESULT_CANCELED)

        // 2. Find the widget ID from the intent.
        val intent = intent
        val extras = intent.extras
        if (extras != null) {
            appWidgetId = extras.getInt(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID
            )
        }

        // 3. If the intent doesn't have a widget ID, finish.
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        setContent {
            val mainViewModel: MainViewModel = hiltViewModel()
            val appSettings by mainViewModel.appSettings.collectAsState()

            YearlyProgressTheme(
                appTheme = appSettings?.appTheme ?: WidgetTheme.DEFAULT
            ) {
                CalendarWidgetConfigScreen(
                    appWidgetId = appWidgetId,
                    onSaveSuccess = {
                        // 4. On successful save, pass back the original ID and finish
                        val resultValue = Intent().putExtra(
                            AppWidgetManager.EXTRA_APPWIDGET_ID,
                            appWidgetId
                        )
                        // Tell the Widget to update
                        sendBroadcast(
                            Intent(this, WidgetUpdateBroadcastReceiver::class.java)
                        )
                        setResult(RESULT_OK, resultValue)
                        finish()
                    },
                    onRequestPermission = {
                        permissionLauncher.launch(Manifest.permission.READ_CALENDAR)
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun CalendarWidgetConfigScreen(
    modifier: Modifier = Modifier,
    appWidgetId: Int,
    onSaveSuccess: () -> Unit,
    onRequestPermission: () -> Unit,
    configViewModel: CalendarWidgetConfigViewModel = hiltViewModel(),
    importEventsViewModel: ImportEventsViewModel = hiltViewModel(),
) {
    // Load options for this specific ID when the screen launches
    LaunchedEffect(appWidgetId) {
        configViewModel.setWidgetId(appWidgetId)
        importEventsViewModel.checkCalendarPermission()
    }

    // Listen for the Save Success event
    LaunchedEffect(true) {
        configViewModel.uiEvent.collect { event ->
            when (event) {
                is CalendarWidgetConfigViewModel.UiEvent.SaveSuccess -> {
                    onSaveSuccess()
                }
            }
        }
    }

    val options by configViewModel.options.collectAsState()
    val pagerState = rememberPagerState(pageCount = { 2 })
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.calendar_widget)) },
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    configViewModel.saveOptions()
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ) {
                Icon(Icons.Default.Save, contentDescription = "Save")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tab Row with animation
            PrimaryTabRow(
                selectedTabIndex = pagerState.currentPage,
                indicator = {
                    TabRowDefaults.PrimaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(
                            pagerState.currentPage,
                        ),
                        shape = RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = 0.dp,
                            bottomEnd = 0.dp
                        ),
                        width = 48.dp
                    )
                }
            ) {
                Tab(
                    selected = pagerState.currentPage == 0,
                    onClick = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(0)
                        }
                    },
                    text = { Text(stringResource(R.string.customization)) }
                )
                Tab(
                    selected = pagerState.currentPage == 1,
                    onClick = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(1)
                        }
                    },
                    text = { Text(stringResource(R.string.calendars)) }
                )
            }

            // HorizontalPager for swipeable content
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                when (page) {
                    0 -> CalendarSettingsTab(
                        options = options,
                        viewModel = configViewModel
                    )
                    1 -> CalendarSelectionTab(
                        importEventsViewModel = importEventsViewModel,
                        configViewModel = configViewModel,
                        onRequestPermission = onRequestPermission
                    )
                }
            }
        }
    }
}

@Composable
fun CalendarSettingsTab(
    options: com.a3.yearlyprogess.feature.widgets.domain.model.CalendarWidgetOptions,
    viewModel: CalendarWidgetConfigViewModel,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Spacer(Modifier.height(8.dp))

        SharedWidgetSettings(
            theme = options.theme ?: WidgetTheme.DEFAULT,
            timeStatusCounter = options.timeStatusCounter,
            dynamicTimeStatusCounter = options.dynamicTimeStatusCounter,
            replaceProgressWithTimeLeft = options.replaceProgressWithTimeLeft,
            decimalDigits = options.decimalDigits,
            backgroundTransparency = options.backgroundTransparency,
            fontScale = options.fontScale,
            onThemeChange = viewModel::updateTheme,
            onTimeStatusCounterChange = viewModel::updateTimeStatusCounter,
            onDynamicTimeStatusCounterChange = viewModel::updateDynamicTimeStatusCounter,
            onReplaceProgressChange = viewModel::updateReplaceProgressWithTimeLeft,
            onDecimalDigitsChange = viewModel::updateDecimalDigits,
            onBackgroundTransparencyChange = viewModel::updateBackgroundTransparency,
            onFontScaleChange = viewModel::updateFontScale
        )

        // Add some padding at the bottom so the FAB doesn't cover the last slider
        Spacer(Modifier.height(80.dp))
    }
}

@Composable
fun CalendarSelectionTab(
    importEventsViewModel: ImportEventsViewModel,
    configViewModel: CalendarWidgetConfigViewModel,
    onRequestPermission: () -> Unit
) {
    val uiState by importEventsViewModel.uiState.collectAsState()
    val shouldShowPermissionDialog by importEventsViewModel.shouldShowPermissionDialog.collectAsState()
    val availableCalendars by importEventsViewModel.availableCalendars.collectAsState()
    val options by configViewModel.options.collectAsState()


    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            importEventsViewModel.onPermissionGranted()
        } else {
            importEventsViewModel.onPermissionDenied()
        }
    }


    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        when (uiState) {
            is CalendarUiState.Initial,
            is CalendarUiState.Loading -> {
                // Loading state handled by ViewModel
            }
            is CalendarUiState.PermissionRequired -> {
                CalendarRequiredCard(
                    onGoToSettings = {
                        importEventsViewModel.onGoToSettings()
                    }
                )
            }
            is CalendarUiState.Success -> {
                CalendarSelectionList(
                    calendars = availableCalendars,
                    selectedCalendarIds = options.selectedCalendarIds,
                    onCalendarToggle = { calendarId ->
                        configViewModel.toggleCalendarSelection(calendarId)
                    }
                )
            }
            is CalendarUiState.Error -> {
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