package com.a3.yearlyprogess.app.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import com.a3.yearlyprogess.R
import com.a3.yearlyprogess.core.backup.BackupManager
import com.a3.yearlyprogess.feature.backup_restore.BackupRestoreDialog
import com.a3.yearlyprogess.core.util.CommunityUtil
import com.a3.yearlyprogess.feature.events.presentation.EventViewModel
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

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
    backupManager: BackupManager? = null,
    showShareButton: Boolean = false,
    showAboutButton: Boolean = false,
    showBackAndRestore: Boolean = false
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedIds = eventViewModel?.selectedIds?.collectAsState()
    val isAllSelected = eventViewModel?.isAllSelected?.collectAsState()
    val isActionMode by remember { derivedStateOf { selectedIds?.value?.isNotEmpty() == true } }
    var showDeleteDialogBox by remember { mutableStateOf(false) }
    var showBackupRestoreDialog by remember { mutableStateOf(false) }
    var showAboutDialogBox by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Generate timestamp for backup filename
    val backupFileName = remember {
        val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
        "backup-$timestamp.ypp"
    }

    val backupLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.CreateDocument("application/octet-stream")
        ) { uri ->
            uri ?: return@rememberLauncherForActivityResult
            scope.launch {
                backupManager?.backup(uri)
            }
        }
    val restoreLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.OpenDocument()
        ) { uri ->
            uri ?: return@rememberLauncherForActivityResult
            scope.launch {
                backupManager?.restore(uri)
            }
        }
    TopAppBar(
        title = {
            if (isActionMode) Text("${selectedIds?.value?.size ?: 0}") else Text(
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
                    if (showBackAndRestore) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.backup_restore)) },
                            onClick = {
                                expanded = false
                                showBackupRestoreDialog = true
                            }
                        )
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
                            CommunityUtil.onShare(context)
                        })
                    }
                    if (showAboutButton) {
                        DropdownMenuItem(text = { Text(stringResource(R.string.about)) }, onClick = {
                            expanded = false
                            showAboutDialogBox = true
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
                }) { Text(stringResource(R.string.yes)) }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDeleteDialogBox = false
                }) { Text(stringResource(R.string.no)) }
            },
        )
    }


    AboutModal(
        open = showAboutDialogBox,
        onDismissRequest = { showAboutDialogBox = false }
    )

    BackupRestoreDialog(
        open = showBackupRestoreDialog,
        onBackup = {
            // Use the generated filename with timestamp
            backupLauncher.launch(backupFileName)
        },
        onRestore = {
            restoreLauncher.launch(arrayOf("application/octet-stream"))
        },
        onDismissRequest = {
            showBackupRestoreDialog = false
        }
    )

}