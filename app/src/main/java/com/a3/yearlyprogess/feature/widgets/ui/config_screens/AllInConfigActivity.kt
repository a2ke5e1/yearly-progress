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
import androidx.compose.foundation.layout.requiredWidth
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.a3.yearlyprogess.R
import com.a3.yearlyprogess.app.MainViewModel
import com.a3.yearlyprogess.core.ui.components.Switch
import com.a3.yearlyprogess.core.ui.theme.YearlyProgressTheme
import com.a3.yearlyprogess.core.util.YearlyProgressUtil
import com.a3.yearlyprogess.feature.widgets.domain.model.AllInWidgetOptions
import com.a3.yearlyprogess.feature.widgets.domain.model.WidgetTheme
import com.a3.yearlyprogess.feature.widgets.ui.AllInWidget.Companion.createAllInOneWidgetRemoteView
import com.a3.yearlyprogess.feature.widgets.ui.components.SharedWidgetSettings
import com.a3.yearlyprogess.feature.widgets.update.WidgetUpdateBroadcastReceiver
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AllInConfigActivity : ComponentActivity() {

    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        enableEdgeToEdge()

        setResult(RESULT_CANCELED)

        val intent = intent
        val extras = intent.extras
        if (extras != null) {
            appWidgetId = extras.getInt(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID
            )
        }

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
                AllInWidgetConfigScreen(
                    appWidgetId = appWidgetId,
                    onSaveSuccess = {
                        val resultValue = Intent().putExtra(
                            AppWidgetManager.EXTRA_APPWIDGET_ID,
                            appWidgetId
                        )
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
fun AllInWidgetConfigScreen(
    modifier: Modifier = Modifier,
    appWidgetId: Int,
    onSaveSuccess: () -> Unit,
    configViewModel: AllInWidgetConfigViewModel = hiltViewModel(),
) {
    LaunchedEffect(appWidgetId) {
        configViewModel.setWidgetId(appWidgetId)
    }

    LaunchedEffect(true) {
        configViewModel.uiEvent.collect { event ->
            when (event) {
                is AllInWidgetConfigViewModel.UiEvent.SaveSuccess -> {
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
                    Text(stringResource(R.string.all_in_one_widget))
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

            WidgetPreview(
                options = options,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(Modifier.height(16.dp))

            AllInSettingsContent(
                options = options,
                viewModel = configViewModel
            )

            Spacer(Modifier.height(80.dp))
        }
    }
}

@Composable
fun WidgetPreview(
    options: AllInWidgetOptions,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val yp = remember { YearlyProgressUtil() }

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
            text = stringResource(R.string.widget_preview),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Using requiredWidth and scaling down more aggressively to prevent clipping on all devices.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp),
            contentAlignment = Alignment.Center
        ) {
            key(options) {
                AndroidView(
                    factory = { ctx ->
                        createRemoteViews(ctx, yp, options).apply(ctx, null)
                    },
                    update = { view ->
                        createRemoteViews(context, yp, options).reapply(
                            context,
                            view
                        )
                    },
                    modifier = Modifier
                        .requiredWidth(450.dp)
                        .height(120.dp)
                        .graphicsLayer(
                            scaleX = 0.8f,
                            scaleY = 0.8f,
                            clip = false
                        )
                )
            }
        }
    }
}

private fun createRemoteViews(
    context: Context,
    yp: YearlyProgressUtil,
    userConfig: AllInWidgetOptions
): RemoteViews {
    val bundleOptions = Bundle().apply {
        putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 320)
        putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 220)
    }
    return createAllInOneWidgetRemoteView(context, yp, userConfig, bundleOptions, isWidgetPreview = true)
}

@Composable
fun AllInSettingsContent(
    options: AllInWidgetOptions,
    viewModel: AllInWidgetConfigViewModel,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // Visible Items Section
        Text(
            text = "Show",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        // Toggle switches for each time period
        Switch(
            title = stringResource(R.string.day),
            description = "Display day progress in widget",
            checked = options.showDay,
            onCheckedChange = { enabled ->
                viewModel.updateShowDay(enabled)
            }
        )

        Switch(
            title = stringResource(R.string.week),
            description = "Display week progress in widget",
            checked = options.showWeek,
            onCheckedChange = { enabled ->
                viewModel.updateShowWeek(enabled)
            }
        )

        Switch(
            title = stringResource(R.string.month),
            description = "Display month progress in widget",
            checked = options.showMonth,
            onCheckedChange = { enabled ->
                viewModel.updateShowMonth(enabled)
            }
        )

        Switch(
            title = stringResource(R.string.year),
            description = "Display year progress in widget",
            checked = options.showYear,
            onCheckedChange = { enabled ->
                viewModel.updateShowYear(enabled)
            }
        )

        Spacer(Modifier.height(8.dp))

        // Shared widget settings
        SharedWidgetSettings(
            theme = options.theme ?: WidgetTheme.DEFAULT,
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
            showTheme = true,
            showDecimalDigits = true,
            showTimeStatusCounter = false,
            showBackgroundTransparency = false,
            showFontScale = false
        )
    }
}
