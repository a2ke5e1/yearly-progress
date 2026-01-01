package com.a3.yearlyprogess.feature.events.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import com.a3.yearlyprogess.R
import com.a3.yearlyprogess.core.ui.components.PermissionDialog

@Composable
fun CalendarPermissionDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    PermissionDialog(
        icon = Icons.Default.CalendarToday,
        title = stringResource(R.string.calendar_permission_title),
        description = stringResource(R.string.calendar_permission_description),
        bulletPoints = stringArrayResource(R.array.calendar_permission_bullet_points),
        onDismiss = onDismiss,
        onConfirm = onConfirm,
    )
}
