package com.a3.yearlyprogess.feature.widgets.ui.config_screens

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.RemoteViews
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.a3.yearlyprogess.R
import com.a3.yearlyprogess.core.ui.theme.YearlyProgressTheme
import com.a3.yearlyprogess.core.util.YearlyProgressUtil
import com.a3.yearlyprogess.domain.model.SunriseSunset
import com.a3.yearlyprogess.feature.home.HomeUiState
import com.a3.yearlyprogess.feature.home.HomeViewModel
import com.a3.yearlyprogess.feature.widgets.domain.model.StandaloneWidgetOptions
import com.a3.yearlyprogess.feature.widgets.domain.model.StandaloneWidgetOptions.Companion.WidgetShape
import com.a3.yearlyprogess.feature.widgets.ui.StandaloneWidget.Companion.cloverRemoteView
import com.a3.yearlyprogess.feature.widgets.ui.StandaloneWidget.Companion.pillRemoteView
import com.a3.yearlyprogess.feature.widgets.ui.StandaloneWidget.Companion.rectangularRemoteView
import com.a3.yearlyprogess.feature.widgets.ui.StandaloneWidgetType
import com.a3.yearlyprogess.feature.widgets.ui.components.WidgetShapeSelector
import com.a3.yearlyprogess.feature.widgets.ui.components.SharedWidgetSettings
import com.a3.yearlyprogess.feature.widgets.update.WidgetUpdateBroadcastReceiver
import com.a3.yearlyprogess.feature.widgets.util.WidgetRenderer
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class StandaloneConfigActivity : ComponentActivity() {

    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

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
            YearlyProgressTheme {
                StandaloneWidgetConfigScreen(
                    appWidgetId = appWidgetId,
                    onSaveSuccess = {
                        // On successful save, pass back the original ID and finish
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
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StandaloneWidgetConfigScreen(
    modifier: Modifier = Modifier,
    appWidgetId: Int,
    onSaveSuccess: () -> Unit,
    configViewModel: StandaloneWidgetConfigViewModel = hiltViewModel(),
) {
    // Load options for this specific ID when the screen launches
    LaunchedEffect(appWidgetId) {
        configViewModel.setWidgetId(appWidgetId)
    }

    // Listen for the Save Success event
    LaunchedEffect(true) {
        configViewModel.uiEvent.collect { event ->
            when (event) {
                is StandaloneWidgetConfigViewModel.UiEvent.SaveSuccess -> {
                    onSaveSuccess()
                }
            }
        }
    }

    val options by configViewModel.options.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        when (options.widgetType) {
                            StandaloneWidgetType.DAY -> stringResource(R.string.day)
                            StandaloneWidgetType.WEEK -> stringResource(R.string.week)
                            StandaloneWidgetType.MONTH -> stringResource(R.string.month)
                            StandaloneWidgetType.YEAR -> stringResource(R.string.year)
                            StandaloneWidgetType.DAY_LIGHT -> stringResource(R.string.day_light)
                            StandaloneWidgetType.NIGHT_LIGHT -> stringResource(R.string.night_light)
                            else -> "Standalone Widget Options"
                        }
                    )
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    configViewModel.saveOptions()
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Save, contentDescription = "Save")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Spacer(Modifier.height(8.dp))

            // Widget Preview at the top
            WidgetPreview(
                options = options,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(Modifier.height(16.dp))

            StandaloneSettingsContent(
                options = options,
                viewModel = configViewModel
            )

            // Add some padding at the bottom so the FAB doesn't cover the last slider
            Spacer(Modifier.height(80.dp))
        }
    }
}

@Composable
fun WidgetPreview(
    options: StandaloneWidgetOptions,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val yp = remember { YearlyProgressUtil() }
    val state by viewModel.uiState.collectAsState()
    val sunsetData = if (state is HomeUiState.Success) {(state as HomeUiState.Success).data } else null

        Column(
            modifier = modifier
                .fillMaxWidth()
                .background(
                    color = MaterialTheme.colorScheme.surfaceContainerLowest,
                    shape = RoundedCornerShape(16.dp)
                )
                .clip(shape = RoundedCornerShape(16.dp))
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Widget Preview",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Widget preview container
            Box(
                modifier = Modifier
                    .width(200.dp)
                    .height(120.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                // Use the shape as a key to force recomposition when shape changes
                key(options.widgetShape, options.widgetType, sunsetData) {
                    AndroidView(
                        factory = { ctx ->
                            createRemoteViews(ctx, yp, options, sunsetData).apply(ctx, null)
                        },
                        update = { view ->
                            createRemoteViews(context, yp, options, sunsetData).reapply(
                                context,
                                view
                            )
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
}

/**
 * Helper function to create RemoteViews based on widget options.
 * This avoids duplicating logic in the AndroidView's factory and update blocks.
 */
private fun createRemoteViews(
    context: Context,
    yp: YearlyProgressUtil, userConfig: StandaloneWidgetOptions, sunsetData: List<SunriseSunset>?
): RemoteViews {
    val widgetType = userConfig.widgetType
    return when (userConfig.widgetShape) {
        WidgetShape.RECTANGULAR -> when (widgetType) {
            StandaloneWidgetType.DAY_LIGHT -> rectangularRemoteView(
                context,
                yp,
                userConfig,
                sunsetData,
            )

            StandaloneWidgetType.NIGHT_LIGHT -> rectangularRemoteView(
                context,
                yp,
                userConfig,
                sunsetData,
            )
            null -> WidgetRenderer.errorWidgetRemoteView(context, "Error failed to load preview")
            else -> rectangularRemoteView(context, yp, userConfig)
        }

        WidgetShape.CLOVER -> {
            val bundleOptions = Bundle().apply {
                putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 203)
                putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 220)
            }
            when (widgetType) {
                StandaloneWidgetType.DAY_LIGHT -> cloverRemoteView(
                    context,
                    yp,
                    userConfig,
                    sunsetData,
                    bundleOptions
                )

                StandaloneWidgetType.NIGHT_LIGHT -> cloverRemoteView(
                    context,
                    yp,
                    userConfig,
                    sunsetData,
                    bundleOptions
                )
                null -> WidgetRenderer.errorWidgetRemoteView(context, "Error failed to load preview")
                else -> cloverRemoteView(context, yp, userConfig, bundleOptions)
            }

        }

        WidgetShape.PILL -> {
            val bundleOptions = Bundle().apply {
                putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 160)
                putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 160)
            }
            when (widgetType) {
                StandaloneWidgetType.DAY_LIGHT -> pillRemoteView(
                    context,
                    yp,
                    userConfig,
                    sunsetData,
                    bundleOptions
                )

                StandaloneWidgetType.NIGHT_LIGHT -> pillRemoteView(
                    context,
                    yp,
                    userConfig,
                    sunsetData,
                    bundleOptions
                )
                null -> WidgetRenderer.errorWidgetRemoteView(context, "Error failed to load preview")
                else -> pillRemoteView(context, yp, userConfig, bundleOptions)
            }
        }
    }

}

@Composable
fun StandaloneSettingsContent(
    options: StandaloneWidgetOptions,
    viewModel: StandaloneWidgetConfigViewModel,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // Widget Shape Selector (specific to Standalone widget)
        WidgetShapeSelector(
            selectedShape = options.widgetShape,
            onShapeSelected = { shape ->
                viewModel.updateWidgetShape(shape)
            },
            Modifier.padding(horizontal = 16.dp)
        )

        Spacer(Modifier.height(8.dp))

        SharedWidgetSettings(
            theme = options.theme,
            timeStatusCounter = options.timeLeftCounter,
            dynamicTimeStatusCounter = options.dynamicLeftCounter,
            replaceProgressWithTimeLeft = options.replaceProgressWithDaysLeft,
            decimalDigits = options.decimalPlaces,
            backgroundTransparency = options.backgroundTransparency,
            fontScale = options.fontScale,
            onThemeChange = viewModel::updateTheme,
            onTimeStatusCounterChange = viewModel::updateTimeLeftCounter,
            onDynamicTimeStatusCounterChange = viewModel::updateDynamicLeftCounter,
            onReplaceProgressChange = viewModel::updateReplaceProgressWithDaysLeft,
            onDecimalDigitsChange = viewModel::updateDecimalPlaces,
            onBackgroundTransparencyChange = viewModel::updateBackgroundTransparency,
            onFontScaleChange = viewModel::updateFontScale,
           )
    }
}