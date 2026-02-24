package com.a3.yearlyprogess.feature.events.ui.components

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.icu.text.NumberFormat
import android.os.Build
import android.os.Bundle
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.a3.yearlyprogess.core.domain.model.AppSettings
import com.a3.yearlyprogess.core.ui.interaction.PressAnimationConfig
import com.a3.yearlyprogess.core.ui.interaction.applyPressGesture
import com.a3.yearlyprogess.core.ui.interaction.rememberPressInteractionState
import com.a3.yearlyprogess.core.ui.style.CardCornerStyle
import com.a3.yearlyprogess.core.util.ProgressSettings
import com.a3.yearlyprogess.core.util.YearlyProgressUtil
import com.a3.yearlyprogess.core.util.formatEventDateTime
import com.a3.yearlyprogess.core.util.formatEventTimeStatus
import com.a3.yearlyprogess.feature.events.domain.model.Event
import com.a3.yearlyprogess.feature.home.ui.components.FormattedPercentage
import com.a3.yearlyprogess.feature.widgets.domain.model.EventWidgetOptions
import com.a3.yearlyprogess.feature.widgets.domain.model.WidgetTheme
import com.a3.yearlyprogess.feature.widgets.ui.EventWidget
import com.a3.yearlyprogess.feature.widgets.update.PinEventWidgetReceiver
import kotlinx.coroutines.delay


data class EventDetailCardStyle(
    val cardHeight: Dp,
    val cardPadding: Dp,
    val backgroundColor: Color,
    val labelTextStyle: TextStyle,
    val titleTextStyle: TextStyle,
    val progressTextStyle: TextStyle,
    val durationTextStyle: TextStyle,
    val cornerStyle: CardCornerStyle,
    val pressConfig: PressAnimationConfig
)


object EventDetailCardDefaults {

    @Composable
    fun eventDetailCardStyle(
        cardHeight: Dp = 160.dp,
        cardPadding: Dp = 18.dp,
        progressBarColor: Color = MaterialTheme.colorScheme.primaryContainer,
        backgroundColor: Color = MaterialTheme.colorScheme.surfaceContainer,
        labelTextStyle: TextStyle = MaterialTheme.typography.labelSmall.copy(
            color = MaterialTheme.colorScheme.onSurface
        ),
        titleTextStyle: TextStyle = MaterialTheme.typography.titleMedium.copy(
            color = MaterialTheme.colorScheme.onSurface
        ),
        progressTextStyle: TextStyle = MaterialTheme.typography.bodySmall.copy(
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        ),
        durationTextStyle: TextStyle = MaterialTheme.typography.bodySmall.copy(
            color = MaterialTheme.colorScheme.onSurface
        ),
        cornerStyle: CardCornerStyle = CardCornerStyle.Default,
        pressConfig: PressAnimationConfig = PressAnimationConfig()
    ): EventDetailCardStyle = EventDetailCardStyle(
        cardHeight = cardHeight,
        cardPadding = cardPadding,
        backgroundColor = backgroundColor,
        labelTextStyle = labelTextStyle,
        titleTextStyle = titleTextStyle,
        progressTextStyle = progressTextStyle,
        durationTextStyle = durationTextStyle,
        cornerStyle = cornerStyle,
        pressConfig = pressConfig
    )
}

data class EventProgressState(
    val progress: Double,
    val statusText: String,
    val formattedText: String,
)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun EventDetailCard(
    modifier: Modifier = Modifier,
    event: Event,
    onClick: () -> Unit,
    isSelected: Boolean = false,
    showPinOption: Boolean = true,
    onLongPress: (() -> Unit)? = null,
    refreshInterval: Long = 16L,
    style: EventDetailCardStyle = EventDetailCardDefaults.eventDetailCardStyle(),
    settings: AppSettings
) {
    val decimals = settings.eventProgressDecimalDigits.coerceIn(0, 6)
    val progressUtil = remember { YearlyProgressUtil(settings.progressSettings) }
    val context = LocalContext.current

    val uiState by produceState(
        initialValue = EventProgressState(0.0, "", ""),
        key1 = event
    ) {
        val (start, end) = event.nextStartAndEndTime()
        while (true) {
            value = EventProgressState(
                progress = progressUtil.calculateProgress(start, end),
                statusText = formatEventTimeStatus(context, start, end),
                formattedText = formatEventDateTime(context, start, end, event.allDayEvent)
            )
            delay(refreshInterval)
        }
    }

    val pressState = rememberPressInteractionState(style.pressConfig)
    val animatedCorners = pressState.animateCorners(default = style.cornerStyle)

    pressState.setPressed(isSelected)

    // Track if the background image is successfully loaded
    var isImageVisible by remember(event.backgroundImageUri) { mutableStateOf(false) }

    // Animate background color for a smoother transition when image loads
    val animatedBackgroundColor by animateColorAsState(
        targetValue = if (isImageVisible) MaterialTheme.colorScheme.surface else style.backgroundColor,
        label = "EventCardBackground"
    )

    Box(
        modifier = modifier
            .height(style.cardHeight)
            .fillMaxWidth()
            .background(
                color = animatedBackgroundColor,
                shape = style.cornerStyle.toAnimatedShape(animatedCorners)
            )
            .clip(style.cornerStyle.toAnimatedShape(animatedCorners))
            .applyPressGesture(pressState, onTap = onClick, onLongPress = onLongPress)
    ) {
        // Background Image (if available)
        event.backgroundImageUri?.let { imagePath ->
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imagePath)
                    .crossfade(true)
                    .build(),
                onState = { state ->
                    isImageVisible = state is AsyncImagePainter.State.Success
                },
                contentDescription = null,
                contentScale = ContentScale.Crop,
                alpha = 0.2f,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(style.cornerStyle.toAnimatedShape(animatedCorners))
            )
        }

        // Content
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f),
                ) {
                    Text(
                            text = event.eventTitle, maxLines = 1, overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.titleMediumEmphasized,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    Text(
                        text = uiState.formattedText,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Justify,
                    )
                    Text(
                        text = uiState.statusText,
                        style = MaterialTheme.typography.labelSmall,
                        color =  MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
                    )
                    if (event.eventDescription.isNotEmpty()) {
                        Text(
                            text = event.eventDescription.trimIndent(),
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.bodySmall,
                            color =  MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Justify,
                            maxLines = 4
                        )
                    }
                }
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.padding(start = 8.dp).align(Alignment.CenterVertically)
                ) {
                    CircularWavyProgressIndicator(
                        progress = {
                            uiState.progress.toFloat() / 100
                        },
                        modifier = Modifier.size(80.dp),
                    )
                    FormattedPercentage(
                        progressProvider = { uiState.progress },
                        digits = decimals,
                        style = style.progressTextStyle,
                    )
                }
            }
        }

        if (showPinOption) {
            val appWidgetManager = context.getSystemService(AppWidgetManager::class.java)
            if (appWidgetManager != null && appWidgetManager.isRequestPinAppWidgetSupported) {
                FilledTonalIconButton(
                    onClick = {
                        val myProvider = ComponentName(context, EventWidget::class.java)
                        val successCallback = Intent(context, PinEventWidgetReceiver::class.java).apply {
                            putExtra(PinEventWidgetReceiver.EXTRA_EVENT_ID, event.id)
                        }.let { intent ->
                            PendingIntent.getBroadcast(
                                context,
                                event.id,
                                intent,
                                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
                            )
                        }

                        val remoteViews = EventWidget.responsiveRemoteView(
                            context = context,
                            event = event,
                            yp = progressUtil,
                            theme = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) WidgetTheme.DYNAMIC else WidgetTheme.DEFAULT,
                            userConfig = EventWidgetOptions(
                                theme = null,
                                timeStatusCounter = true,
                                dynamicTimeStatusCounter = false,
                                replaceProgressWithTimeLeft = false,
                                decimalDigits = decimals,
                                backgroundTransparency = 100,
                                fontScale = 1.0f,
                                showEventImage = true
                            )
                        )

                        val bundle = Bundle().apply {
                            putParcelable(AppWidgetManager.EXTRA_APPWIDGET_PREVIEW, remoteViews)
                        }

                        appWidgetManager.requestPinAppWidget(myProvider, bundle, successCallback)
                    },
                    modifier = Modifier.size(24.dp).align(Alignment.TopEnd).offset((-12).dp, (10).dp),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.PushPin,
                        contentDescription = "Pin to home screen",
                        modifier = Modifier.size(12.dp),
                        tint = IconButtonDefaults.filledTonalIconButtonColors().contentColor,
                    )
                }
            }
        }

        // Selection Overlay
        if (isSelected) {
            Box(
                Modifier
                    .matchParentSize()
                    .background(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        shape = style.cornerStyle.toAnimatedShape(animatedCorners)
                    )
            )
        }
    }
}
