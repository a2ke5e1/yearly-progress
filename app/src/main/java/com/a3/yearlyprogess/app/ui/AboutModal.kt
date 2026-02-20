package com.a3.yearlyprogess.app.ui

import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.a3.yearlyprogess.BuildConfig
import com.a3.yearlyprogess.R
import com.a3.yearlyprogess.core.util.CommunityUtil
import androidx.core.net.toUri

data class Credits(
    val name: String,
    val username: String? = null,
    val language: String,
    val github: String? = null
) {
    fun getTelegramLink(): String? {
        return username?.let { "https://t.me/$it" }
    }

    fun getGithubLink(): String? {
        return github?.let { "https://github.com/$it" }
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutModal(
    open: Boolean = false,
    onDismissRequest: () -> Unit = {},
) {
    if (!open) return
    BasicAlertDialog(onDismissRequest = onDismissRequest) {
        Box {
            AboutModalContent()
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
@Preview
@Composable
private fun AboutModalContent() {
    val context = LocalContext.current
    val credits = listOf(
//        Credits("ASG13043", "ASG13043", "हिंदी"),
//        Credits("mojienjoyment", "mojienjoyment", "فارسی"),
        Credits("Matteo", "Sgattocuki", "Italian"),
        Credits(name = "Максим", language =  "Rusian", github = "gerasimov-mv")
    )

    Surface(
        modifier = Modifier.clip(MaterialTheme.shapes.largeIncreased),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Image(
                painter = painterResource(R.drawable.ic_launcher_foreground),
                contentDescription = "App icon",
                modifier = Modifier
                    .size(48.dp)
                    .clip(shape = MaterialShapes.Circle.toShape())
                    .background(color = Color.White)
            )
            Column(

            ) {
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = stringResource(R.string.app_version, BuildConfig.VERSION_NAME),
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(R.string.translation_credits),
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )

                Spacer(modifier = Modifier.height(4.dp))

                Column(
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    credits.forEach { credit ->
                        CreditsItem(credit = credit)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(R.string.open_source),
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
                Text(
                    text = stringResource(R.string.open_source_desc),
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        CommunityUtil.onViewSourceCode(context)
                    },
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    shapes = ButtonDefaults.shapes(
                        shape = RoundedCornerShape(10.dp),
                        pressedShape = RoundedCornerShape(16.dp)
                    ), colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer,
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        text = stringResource(R.string.view_source_code),
                        style = MaterialTheme.typography.labelMedium
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(R.string.for_support_and_feedback),
                    style = MaterialTheme.typography.labelSmall
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            CommunityUtil.onJoinSupportGroup(context)
                        },
                        contentPadding = PaddingValues(all = 16.dp),
                        shapes = ButtonDefaults.shapes(
                            shape = RoundedCornerShape(10.dp),
                            pressedShape = RoundedCornerShape(16.dp)
                        ), colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer,
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            painterResource(R.drawable.ic_telegram_app),
                            contentDescription = stringResource(R.string.join_telegram_group),
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Button(
                        onClick = {
                            CommunityUtil.onShare(context)
                        },
                        contentPadding = PaddingValues(all = 16.dp),
                        shapes = ButtonDefaults.shapes(
                            shape = RoundedCornerShape(10.dp),
                            pressedShape = RoundedCornerShape(16.dp)
                        ), colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer,
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            Icons.Outlined.Share,
                            contentDescription = stringResource(R.string.share),
                        )
                    }
                }

            }
        }
    }
}

@Composable
private fun CreditsItem(credit: Credits) {
    val context = LocalContext.current
    val link =  if (credit.username != null)  { credit.getTelegramLink() }
                else if (credit.github != null) { credit.getGithubLink() }
                else null


    Text(
        text = "${credit.name} - ${credit.language}",
        style = MaterialTheme.typography.bodySmall.copy(
            color = if (link != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            textDecoration = if (link != null) TextDecoration.Underline else TextDecoration.None
        ),
        modifier = Modifier.then(
            if (link != null) {
                Modifier.clickable {
                    val intent = Intent(Intent.ACTION_VIEW, link.toUri())
                    context.startActivity(intent)
                }
            } else {
                Modifier
            }
        )
    )
}