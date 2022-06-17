package com.a3.yearlyprogess.manager.services;

import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.a3.yearlyprogess.manager.WakeLocker;
import com.a3.yearlyprogess.mwidgets.DayWidget;

public class DayWidgetService extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        //wake the device
        WakeLocker.acquire(context);

        //increase the number in the widget
        SharedPreferences preferences = context.getSharedPreferences("PREFS", 0);
        int value = preferences.getInt("value", 1);
        value++;
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("value", value);
        editor.apply();

        //force widget update
        Intent widgetIntent = new Intent(context, DayWidget.class);
        widgetIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        int[] ids = AppWidgetManager.getInstance(context).getAppWidgetIds(new ComponentName(context, DayWidget.class));
        widgetIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        context.sendBroadcast(widgetIntent);

        Log.d("WIDGET", "Widget set to update!");

        //go back to sleep
        WakeLocker.release();
    }
}
