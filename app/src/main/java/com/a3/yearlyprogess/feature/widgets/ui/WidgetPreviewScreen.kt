package com.a3.yearlyprogess.feature.widgets.ui

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.RemoteViews
import androidx.compose.foundation.background
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.a3.yearlyprogess.R
import com.a3.yearlyprogess.core.ui.components.ad.AdCard
import com.a3.yearlyprogess.core.ui.components.ad.AdCardDefaults
import com.a3.yearlyprogess.core.ui.style.CardCornerStyle
import com.a3.yearlyprogess.core.util.YearlyProgressUtil
import com.a3.yearlyprogess.feature.home.HomeUiState
import com.a3.yearlyprogess.feature.home.HomeViewModel
import com.a3.yearlyprogess.feature.widgets.ui.config_screens.StandaloneWidgetConfigViewModel
import kotlinx.coroutines.delay

data class WidgetPreviewItem(
    val title: String,
    val type: StandaloneWidgetType,
    val componentClass: Class<*>
)

@Composable
fun WidgetPreviewScreen(
    homeViewModel: HomeViewModel,
    configViewModel: StandaloneWidgetConfigViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val yp = remember { YearlyProgressUtil() }
    val homeUiState by homeViewModel.uiState.collectAsState()
    val userWidgetDefaultOptions by configViewModel.options.collectAsState()
    val sunsetData = (homeUiState as? HomeUiState.Success)?.data

    // Timer state to trigger updates every 500ms
    var tick by remember { mutableStateOf(0L) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(500)
            tick = System.currentTimeMillis()
        }
    }

    val widgetItems = remember {
        listOf(
            WidgetPreviewItem("Day", StandaloneWidgetType.DAY, DayWidget::class.java),
            WidgetPreviewItem("Week", StandaloneWidgetType.WEEK, WeekWidget::class.java),
            WidgetPreviewItem("Month", StandaloneWidgetType.MONTH, MonthWidget::class.java),
            WidgetPreviewItem("Year", StandaloneWidgetType.YEAR, YearWidget::class.java),
            WidgetPreviewItem(
                "Day Light",
                StandaloneWidgetType.DAY_LIGHT,
                DayLightWidget::class.java
            ),
            WidgetPreviewItem(
                "Night Light",
                StandaloneWidgetType.NIGHT_LIGHT,
                NightLightWidget::class.java
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 160.dp),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(widgetItems) { item ->
                val remoteViews = remember(item, tick, sunsetData) {
                    val options = userWidgetDefaultOptions.copy(
                        widgetType = item.type,
                    )
                    if (item.type == StandaloneWidgetType.DAY_LIGHT || item.type == StandaloneWidgetType.NIGHT_LIGHT) {
                        StandaloneWidget.rectangularRemoteView(
                            context,
                            yp,
                            options,
                            sunsetData,
                            false
                        )
                    } else {
                        StandaloneWidget.rectangularRemoteView(context, yp, options, false)
                    }
                }
                WidgetPreviewCard(item, remoteViews) {
                    pinWidget(context, item.componentClass)
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

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun WidgetPreviewCard(
    item: WidgetPreviewItem,
    remoteViews: RemoteViews,
    onAddClicked: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Container for RemoteViews
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
                }
            }
        },
        update = { host ->
            host.getChildAt(0)?.let { view ->
                try {
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

private fun pinWidget(context: Context, widgetClass: Class<*>) {
    val appWidgetManager = context.getSystemService(AppWidgetManager::class.java)
    val myProvider = ComponentName(context, widgetClass)

    if (appWidgetManager != null && appWidgetManager.isRequestPinAppWidgetSupported) {
        appWidgetManager.requestPinAppWidget(myProvider, null, null)
    }
}
