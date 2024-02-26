package com.a3.yearlyprogess.widgets.manager.updateManager

import android.content.Context
import android.os.PowerManager

object WakeLocker {
    private var wakeLock: PowerManager.WakeLock? = null

    //wake the device
    fun acquire(context: Context) {
        wakeLock?.release()
        val powerManager: PowerManager =
            context.getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK or
                    PowerManager.ACQUIRE_CAUSES_WAKEUP or
                    PowerManager.ON_AFTER_RELEASE,
            "WIDGET: Wake lock acquired!"
        )
        wakeLock?.acquire(2000)
    }

    fun release() {
        wakeLock?.release()
        wakeLock = null
    }
}