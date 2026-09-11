package com.a3.yearlyprogess.feature.widgets.util

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.TypedValue
import android.view.View
import android.widget.RemoteViews
import androidx.annotation.DimenRes
import androidx.annotation.IdRes
import com.a3.yearlyprogess.R
import com.a3.yearlyprogess.app.MainActivity
import com.a3.yearlyprogess.feature.widgets.domain.model.WidgetColors
import com.a3.yearlyprogess.feature.widgets.domain.model.WidgetTheme
import com.a3.yearlyprogess.feature.widgets.update.WidgetUpdateBroadcastReceiver

object WidgetRenderer {

    private const val START_APP_REQUEST_CODE = 200
    private const val UPDATE_REQUEST_CODE = 201

    fun applyLinearProgressBar(
        views: RemoteViews,
        progress: Int,
        theme: WidgetTheme
    ) {
        val activeId = WidgetThemeIds.linearProgressId(theme)
        WidgetThemeIds.linearProgressIds.forEach { id ->
            if (id == activeId) {
                views.setViewVisibility(id, View.VISIBLE)
                views.setProgressBar(id, 100, progress, false)
            } else {
                views.setViewVisibility(id, View.GONE)
            }
        }
    }

    fun applyCloverProgressContainer(views: RemoteViews, progress: Double, theme: WidgetTheme) {
        val activeId = WidgetThemeIds.shapeContainerId(theme)
        WidgetThemeIds.shapeContainerIds.forEach { id ->
            if (id == activeId) {
                views.setViewVisibility(id, View.VISIBLE)
                views.setImageViewResource(
                    id,
                    when (progress) {
                        in 0.0..5.0 -> R.drawable.background_clover_00
                        in 5.0..10.0 -> R.drawable.background_clover_05
                        in 10.0..20.0 -> R.drawable.background_clover_10
                        in 20.0..30.0 -> R.drawable.background_clover_20
                        in 30.0..40.0 -> R.drawable.background_clover_30
                        in 40.0..50.0 -> R.drawable.background_clover_40
                        in 50.0..60.0 -> R.drawable.background_clover_50
                        in 60.0..70.0 -> R.drawable.background_clover_60
                        in 70.0..80.0 -> R.drawable.background_clover_70
                        in 80.0..90.0 -> R.drawable.background_clover_80
                        in 90.0..95.0 -> R.drawable.background_clover_90
                        in 95.0..98.0 -> R.drawable.background_clover_95
                        else -> R.drawable.background_clover_100
                    },
                )
            } else {
                views.setViewVisibility(id, View.GONE)
            }
        }
    }

    fun applyPillProgressContainer(views: RemoteViews, progress: Double, theme: WidgetTheme) {
        val activeId = WidgetThemeIds.shapeContainerId(theme)
        WidgetThemeIds.shapeContainerIds.forEach { id ->
            if (id == activeId) {
                views.setViewVisibility(id, View.VISIBLE)
                views.setImageViewResource(
                    id,
                    when (progress) {
                        in 0.0..5.0 -> R.drawable.background_pill_00
                        in 5.0..10.0 -> R.drawable.background_pill_05
                        in 10.0..20.0 -> R.drawable.background_pill_10
                        in 20.0..30.0 -> R.drawable.background_pill_20
                        in 30.0..40.0 -> R.drawable.background_pill_30
                        in 40.0..50.0 -> R.drawable.background_pill_40
                        in 50.0..60.0 -> R.drawable.background_pill_50
                        in 60.0..70.0 -> R.drawable.background_pill_60
                        in 70.0..80.0 -> R.drawable.background_pill_70
                        in 80.0..90.0 -> R.drawable.background_pill_80
                        in 90.0..95.0 -> R.drawable.background_pill_90
                        in 95.0..98.0 -> R.drawable.background_pill_95
                        else -> R.drawable.background_pill_100
                    },
                )
            } else {
                views.setViewVisibility(id, View.GONE)
            }
        }
    }

    private fun pxToSp(context: Context, @DimenRes id: Int): Float {
        val res = context.resources
        val metrics = res.displayMetrics
        val px = res.getDimension(id)

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            TypedValue.deriveDimension(TypedValue.COMPLEX_UNIT_SP, px, metrics)
        } else {
            TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_PX,
                px,
                metrics
            ) / metrics.density
        }
    }

    private fun getSpValue(context: Context, @DimenRes id: Int): Float {
        val typedValue = TypedValue()
        context.resources.getValue(id, typedValue, true)
        return TypedValue.complexToFloat(typedValue.data)
    }

    fun RemoteViews.applyTextViewTextSize(
        context: Context,
        @IdRes viewId: Int,
        @DimenRes defaultTextSize: Int,
        fontScale: Float = 1f,
        minSp: Float = 4f,
        maxSp: Float = 32f
    ) {
        val baseSp = getSpValue(context, defaultTextSize)
        val finalSp = (baseSp * fontScale).coerceIn(minSp, maxSp)
        this.setTextViewTextSize(viewId, TypedValue.COMPLEX_UNIT_SP, finalSp)
    }

    fun errorWidgetRemoteView(context: Context, message: String, widgetTheme: WidgetTheme = WidgetTheme.DEFAULT): RemoteViews {
        val view = RemoteViews(context.packageName, R.layout.error_widget)
        val widgetColors = WidgetColors.fromTheme(context, widgetTheme)
        view.setTextViewText(R.id.error_text, message)
            view.setTextColor(R.id.error_text, widgetColors.primaryColor)
        return view
    }

     fun onParentTap(view: RemoteViews, context: Context, @IdRes viewId: Int? = null) {
        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
            ?: Intent(context, MainActivity::class.java)

        view.setOnClickPendingIntent(
            viewId ?: R.id.background,
            PendingIntent.getActivity(
                context,
                START_APP_REQUEST_CODE,
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
            ),
        )
    }

    fun onParentTapToUpdate(view: RemoteViews, context: Context, @IdRes viewId: Int? = null) {
        val intent = Intent(context, WidgetUpdateBroadcastReceiver::class.java)

        view.setOnClickPendingIntent(
            viewId ?: R.id.background,
            PendingIntent.getBroadcast(
                context,
                UPDATE_REQUEST_CODE,
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
            ),
        )
    }


    fun applyCircularProgressBar(
        views: RemoteViews,
        progress: Int,
        theme: WidgetTheme
    ) {
        val activeId = WidgetThemeIds.circularProgressId(theme)
        WidgetThemeIds.circularProgressIds.forEach { id ->
            if (id == activeId) {
                views.setViewVisibility(id, View.VISIBLE)
                views.setProgressBar(id, 100, progress, false)
            } else {
                views.setViewVisibility(id, View.GONE)
            }
        }
    }



}