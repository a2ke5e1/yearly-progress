package com.a3.yearlyprogess.feature.widgets.update

import android.appwidget.AppWidgetManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.a3.yearlyprogess.feature.widgets.domain.model.StandaloneWidgetOptions.Companion.WidgetShape
import com.a3.yearlyprogess.feature.widgets.domain.repository.StandaloneWidgetOptionsRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class PinStandaloneWidgetReceiver : BroadcastReceiver() {

    @Inject
    lateinit var standaloneWidgetOptionsRepository: StandaloneWidgetOptionsRepository
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        val appWidgetId = intent.getIntExtra(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        )
        val shapeName = intent.getStringExtra(EXTRA_WIDGET_SHAPE)

        if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID && shapeName != null) {
            val shape = runCatching { WidgetShape.valueOf(shapeName) }.getOrNull() ?: return
            scope.launch {
                standaloneWidgetOptionsRepository.updateWidgetShape(appWidgetId, shape)
                val updateIntent = Intent(context, WidgetUpdateBroadcastReceiver::class.java)
                context.sendBroadcast(updateIntent)
            }
        }
    }

    companion object {
        const val EXTRA_WIDGET_SHAPE = "extra_widget_shape"
    }
}
