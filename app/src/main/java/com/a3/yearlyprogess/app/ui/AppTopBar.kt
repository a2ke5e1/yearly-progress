package com.a3.yearlyprogess.app.ui

import android.content.Context
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Deselect
import androidx.compose.material.icons.outlined.SelectAll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import com.a3.yearlyprogess.BuildConfig
import com.a3.yearlyprogess.R
import com.a3.yearlyprogess.feature.events.presentation.EventViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    modifier: Modifier = Modifier,
    title: String = stringResource(R.string.app_name),
    scrollBehavior: TopAppBarScrollBehavior,
    onSettingsClick: (() -> Unit)? = null,
    onNavigateUp: (() -> Unit)? = null,
    onImportEvents: (() -> Unit)? = null,
    eventViewModel: EventViewModel? = null,
    showShareButton: Boolean = false
    ) {
    var expanded by remember { mutableStateOf(false) }
    val selectedIds = eventViewModel?.selectedIds?.collectAsState()
    val isAllSelected = eventViewModel?.isAllSelected?.collectAsState()
    val isActionMode by remember { derivedStateOf { selectedIds?.value?.isNotEmpty() == true } }
    var showDeleteDialogBox by remember { mutableStateOf(false) }

    val context = LocalContext.current
    TopAppBar(
        title = {
            if (isActionMode) Text("Select events ${selectedIds?.value?.size ?: 0}") else Text(
                title
            )
        },
        navigationIcon = {
            if (onNavigateUp != null && !isActionMode) {
                IconButton(onClick = onNavigateUp) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            }
            if (isActionMode) {
                IconButton(onClick = {
                    eventViewModel?.clearSelection()
                }) {
                    Icon(Icons.Outlined.Close, contentDescription = "Back")
                }
            }
        },
        actions = {
            AnimatedVisibility(
                visible = isActionMode,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                IconButton(onClick = {
                    showDeleteDialogBox = true
                }) {
                    Icon(Icons.Outlined.Delete, contentDescription = "Delete")
                }
            }
            AnimatedVisibility(
                visible = isActionMode,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                IconButton(onClick = {
                    eventViewModel?.toggleAllSelections()
                }) {
                    Icon(
                        if (isAllSelected?.value == true)
                            Icons.Outlined.Deselect
                        else
                            Icons.Outlined.SelectAll, contentDescription = "Toggle selection"
                    )
                }
            }
            Box {
                if (onImportEvents != null || onSettingsClick != null || showShareButton) {
                    IconButton(onClick = { expanded = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More")
                    }
                }
                DropdownMenu(
                    expanded = expanded, onDismissRequest = { expanded = false }) {
                    if (onImportEvents != null) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.import_events)) },
                            onClick = {
                                expanded = false
                                onImportEvents()
                            })
                    }
                    if(onSettingsClick != null) {
                        DropdownMenuItem(text = { Text(stringResource(R.string.settings)) }, onClick = {
                            expanded = false
                            onSettingsClick()
                        })
                    }
                    if (showShareButton) {
                        DropdownMenuItem(text = { Text(stringResource(R.string.share)) }, onClick = {
                            expanded = false
                            showShareScreen(context)
                        })
                    }
                }
            }
        }, scrollBehavior = scrollBehavior, modifier = modifier,
    )

    if (showDeleteDialogBox) {
        AlertDialog(
            onDismissRequest = {
                showDeleteDialogBox = false
                eventViewModel?.clearSelection()
            },
            title = { Text(text = "Delete Event", style = MaterialTheme.typography.bodyLarge) },
            text = {
                val selectionCount = selectedIds?.value?.size ?: 0
                val pluralString = pluralStringResource(
                    R.plurals.delete_events_selection_confirmation,
                    count = selectionCount
                )
                val annotatedString = buildAnnotatedString {
                    // Split the string by the placeholder to insert the styled number
                    val parts = pluralString.split("%d")

                    append(parts.getOrNull(0) ?: "") // Append text before the number

                    // Append the number with a bold style
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(selectionCount.toString())
                    }

                    append(parts.getOrNull(1) ?: "") // Append text after the number
                }
                Text(text = annotatedString)
            },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialogBox = false
                    eventViewModel?.deleteSelectedEvents()
                }) { Text("Yes") }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDeleteDialogBox = false
                }) { Text("No") }
            },
        )
    }
}

private fun showShareScreen(context: Context): Boolean {
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
    return true
}
