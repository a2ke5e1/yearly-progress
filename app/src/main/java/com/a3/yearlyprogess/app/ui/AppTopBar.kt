package com.a3.yearlyprogess.app.ui


import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    title: String = "Yearly Progress",
    onSettingsClick: () -> Unit = {}
) {
    CenterAlignedTopAppBar(
        title = { Text(title) },
        actions = {
            IconButton(onClick = onSettingsClick) {
                Icon(
                    Icons.Filled.Settings, contentDescription = "Settings"
                )
            }
        },
    )
}
