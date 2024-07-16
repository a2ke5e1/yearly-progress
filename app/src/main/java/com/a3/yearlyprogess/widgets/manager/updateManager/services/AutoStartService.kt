package com.a3.yearlyprogess.widgets.manager.updateManager.services;

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.a3.yearlyprogess.widgets.manager.updateManager.services.WidgetUpdateService

// This service will be started on boot up
// It will trigger the widget update service.
class AutoStartService : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action == Intent.ACTION_BOOT_COMPLETED || action.equals("com.a3.yearlyprogress.RESTART_WIDGET_UPDATE_SERVICE")) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
                Log.d("AutoStartService", "Starting foreground service is not supported on android 15 and above")
                return
            }
            val serviceIntent = Intent(context, WidgetUpdateService::class.java)
            context.startForegroundService(serviceIntent)
        }
    }
}


