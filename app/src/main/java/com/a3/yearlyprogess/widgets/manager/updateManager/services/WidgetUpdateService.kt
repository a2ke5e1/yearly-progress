package com.a3.yearlyprogess.widgets.manager.updateManager.services

import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.preference.PreferenceManager
import com.a3.yearlyprogess.R
import com.a3.yearlyprogess.widgets.manager.updateManager.WidgetUpdateAlarmHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class WidgetUpdateService : Service() {

    private val channelId = "yearly_progress_service_channel"
    private lateinit var coroutineScope: CoroutineScope


    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(1, createNotification())

        coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())


        coroutineScope.launch {
            while (true) {

                // Checks if user wants to use foreground service to update widgets
                // If not, stops the service.
                if (!useForegroundService(this@WidgetUpdateService)) {
                    stopSelf()
                    break
                }


                try {
                    val intent =
                        Intent(this@WidgetUpdateService, WidgetUpdateBroadcastReceiver::class.java)
                    intent.action = "com.a3.yearlyprogress.RESTART_WIDGET_UPDATE_SERVICE"
                    sendBroadcast(intent)
                } catch (ex: Exception) {
                    Log.e("ProgressService", "Error updating widget", ex)
                }
                delay(1000)
            }
        }
    }

    override fun onDestroy() {
        coroutineScope.cancel()
        super.onDestroy()
         val am = WidgetUpdateAlarmHandler(this)

        am.scheduleRestartWidgetUpdateForegroundService()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Yearly Progress Service")
            .setContentText("Updating all widgets...")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .build()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            channelId,
            "Widget Updater",
            NotificationManager.IMPORTANCE_LOW
        )
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }
}

fun useForegroundService(context: Context): Boolean {
    val settingPref = PreferenceManager.getDefaultSharedPreferences(context)
    return settingPref?.getBoolean(
        context.getString(R.string.widget_widget_use_foreground_service),
        false
    ) ?: false
}

