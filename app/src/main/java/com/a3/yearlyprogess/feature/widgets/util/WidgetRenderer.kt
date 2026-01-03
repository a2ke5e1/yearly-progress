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

object WidgetRenderer {

    private val linearProgressIds = listOf(
        R.id.widgetProgressBarDefault,
        R.id.widgetProgressBarGreen,
        R.id.widgetProgressBarOcean,
        R.id.widgetProgressBarSunset,
        R.id.widgetProgressBarPurple,
        R.id.widgetProgressBarRose,
        R.id.widgetProgressBarMint,
        R.id.widgetProgressBarCoral,
        R.id.widgetProgressBarSky,
        R.id.widgetProgressBarForest,
        R.id.widgetProgressBarAmber,
        R.id.widgetProgressBarTeal,
        R.id.widgetProgressBarLavender,
        R.id.widgetProgressBarCrimson,
        R.id.widgetProgressBarIndigo,
        R.id.widgetProgressBarPeach,
        R.id.widgetProgressBarCyan,
        R.id.widgetProgressBarMagenta,
        R.id.widgetProgressBarLime,
        R.id.widgetProgressBarRuby,
        R.id.widgetProgressBarSlate,
        R.id.widgetProgressBarEmerald,
        R.id.widgetProgressBarFuchsia,
        R.id.widgetProgressBarYellow,
        R.id.widgetProgressBarViolet,
        R.id.widgetProgressBarSteel,
        R.id.widgetProgressBarBronze,
        R.id.widgetProgressBarAqua,
        R.id.widgetProgressBarCherry,
        R.id.widgetProgressBarNavy,
        R.id.widgetProgressBarSage,
        R.id.widgetProgressBarBurgundy,
        R.id.widgetProgressBarCharcoal,
        R.id.widgetProgressBarTurquoise,
        R.id.widgetProgressBarMauve,
        R.id.widgetProgressBarGold,
        R.id.widgetProgressBarSeafoam,
        R.id.widgetProgressBarRaspberry,
        R.id.widgetProgressBarMidnight,
        R.id.widgetProgressBarOlive,
        R.id.widgetProgressBarSalmon,
        R.id.widgetProgressBarPeriwinkle,
        R.id.widgetProgressBarAutumn,
        R.id.widgetProgressBarGraphite,
        R.id.widgetProgressBarSpring,
        R.id.widgetProgressBarBlush,
        R.id.widgetProgressBarCobalt,
        R.id.widgetProgressBarPistachio,
        R.id.widgetProgressBarMocha,
        R.id.widgetProgressBarLilac,
        R.id.widgetProgressBarTangerine,
        R.id.widgetProgressBarArctic,
        R.id.widgetProgressBarPlum,
        R.id.widgetProgressBarHoneydew,
        R.id.widgetProgressBarDynamic
    )

    private val shapeContainerIds = listOf(
        R.id.widgetContainerDefault,
        R.id.widgetContainerGreen,
        R.id.widgetContainerOcean,
        R.id.widgetContainerSunset,
        R.id.widgetContainerPurple,
        R.id.widgetContainerRose,
        R.id.widgetContainerMint,
        R.id.widgetContainerCoral,
        R.id.widgetContainerSky,
        R.id.widgetContainerForest,
        R.id.widgetContainerAmber,
        R.id.widgetContainerTeal,
        R.id.widgetContainerLavender,
        R.id.widgetContainerCrimson,
        R.id.widgetContainerIndigo,
        R.id.widgetContainerPeach,
        R.id.widgetContainerCyan,
        R.id.widgetContainerMagenta,
        R.id.widgetContainerLime,
        R.id.widgetContainerRuby,
        R.id.widgetContainerSlate,
        R.id.widgetContainerEmerald,
        R.id.widgetContainerFuchsia,
        R.id.widgetContainerYellow,
        R.id.widgetContainerViolet,
        R.id.widgetContainerSteel,
        R.id.widgetContainerBronze,
        R.id.widgetContainerAqua,
        R.id.widgetContainerCherry,
        R.id.widgetContainerNavy,
        R.id.widgetContainerSage,
        R.id.widgetContainerBurgundy,
        R.id.widgetContainerCharcoal,
        R.id.widgetContainerTurquoise,
        R.id.widgetContainerMauve,
        R.id.widgetContainerGold,
        R.id.widgetContainerSeafoam,
        R.id.widgetContainerRaspberry,
        R.id.widgetContainerMidnight,
        R.id.widgetContainerOlive,
        R.id.widgetContainerSalmon,
        R.id.widgetContainerPeriwinkle,
        R.id.widgetContainerAutumn,
        R.id.widgetContainerGraphite,
        R.id.widgetContainerSpring,
        R.id.widgetContainerBlush,
        R.id.widgetContainerCobalt,
        R.id.widgetContainerPistachio,
        R.id.widgetContainerMocha,
        R.id.widgetContainerLilac,
        R.id.widgetContainerTangerine,
        R.id.widgetContainerArctic,
        R.id.widgetContainerPlum,
        R.id.widgetContainerHoneydew,
        R.id.widgetContainerDynamic
    )

    private fun linearProgressId(theme: WidgetTheme): Int =
        when (theme) {
            WidgetTheme.DEFAULT -> R.id.widgetProgressBarDefault
            WidgetTheme.GREEN -> R.id.widgetProgressBarGreen
            WidgetTheme.OCEAN -> R.id.widgetProgressBarOcean
            WidgetTheme.SUNSET -> R.id.widgetProgressBarSunset
            WidgetTheme.PURPLE -> R.id.widgetProgressBarPurple
            WidgetTheme.ROSE -> R.id.widgetProgressBarRose
            WidgetTheme.MINT -> R.id.widgetProgressBarMint
            WidgetTheme.CORAL -> R.id.widgetProgressBarCoral
            WidgetTheme.SKY -> R.id.widgetProgressBarSky
            WidgetTheme.FOREST -> R.id.widgetProgressBarForest
            WidgetTheme.AMBER -> R.id.widgetProgressBarAmber
            WidgetTheme.TEAL -> R.id.widgetProgressBarTeal
            WidgetTheme.LAVENDER -> R.id.widgetProgressBarLavender
            WidgetTheme.CRIMSON -> R.id.widgetProgressBarCrimson
            WidgetTheme.INDIGO -> R.id.widgetProgressBarIndigo
            WidgetTheme.PEACH -> R.id.widgetProgressBarPeach
            WidgetTheme.CYAN -> R.id.widgetProgressBarCyan
            WidgetTheme.MAGENTA -> R.id.widgetProgressBarMagenta
            WidgetTheme.LIME -> R.id.widgetProgressBarLime
            WidgetTheme.RUBY -> R.id.widgetProgressBarRuby
            WidgetTheme.SLATE -> R.id.widgetProgressBarSlate
            WidgetTheme.EMERALD -> R.id.widgetProgressBarEmerald
            WidgetTheme.FUCHSIA -> R.id.widgetProgressBarFuchsia
            WidgetTheme.YELLOW -> R.id.widgetProgressBarYellow
            WidgetTheme.VIOLET -> R.id.widgetProgressBarViolet
            WidgetTheme.STEEL -> R.id.widgetProgressBarSteel
            WidgetTheme.BRONZE -> R.id.widgetProgressBarBronze
            WidgetTheme.AQUA -> R.id.widgetProgressBarAqua
            WidgetTheme.CHERRY -> R.id.widgetProgressBarCherry
            WidgetTheme.NAVY -> R.id.widgetProgressBarNavy
            WidgetTheme.SAGE -> R.id.widgetProgressBarSage
            WidgetTheme.BURGUNDY -> R.id.widgetProgressBarBurgundy
            WidgetTheme.CHARCOAL -> R.id.widgetProgressBarCharcoal
            WidgetTheme.TURQUOISE -> R.id.widgetProgressBarTurquoise
            WidgetTheme.MAUVE -> R.id.widgetProgressBarMauve
            WidgetTheme.GOLD -> R.id.widgetProgressBarGold
            WidgetTheme.SEAFOAM -> R.id.widgetProgressBarSeafoam
            WidgetTheme.RASPBERRY -> R.id.widgetProgressBarRaspberry
            WidgetTheme.MIDNIGHT -> R.id.widgetProgressBarMidnight
            WidgetTheme.OLIVE -> R.id.widgetProgressBarOlive
            WidgetTheme.SALMON -> R.id.widgetProgressBarSalmon
            WidgetTheme.PERIWINKLE -> R.id.widgetProgressBarPeriwinkle
            WidgetTheme.AUTUMN -> R.id.widgetProgressBarAutumn
            WidgetTheme.GRAPHITE -> R.id.widgetProgressBarGraphite
            WidgetTheme.SPRING -> R.id.widgetProgressBarSpring
            WidgetTheme.BLUSH -> R.id.widgetProgressBarBlush
            WidgetTheme.COBALT -> R.id.widgetProgressBarCobalt
            WidgetTheme.PISTACHIO -> R.id.widgetProgressBarPistachio
            WidgetTheme.MOCHA -> R.id.widgetProgressBarMocha
            WidgetTheme.LILAC -> R.id.widgetProgressBarLilac
            WidgetTheme.TANGERINE -> R.id.widgetProgressBarTangerine
            WidgetTheme.ARCTIC -> R.id.widgetProgressBarArctic
            WidgetTheme.PLUM -> R.id.widgetProgressBarPlum
            WidgetTheme.HONEYDEW -> R.id.widgetProgressBarHoneydew
            WidgetTheme.DYNAMIC -> R.id.widgetProgressBarDynamic
        }

    private fun shapeContainerId(theme: WidgetTheme): Int =
        when (theme) {
            WidgetTheme.DEFAULT -> R.id.widgetContainerDefault
            WidgetTheme.GREEN -> R.id.widgetContainerGreen
            WidgetTheme.OCEAN -> R.id.widgetContainerOcean
            WidgetTheme.SUNSET -> R.id.widgetContainerSunset
            WidgetTheme.PURPLE -> R.id.widgetContainerPurple
            WidgetTheme.ROSE -> R.id.widgetContainerRose
            WidgetTheme.MINT -> R.id.widgetContainerMint
            WidgetTheme.CORAL -> R.id.widgetContainerCoral
            WidgetTheme.SKY -> R.id.widgetContainerSky
            WidgetTheme.FOREST -> R.id.widgetContainerForest
            WidgetTheme.AMBER -> R.id.widgetContainerAmber
            WidgetTheme.TEAL -> R.id.widgetContainerTeal
            WidgetTheme.LAVENDER -> R.id.widgetContainerLavender
            WidgetTheme.CRIMSON -> R.id.widgetContainerCrimson
            WidgetTheme.INDIGO -> R.id.widgetContainerIndigo
            WidgetTheme.PEACH -> R.id.widgetContainerPeach
            WidgetTheme.CYAN -> R.id.widgetContainerCyan
            WidgetTheme.MAGENTA -> R.id.widgetContainerMagenta
            WidgetTheme.LIME -> R.id.widgetContainerLime
            WidgetTheme.RUBY -> R.id.widgetContainerRuby
            WidgetTheme.SLATE -> R.id.widgetContainerSlate
            WidgetTheme.EMERALD -> R.id.widgetContainerEmerald
            WidgetTheme.FUCHSIA -> R.id.widgetContainerFuchsia
            WidgetTheme.YELLOW -> R.id.widgetContainerYellow
            WidgetTheme.VIOLET -> R.id.widgetContainerViolet
            WidgetTheme.STEEL -> R.id.widgetContainerSteel
            WidgetTheme.BRONZE -> R.id.widgetContainerBronze
            WidgetTheme.AQUA -> R.id.widgetContainerAqua
            WidgetTheme.CHERRY -> R.id.widgetContainerCherry
            WidgetTheme.NAVY -> R.id.widgetContainerNavy
            WidgetTheme.SAGE -> R.id.widgetContainerSage
            WidgetTheme.BURGUNDY -> R.id.widgetContainerBurgundy
            WidgetTheme.CHARCOAL -> R.id.widgetContainerCharcoal
            WidgetTheme.TURQUOISE -> R.id.widgetContainerTurquoise
            WidgetTheme.MAUVE -> R.id.widgetContainerMauve
            WidgetTheme.GOLD -> R.id.widgetContainerGold
            WidgetTheme.SEAFOAM -> R.id.widgetContainerSeafoam
            WidgetTheme.RASPBERRY -> R.id.widgetContainerRaspberry
            WidgetTheme.MIDNIGHT -> R.id.widgetContainerMidnight
            WidgetTheme.OLIVE -> R.id.widgetContainerOlive
            WidgetTheme.SALMON -> R.id.widgetContainerSalmon
            WidgetTheme.PERIWINKLE -> R.id.widgetContainerPeriwinkle
            WidgetTheme.AUTUMN -> R.id.widgetContainerAutumn
            WidgetTheme.GRAPHITE -> R.id.widgetContainerGraphite
            WidgetTheme.SPRING -> R.id.widgetContainerSpring
            WidgetTheme.BLUSH -> R.id.widgetContainerBlush
            WidgetTheme.COBALT -> R.id.widgetContainerCobalt
            WidgetTheme.PISTACHIO -> R.id.widgetContainerPistachio
            WidgetTheme.MOCHA -> R.id.widgetContainerMocha
            WidgetTheme.LILAC -> R.id.widgetContainerLilac
            WidgetTheme.TANGERINE -> R.id.widgetContainerTangerine
            WidgetTheme.ARCTIC -> R.id.widgetContainerArctic
            WidgetTheme.PLUM -> R.id.widgetContainerPlum
            WidgetTheme.HONEYDEW -> R.id.widgetContainerHoneydew
            WidgetTheme.DYNAMIC -> R.id.widgetContainerDefault
        }

    fun applyLinearProgressBar(
        views: RemoteViews,
        progress: Int,
        theme: WidgetTheme
    ) {
        val activeId = linearProgressId(theme)
        linearProgressIds.forEach { id ->
            if (id == activeId) {
                views.setViewVisibility(id, View.VISIBLE)
                views.setProgressBar(id, 100, progress, false)
            } else {
                views.setViewVisibility(id, View.GONE)
            }
        }
    }

    fun applyCloverProgressContainer(views: RemoteViews, progress: Double, theme: WidgetTheme) {
        val activeId = shapeContainerId(theme)
        shapeContainerIds.forEach { id ->
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
        val activeId = shapeContainerId(theme)
        shapeContainerIds.forEach { id ->
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
        minSp: Float = 8f,
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

     fun onParentTap(view: RemoteViews, context: Context) {
        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
            ?: Intent(context, MainActivity::class.java)

        view.setOnClickPendingIntent(
            R.id.background,
            PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
            ),
        )
    }


}