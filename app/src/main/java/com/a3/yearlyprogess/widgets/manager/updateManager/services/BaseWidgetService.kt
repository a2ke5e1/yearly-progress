package com.a3.yearlyprogess.widgets.manager.updateManager.services

import android.appwidget.AppWidgetManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent

abstract class BaseWidgetService : BroadcastReceiver() {



    abstract fun setIntent(context: Context): Intent

    abstract fun setComponent(context: Context): ComponentName

    override fun onReceive(context: Context, intent: Intent) {
        //wake the device
        //WakeLocker.acquire(context)

        //force widget update
        val widgetIntent = setIntent(context)
        widgetIntent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        val ids = AppWidgetManager.getInstance(context)
            .getAppWidgetIds(setComponent(context))
        widgetIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
        context.sendBroadcast(widgetIntent)

        //go back to sleep
        //WakeLocker.release()
    }
}