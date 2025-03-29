package com.a3.yearlyprogess

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContent
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.a3.yearlyprogess.ui.theme.YearlyProgressTheme

class WelcomeScreenV2 : ComponentActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    val pref = getSharedPreferences(MainActivity.YEARLY_PROGRESS_PREF, MODE_PRIVATE)
    val edit = pref.edit()

    setContent {
      YearlyProgressTheme {
        WelcomeScreen(
          onStartClick = {
            edit.putBoolean(MainActivity.FIRST_LAUNCH, false).apply()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
          })
      }
    }
  }
}

private const val TOS_URL = "https://www.a3group.co.in/yearly-progress/terms-of-service"
private const val PP_URL = "https://www.a3group.co.in/yearly-progress/privacy-policy"

@Composable
fun WelcomeScreen(onStartClick: () -> Unit) {
  Scaffold(contentWindowInsets = WindowInsets.safeContent) { innerPadding ->
    Column(
      modifier = Modifier
        .padding(innerPadding)
        .fillMaxSize(),
      verticalArrangement = Arrangement.SpaceBetween,
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(stringResource(R.string.app_name), style = MaterialTheme.typography.headlineLarge)
      }

      Image(painterResource(R.drawable.demoscreen), contentDescription = null, modifier = Modifier.padding(vertical = 32.dp).weight(1f))

      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        TermsAndPrivacyText(textAlignment = TextAlign.Center)
        Button(onStartClick) { Text("Start") }
      }
    }
  }
}

@Composable
fun TermsAndPrivacyText(
  initialMessage: String = "By clicking on start you agree to our ",
  textAlignment: TextAlign = TextAlign.Start,
  style: TextStyle = MaterialTheme.typography.bodySmall
) {
  val linkColor = MaterialTheme.colorScheme.primary
  Text(
    buildAnnotatedString {
      append(initialMessage)
      withLink(
        LinkAnnotation.Url(
          TOS_URL,
          TextLinkStyles(
            style =
            SpanStyle(color = linkColor, textDecoration = TextDecoration.Underline)
          )
        )
      ) {
        append("terms of service")
      }
      append(" and ")
      withLink(
        LinkAnnotation.Url(
          PP_URL,
          TextLinkStyles(
            style =
            SpanStyle(color = linkColor, textDecoration = TextDecoration.Underline)
          )
        )
      ) {
        append("privacy policy")
      }
      append(".")
    },
    textAlign = textAlignment,
    style = style
  )
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun WelcomeScreenPreview() {
  WelcomeScreen(onStartClick = {})
}
