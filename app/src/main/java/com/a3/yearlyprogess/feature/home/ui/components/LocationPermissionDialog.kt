package com.a3.yearlyprogess.feature.home.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import com.a3.yearlyprogess.R
import com.a3.yearlyprogess.core.ui.components.PermissionDialog

@Composable
fun LocationPermissionDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    PermissionDialog(
        icon = Icons.Default.LocationOn,
        title = stringResource(R.string.location_permission_title),
        description = stringResource(R.string.location_permission_description),
        bulletPoints = stringArrayResource(R.array.location_permission_bullet_points),
        onDismiss = onDismiss,
        onConfirm = onConfirm,
    )
}