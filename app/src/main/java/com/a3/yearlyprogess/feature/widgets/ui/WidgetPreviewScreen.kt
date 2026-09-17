package com.a3.yearlyprogess.feature.widgets.ui

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.RemoteViews
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.a3.yearlyprogess.R
import com.a3.yearlyprogess.app.MainViewModel
import com.a3.yearlyprogess.core.ui.components.ad.AdCard
import com.a3.yearlyprogess.core.util.YearlyProgressUtil
import com.a3.yearlyprogess.feature.home.HomeUiState
import com.a3.yearlyprogess.feature.home.HomeViewModel
import com.a3.yearlyprogess.feature.widgets.domain.model.StandaloneWidgetOptions
import com.a3.yearlyprogess.feature.widgets.domain.model.WidgetTheme
import com.a3.yearlyprogess.feature.widgets.ui.config_screens.AllInWidgetConfigViewModel
import com.a3.yearlyprogess.feature.widgets.ui.config_screens.StandaloneWidgetConfigViewModel
import com.a3.yearlyprogess.feature.widgets.ui.config_screens.createRemoteViews
import com.a3.yearlyprogess.feature.widgets.update.PinStandaloneWidgetReceiver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

@Immutable
data class WidgetPreviewItem(
    @param:StringRes val titleRes: Int,
    val type: StandaloneWidgetType,
    val componentClass: Class<*>
)

private data class WidgetTypeGroup(
    @param:StringRes val titleRes: Int,
    val type: StandaloneWidgetType,
    val componentClass: Class<*>
)

private val WIDGET_TYPE_GROUPS = listOf(
    WidgetTypeGroup(R.string.day, StandaloneWidgetType.DAY, DayWidget::class.java),
    WidgetTypeGroup(R.string.week, StandaloneWidgetType.WEEK, WeekWidget::class.java),
    WidgetTypeGroup(R.string.month, StandaloneWidgetType.MONTH, MonthWidget::class.java),
    WidgetTypeGroup(R.string.year, StandaloneWidgetType.YEAR, YearWidget::class.java),
    WidgetTypeGroup(R.string.day_light, StandaloneWidgetType.DAY_LIGHT, DayLightWidget::class.java),
    WidgetTypeGroup(R.string.night_light, StandaloneWidgetType.NIGHT_LIGHT, NightLightWidget::class.java)
)

private val ALL_SHAPES = StandaloneWidgetOptions.Companion.WidgetShape.entries

@Composable
fun WidgetPreviewScreen(
    mainViewModel: MainViewModel,
    homeViewModel: HomeViewModel,
    configViewModel: StandaloneWidgetConfigViewModel = hiltViewModel(),
    allInOneWidgetConfigViewModel: AllInWidgetConfigViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    // Use application context for background threads to avoid leaking Activity context
    val appContext = context.applicationContext

    val yp = remember { YearlyProgressUtil() }
    val appSettings by mainViewModel.appSettings.collectAsState()
    val homeUiState by homeViewModel.uiState.collectAsState()
    val userWidgetDefaultOptions by configViewModel.options.collectAsState()
    val allInOneWidgetDefaultOptions by allInOneWidgetConfigViewModel.options.collectAsState()
    val sunsetData = (homeUiState as? HomeUiState.Success)?.data
    val appTheme = appSettings?.appTheme
        ?: if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) WidgetTheme.DYNAMIC else WidgetTheme.DEFAULT

    Column(modifier = Modifier.fillMaxSize()) {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 160.dp),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            WIDGET_TYPE_GROUPS.forEach { group ->
                // Section header for each widget type
                item(
                    key = "header_${group.type.name}",
                    span = { GridItemSpan(maxLineSpan) }
                ) {
                    Text(
                        text = stringResource(group.titleRes),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                    )
                }

                // All shapes for this widget type
                items(
                    items = ALL_SHAPES.toList(),
                    key = { shape -> "${group.type.name}_${shape.name}" }
                ) { shape ->
                    val options = userWidgetDefaultOptions.copy(
                        widgetType = group.type,
                        widgetShape = shape,
                        theme = appTheme
                    )
                    AsyncWidgetPreviewLoader(
                        updateInterval = 1000L,
                        producer = {
                            createRemoteViews(appContext, yp, options, sunsetData)
                        }
                    ) { remoteViews ->
                        WidgetPreviewCard(remoteViews) {
                            pinWidget(context, group.componentClass, remoteViews, shape)
                        }
                    }
                }
            }

            item(key = "all_in_one", span = { GridItemSpan(maxLineSpan) }) {
                AsyncWidgetPreviewLoader(
                    updateInterval = 1000L,
                    producer = {
                        val bundleOptions = Bundle().apply {
                            putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 320)
                            putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 220)
                        }
                        AllInWidget.createAllInOneWidgetRemoteView(
                            appContext,
                            yp,
                            allInOneWidgetDefaultOptions.copy(theme = appTheme),
                            bundleOptions,
                            isWidgetPreview = true
                        )
                    }
                ) { remoteViews ->
                    WidgetPreviewCard(remoteViews) {
                        pinWidget(context, AllInWidget::class.java, remoteViews)
                    }
                }
            }

            item(key = "native_ad_card", span = { GridItemSpan(maxLineSpan) }) {
                AdCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )
            }
        }
    }
}

/**
 * A helper that generates the RemoteViews on a background thread
 * and updates the UI state only when ready.
 */
@Composable
fun AsyncWidgetPreviewLoader(
    updateInterval: Long,
    producer: suspend () -> RemoteViews,
    content: @Composable (RemoteViews) -> Unit
) {
    // produceState launches a coroutine scoped to this Composable
    val remoteViews by produceState<RemoteViews?>(initialValue = null, key1 = producer) {
        while (true) {
            // Generate on Background Thread
            val newView = withContext(Dispatchers.Default) {
                producer()
            }
            // Update State (triggers recomposition of content only)
            value = newView

            // Wait for next tick
            delay(updateInterval)
        }
    }

    // Only render content if we have a view
    remoteViews?.let {
        content(it)
    } ?: run {
        // Optional: Add a placeholder/loading box here if needed to prevent layout jumps on first load
        Box(modifier = Modifier.fillMaxWidth().height(105.dp))
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun WidgetPreviewCard(
    remoteViews: RemoteViews,
    onAddClicked: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(105.dp)
                .padding(bottom = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            RemoteViewsHost(remoteViews)
        }

        Button(
            onClick = onAddClicked,
            modifier = Modifier.fillMaxWidth(),
            shapes = ButtonDefaults.shapes(
                pressedShape = RoundedCornerShape(24.dp),
                shape = RoundedCornerShape(12.dp)
            )
        ) {
            Text(
                stringResource(R.string.add),
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

@Composable
fun RemoteViewsHost(remoteViews: RemoteViews) {
    AndroidView(
        modifier = Modifier.fillMaxWidth(),
        factory = { context ->
            FrameLayout(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                try {
                    val view = remoteViews.apply(context, this)
                    addView(view)
                } catch (e: Exception) {
                    // Handle inflation errors
                }
            }
        },
        update = { host ->
            host.getChildAt(0)?.let { view ->
                try {
                    // This reapply is lighter than apply, but still runs on Main.
                    // Since we offloaded the creation, this should be smooth enough.
                    remoteViews.reapply(host.context, view)
                } catch (e: Exception) {
                    host.removeAllViews()
                    try {
                        val view = remoteViews.apply(host.context, host)
                        host.addView(view)
                    } catch (inner: Exception) {
                    }
                }
            }
        }
    )
}

/**
 * Pins a widget to the home screen. If [shape] is provided, a [PendingIntent] is created
 * so that [PinStandaloneWidgetReceiver] saves the shape for the newly pinned widget ID.
 */
private fun pinWidget(
    context: Context,
    widgetClass: Class<*>,
    remoteViews: RemoteViews? = null,
    shape: StandaloneWidgetOptions.Companion.WidgetShape? = null
) {
    try {
        val appWidgetManager = context.getSystemService(AppWidgetManager::class.java)
        val myProvider = ComponentName(context, widgetClass)

        var bundle: Bundle? = null
        if (remoteViews != null) {
            bundle = Bundle()
            bundle.putParcelable(AppWidgetManager.EXTRA_APPWIDGET_PREVIEW, remoteViews)
        }

        val successCallback = if (shape != null) {
            Intent(context, PinStandaloneWidgetReceiver::class.java).apply {
                putExtra(PinStandaloneWidgetReceiver.EXTRA_WIDGET_SHAPE, shape.name)
            }.let { intent ->
                PendingIntent.getBroadcast(
                    context,
                    shape.ordinal,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
                )
            }
        } else {
            null
        }

        if (appWidgetManager != null && appWidgetManager.isRequestPinAppWidgetSupported) {
            appWidgetManager.requestPinAppWidget(myProvider, bundle, successCallback)
        }
    } catch (ex: Exception) {
        Toast.makeText(
            context,
            "Failed to pin the widget!",
            Toast.LENGTH_SHORT
        ).show()
    }
}