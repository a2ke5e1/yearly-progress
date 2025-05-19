package com.a3.yearlyprogess

import android.app.Activity.MODE_PRIVATE
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
import androidx.preference.PreferenceManager
import com.a3.yearlyprogess.components.dialogbox.CalendarType
import com.a3.yearlyprogess.components.dialogbox.ListSelectorDialogBox
import com.a3.yearlyprogess.ui.theme.YearlyProgressTheme

class WelcomeScreenV2 : ComponentActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    setContent { YearlyProgressTheme { WelcomeScreen() } }
  }
}

private const val TOS_URL = "https://www.a3group.co.in/yearly-progress/terms-of-service"
private const val PP_URL = "https://www.a3group.co.in/yearly-progress/privacy-policy"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WelcomeScreen() {
  val context = LocalContext.current

  val settingPref = PreferenceManager.getDefaultSharedPreferences(context)
  val pref = context.getSharedPreferences(MainActivity.YEARLY_PROGRESS_PREF, MODE_PRIVATE)

  val edit = pref.edit()
  val editSettingsPref = settingPref.edit()

  val calendarEntries = context.resources.getStringArray(R.array.app_calendar_type_entries)
  val calendarValues = context.resources.getStringArray(R.array.app_calendar_type_values)

  val calendarTypes =
      calendarEntries.zip(calendarValues) { name, value -> CalendarType(name, value) }

  var showDialog by remember { mutableStateOf(false) }
  var selectedType by remember { mutableStateOf(calendarTypes.first()) }

  Scaffold(
      topBar = {
        TopAppBar(
            title = { Text("") },
            actions = {
              TextButton(onClick = { showDialog = true }) {
                Text(text = "${selectedType.name}")
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    painterResource(R.drawable.ic_outline_edit_calendar_24),
                    contentDescription = null)
              }
            })
      },
      contentWindowInsets = WindowInsets.safeContent,
  ) { innerPadding ->
    Column(
        modifier = Modifier.padding(innerPadding).fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(stringResource(R.string.app_name), style = MaterialTheme.typography.headlineLarge)
          }

          Image(
              painterResource(R.drawable.demoscreen),
              contentDescription = null,
              modifier = Modifier.padding(vertical = 32.dp).weight(1f))

          Column(
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.spacedBy(8.dp)) {
                TermsAndPrivacyText(textAlignment = TextAlign.Center)
                Button(
                    onClick = {
                      edit.putBoolean(MainActivity.FIRST_LAUNCH, false).apply()
                      editSettingsPref
                          .putString(
                              context.getString(R.string.app_calendar_type), selectedType.code)
                          .apply()
                      context.startActivity(Intent(context, MainActivity::class.java))
                      (context as ComponentActivity).finish()
                    }) {
                      Text("Start")
                    }
              }

          if (showDialog) {
            ListSelectorDialogBox(
              items = calendarTypes,
              selectedItem = selectedType,
              onItemSelected = { index, it ->
                selectedType = it
                showDialog = false
              },
              renderItem = {
                Text(it.name)
              },
              onDismiss = { showDialog = false },
              title = stringResource(R.string.select_your_calendar_system)
            )
          }
        }
  }
}

@Composable
fun TermsAndPrivacyText(
    initialMessage: String = "By clicking on start you agree to our ",
    textAlignment: TextAlign = TextAlign.Start,
    style: TextStyle = MaterialTheme.typography.bodyMedium
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
                        SpanStyle(color = linkColor, textDecoration = TextDecoration.Underline)))) {
              append("terms of service")
            }
        append(" and ")
        withLink(
            LinkAnnotation.Url(
                PP_URL,
                TextLinkStyles(
                    style =
                        SpanStyle(color = linkColor, textDecoration = TextDecoration.Underline)))) {
              append("privacy policy")
            }
        append(".")
      },
      textAlign = textAlignment,
      style = style)
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun WelcomeScreenPreview() {
  WelcomeScreen()
}
