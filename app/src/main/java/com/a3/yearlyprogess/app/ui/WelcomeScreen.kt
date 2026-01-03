package com.a3.yearlyprogess.app.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.a3.yearlyprogess.R

private const val TOS_URL = "https://www.a3group.co.in/yearly-progress/terms-of-service"
private const val PP_URL = "https://www.a3group.co.in/yearly-progress/privacy-policy"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WelcomeScreen(
    onStartClicked: () -> Unit
) {
    var isProcessing by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("") },
                actions = {

                })
        },
        contentWindowInsets = WindowInsets.safeContent,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(stringResource(R.string.app_name), style = MaterialTheme.typography.headlineLarge)
            }

            Image(
                painterResource(R.drawable.demoscreen),
                contentDescription = null,
                modifier = Modifier
                    .padding(vertical = 32.dp)
                    .weight(1f)
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 32.dp)
            ) {
                TermsAndPrivacyText(
                    textAlignment = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
                if (isProcessing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(32.dp),
                    )
                } else {
                    Button(
                        enabled = !isProcessing,
                        onClick = {
                            if (!isProcessing) {
                                isProcessing = true
                                onStartClicked()
                            }
                        }
                ) {
                        Text(stringResource(R.string.start))
                    }
                }
            }
        }
    }
}

@Composable
fun TermsAndPrivacyText(
    modifier: Modifier = Modifier,
    textAlignment: TextAlign = TextAlign.Start,
    style: TextStyle = MaterialTheme.typography.bodyMedium
) {
    val linkColor = MaterialTheme.colorScheme.primary

    // Define the style for the clickable links
    val linkStyles = remember(linkColor) {
        TextLinkStyles(
            style = SpanStyle(
                color = linkColor,
                textDecoration = TextDecoration.Underline
            )
        )
    }

    // Convert the HTML string into an AnnotatedString
    val annotatedString = AnnotatedString.fromHtml(
        htmlString = stringResource(R.string.terms_and_privacy_message, TOS_URL, PP_URL),
        linkStyles = linkStyles
    )

    Text(
        text = annotatedString,
        textAlign = textAlignment,
        style = style,
        modifier = modifier
    )
}
