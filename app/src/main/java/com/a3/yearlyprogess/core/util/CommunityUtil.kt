package com.a3.yearlyprogess.core.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.a3.yearlyprogess.BuildConfig
import com.a3.yearlyprogess.R

object CommunityUtil {

    private const val SUPPORT_GROUP_URL = "https://t.me/phycalc"

    fun onShare(context: Context) {
        // Launch share if users want to share app with their friends
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.app_name))
        val shareMessage =
            context.getString(
                R.string.share_message,
                context.getString(R.string.app_name),
                BuildConfig.APPLICATION_ID
            )
                .trimIndent()
        intent.putExtra(Intent.EXTRA_TEXT, shareMessage)
        context.startActivity(Intent.createChooser(intent, "Share"))
    }

    fun onJoinSupportGroup(context: Context) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(SUPPORT_GROUP_URL)
        context.startActivity(intent)
    }

}