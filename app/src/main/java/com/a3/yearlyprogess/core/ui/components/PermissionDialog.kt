package com.a3.yearlyprogess.core.ui.components

import android.view.HapticFeedbackConstants
import android.view.SoundEffectConstants
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.a3.yearlyprogess.R

@Composable
fun PermissionDialog(
    icon: ImageVector,
    title: String,
    description: String,
    bulletPoints: Array<String> = emptyArray<String>(),
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    confirmButtonText: String = stringResource(R.string.allow),
    dismissButtonText: String = stringResource(R.string.not_now)
) {
    val view = LocalView.current

    AlertDialog(
        onDismissRequest = {
            view.playSoundEffect(SoundEffectConstants.CLICK)
            view.performHapticFeedback(HapticFeedbackConstants.REJECT)
            onDismiss()
        },
        icon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            BulletedDescription(
                description = description,
                bulletPoints = bulletPoints.toList(),
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    view.playSoundEffect(SoundEffectConstants.CLICK)
                    view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                    onConfirm()
                }
            ) {
                Text(
                    text = confirmButtonText,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    view.playSoundEffect(SoundEffectConstants.CLICK)
                    view.performHapticFeedback(HapticFeedbackConstants.REJECT)
                    onDismiss()
                }
            ) {
                Text(
                    text = dismissButtonText,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp
    )
}

@Composable
fun BulletedDescription(
    description: String,
    bulletPoints: List<String>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Extract style to a variable for readability
        val bulletStyle = MaterialTheme.typography.bodyMedium.copy(
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        bulletPoints.forEach { bullet ->
            key(bullet) {
                Row(
                    modifier = Modifier
                        .padding(vertical = 4.dp)
                        .semantics(mergeDescendants = true) {}
                ) {
                    Text(
                        text = "•",
                        style = bulletStyle,
                        modifier = Modifier.width(12.dp)
                    )
                    Text(
                        text = bullet,
                        style = bulletStyle,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}
