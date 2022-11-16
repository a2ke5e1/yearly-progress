package com.a3.yearlyprogess.manager.services

import android.content.Context
import android.content.Intent

class AllInWidgetService : BaseWidgetService() {
    override fun setIntent(context: Context): Intent {
        return Intent(context, AllInWidgetService::class.java)
    }

}