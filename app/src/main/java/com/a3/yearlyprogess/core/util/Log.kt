package com.a3.yearlyprogess.core.util

import android.util.Log
import com.a3.yearlyprogess.BuildConfig

object Log {

    fun d(tag: String, message: String, throwable: Throwable? = null) {
        if (BuildConfig.DEBUG) {
            if (throwable != null) Log.d(tag, message, throwable)
            else Log.d(tag, message)
        }
    }

    fun i(tag: String, message: String, throwable: Throwable? = null) {
        if (BuildConfig.DEBUG) {
            if (throwable != null) Log.i(tag, message, throwable)
            else Log.i(tag, message)
        }
    }

    fun w(tag: String, message: String, throwable: Throwable? = null) {
        if (BuildConfig.DEBUG) {
            if (throwable != null) Log.w(tag, message, throwable)
            else Log.w(tag, message)
        }
    }

    fun e(tag: String, message: String, throwable: Throwable? = null) {
        if (BuildConfig.DEBUG) {
            if (throwable != null) Log.e(tag, message, throwable)
            else Log.e(tag, message)
        }
    }
}
