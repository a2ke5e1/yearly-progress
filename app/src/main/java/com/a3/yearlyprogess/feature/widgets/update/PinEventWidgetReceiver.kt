package com.a3.yearlyprogess.feature.widgets.update

import android.appwidget.AppWidgetManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.a3.yearlyprogess.feature.widgets.domain.repository.EventWidgetOptionsRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class PinEventWidgetReceiver : BroadcastReceiver() {

    @Inject
    lateinit var eventWidgetOptionsRepository: EventWidgetOptionsRepository

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        val appWidgetId = intent.getIntExtra(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        )
        val eventId = intent.getIntExtra(EXTRA_EVENT_ID, -1)

        if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID && eventId != -1) {
            scope.launch {
                eventWidgetOptionsRepository.updateSelectedEventIds(setOf(eventId), appWidgetId)
                
                // Notify widget update
                val updateIntent = Intent(context, WidgetUpdateBroadcastReceiver::class.java)
                context.sendBroadcast(updateIntent)
            }
        }
    }

    companion object {
        const val EXTRA_EVENT_ID = "extra_event_id"
    }
}
