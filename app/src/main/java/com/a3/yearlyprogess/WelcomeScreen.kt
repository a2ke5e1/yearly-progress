package com.a3.yearlyprogess

import android.app.Activity.MODE_PRIVATE
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.a3.yearlyprogess.ui.theme.YearlyProgressTheme

class WelcomeScreenV2 : ComponentActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()



    setContent {
      YearlyProgressTheme {
        WelcomeScreen(
         )
      }
    }
  }
}

private const val TOS_URL = "https://www.a3group.co.in/yearly-progress/terms-of-service"
private const val PP_URL = "https://www.a3group.co.in/yearly-progress/privacy-policy"

@Composable
fun WelcomeScreen() {
  val context = LocalContext.current
  val settingPref = PreferenceManager.getDefaultSharedPreferences(context)
  val pref = context.getSharedPreferences(MainActivity.YEARLY_PROGRESS_PREF, MODE_PRIVATE)
  val edit = pref.edit()
  val editSettingsPref = settingPref.edit()

  Scaffold(contentWindowInsets = WindowInsets.safeContent) { innerPadding ->
    var showDialog by remember { mutableStateOf(false) }
    var selectedType by remember { mutableStateOf(calendarTypes.first()) }



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

      Image(painterResource(R.drawable.demoscreen), contentDescription = null, modifier = Modifier
        .padding(vertical = 32.dp)
        .weight(1f))

      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        TermsAndPrivacyText(textAlignment = TextAlign.Center)
        Button(onClick = {
          edit.putBoolean(MainActivity.FIRST_LAUNCH, false).apply()
          editSettingsPref.putString(context.getString(R.string.app_calendar_type), selectedType.code).apply()
          context.startActivity(Intent(context, MainActivity::class.java))
          (context as ComponentActivity).finish()
        }) { Text("Start") }
      }


      Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Button(onClick = { showDialog = true }) {
          Text(text = "Select Calendar Type")
        }

        selectedType?.let {
          Text(text = "Selected: $it", modifier = Modifier.padding(top = 16.dp))
        }

        if (showDialog) {
          CalendarTypeDialog(
            calendarTypes = calendarTypes,
            selectedType = selectedType,
            onTypeSelected = {
              selectedType = it
              showDialog = false
            },
            onDismiss = { showDialog = false }
          )
        }
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


data class CalendarType(
  val name: String,
  val code: String
)

val calendarTypes = listOf(
  CalendarType("Gregorian", "gregorian"),
  CalendarType("Buddhist", "buddhist"),
  CalendarType("Chinese", "chinese"),
  CalendarType("Coptic", "coptic"),
  CalendarType("Dangi", "dangi"),
  CalendarType("Ethiopic", "ethiopic"),
  CalendarType("Ethiopic Amete Alem", "ethiopic-amete-alem"),
  CalendarType("Hebrew", "hebrew"),
  CalendarType("Indian", "indian"),
  CalendarType("Islamic", "islamic"),
  CalendarType("Islamic Civil", "islamic-civil"),
  CalendarType("Islamic TBLA", "islamic-tbla"),
  CalendarType("Islamic Umm al-Qura", "islamic-umalqura"),
  CalendarType("Islamic RGSA", "islamic-rgsa"),
  CalendarType("ISO 8601", "iso8601"),
  CalendarType("Japanese", "japanese"),
  CalendarType("Persian", "persian"),
  CalendarType("Republic of China (ROC)", "roc")
)


@Composable
fun CalendarTypeDialog(
  calendarTypes: List<CalendarType>,
  selectedType: CalendarType?,
  onTypeSelected: (CalendarType) -> Unit,
  onDismiss: () -> Unit
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    modifier = Modifier.heightIn(max = 600.dp),
    title = { Text(text = "Choose Calendar Type") },
    text = {
      LazyColumn {
        itemsIndexed(calendarTypes) { index, type ->
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clickable { onTypeSelected(type) },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            RadioButton(
              selected = type == selectedType,
              onClick = { onTypeSelected(type) }
            )
            Text(text = type.name)
          }
        }
      }
    },
    confirmButton = {
      TextButton(onClick = onDismiss) {
        Text(stringResource(R.string.close))
      }
    }
  )
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun WelcomeScreenPreview() {
  WelcomeScreen()
}
