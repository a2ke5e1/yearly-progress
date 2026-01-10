package com.a3.yearlyprogess.feature.backup_restore
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.a3.yearlyprogess.R


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupRestoreDialog(
    open: Boolean = false,
    onDismissRequest: () -> Unit,
    onBackup: () -> Unit,
    onRestore: () -> Unit,
) {
    if (!open) return
    BasicAlertDialog(onDismissRequest = onDismissRequest) {
        Box {
            BackupRestoreModalContent(
                onBackup = onBackup,
                onRestore = onRestore,
                onDismissRequest = onDismissRequest
            )
            IconButton(
                onClick = onDismissRequest, modifier = Modifier.align(Alignment.TopEnd),
            ) {
                Icon(
                    Icons.Default.Close, contentDescription = "Close about dialogbox",
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}


@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
@Preview
private fun BackupRestoreModalContent(
    onBackup: () -> Unit = {},
    onRestore: () -> Unit = {},
    onDismissRequest: () -> Unit = {},
) {
    Surface(
        modifier = Modifier.clip(MaterialTheme.shapes.medium),

    ) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
        ) {
            Text(
                text = stringResource(R.string.backup_restore),
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = stringResource(R.string.backup_restore_message),
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                modifier = Modifier.fillMaxWidth(),
                shapes = ButtonDefaults.shapes(
                    shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp, bottomStart = 4.dp, bottomEnd = 4.dp),
                    pressedShape = RoundedCornerShape(24.dp)
                ),
                onClick = {
                    onBackup()
                    onDismissRequest()
                }
            ) {
                Text(
                    text = stringResource(R.string.backup),
                    modifier = Modifier.padding(8.dp)
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Button(
                modifier = Modifier.fillMaxWidth(),
                shapes = ButtonDefaults.shapes(
                    shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp, bottomStart = 12.dp, bottomEnd =12.dp),
                    pressedShape = RoundedCornerShape(24.dp)
                ),
                onClick = {
                    onRestore()
                    onDismissRequest()
                }
            ) {
                Text(
                    text = stringResource(R.string.restore),
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    }
}