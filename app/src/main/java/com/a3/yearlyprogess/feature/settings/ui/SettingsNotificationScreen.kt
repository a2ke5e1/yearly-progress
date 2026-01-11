package com.a3.yearlyprogess.feature.settings.ui

import android.app.Activity
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.a3.yearlyprogess.R
import com.a3.yearlyprogess.core.ui.components.Switch
import com.a3.yearlyprogess.core.util.YearlyProgressNotification
import com.a3.yearlyprogess.feature.widgets.update.WidgetUpdateBroadcastReceiver
import kotlinx.coroutines.launch

@Composable
fun SettingsNotificationScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    yearlyProgressNotification: YearlyProgressNotification,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val settings by viewModel.settings.collectAsState()
    val notificationSettings = settings.notificationSettings

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = contentPadding
    ) {
        item {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
            ) {
                Switch(
                    modifier = Modifier.padding(16.dp),
                    title = stringResource(R.string.progress_notification),
                    description = stringResource(R.string.shows_progress_in_the_notification),
                    checked = notificationSettings.progressShowNotification,
                    onCheckedChange = { newValue ->
                        if (newValue) {
                            if (!yearlyProgressNotification.hasAppNotificationPermission()) {
                                // Request notification permission if needed
                                (context as? Activity)?.let { activity ->
                                    yearlyProgressNotification.requestNotificationPermission(activity)
                                }
                            }
                        }

                        // Broadcast widget update
                        val widgetUpdateServiceIntent =
                            Intent(context, WidgetUpdateBroadcastReceiver::class.java)
                        context.sendBroadcast(widgetUpdateServiceIntent)

                        // Update notification status
                        scope.launch {
                            viewModel.setProgressShowNotification(newValue)

                            // Update notification immediately based on new setting
                            if (newValue) {
                                yearlyProgressNotification.showProgressNotification(
                                    settings.copy(
                                        notificationSettings = notificationSettings.copy(
                                            progressShowNotification = newValue
                                        )
                                    )
                                )
                            } else {
                                yearlyProgressNotification.hideProgressNotification()
                            }
                        }
                    }
                )
            }
        }

        item {
            Text(
                text = stringResource(R.string.options),
                style = MaterialTheme.typography.labelLarge.copy(
                    color = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            Switch(
                title = stringResource(R.string.year),
                checked = notificationSettings.progressShowNotificationYear,
                onCheckedChange = { newValue ->
                    scope.launch {
                        viewModel.setProgressShowNotificationYear(newValue)

                        // Update notification if it's enabled
                        if (notificationSettings.progressShowNotification) {
                            yearlyProgressNotification.showProgressNotification(
                                settings.copy(
                                    notificationSettings = notificationSettings.copy(
                                        progressShowNotificationYear = newValue
                                    )
                                )
                            )
                        }
                    }
                }
            )
        }

        item {
            Switch(
                title = stringResource(R.string.month),
                checked = notificationSettings.progressShowNotificationMonth,
                onCheckedChange = { newValue ->
                    scope.launch {
                        viewModel.setProgressShowNotificationMonth(newValue)

                        // Update notification if it's enabled
                        if (notificationSettings.progressShowNotification) {
                            yearlyProgressNotification.showProgressNotification(
                                appSettings = settings.copy(
                                    notificationSettings = notificationSettings.copy(
                                        progressShowNotificationMonth = newValue
                                    )
                                )
                            )
                        }
                    }
                }
            )
        }

        item {
            Switch(
                title = stringResource(R.string.week),
                checked = notificationSettings.progressShowNotificationWeek,
                onCheckedChange = { newValue ->
                    scope.launch {
                        viewModel.setProgressShowNotificationWeek(newValue)

                        // Update notification if it's enabled
                        if (notificationSettings.progressShowNotification) {
                            yearlyProgressNotification.showProgressNotification(
                                settings.copy(
                                    notificationSettings = notificationSettings.copy(
                                        progressShowNotificationWeek = newValue
                                    )
                                )
                            )
                        }
                    }
                }
            )
        }

        item {
            Switch(
                title = stringResource(R.string.day),
                checked = notificationSettings.progressShowNotificationDay,
                onCheckedChange = { newValue ->
                    scope.launch {
                        viewModel.setProgressShowNotificationDay(newValue)

                        // Update notification if it's enabled
                        if (notificationSettings.progressShowNotification) {
                            yearlyProgressNotification.showProgressNotification(
                                settings.copy(
                                    notificationSettings = notificationSettings.copy(
                                        progressShowNotificationDay = newValue
                                    )
                                )
                            )
                        }
                    }
                }
            )
        }
    }
}