package com.a3.yearlyprogess

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.a3.yearlyprogess.ui.components.ProgressCard
import com.a3.yearlyprogess.ui.theme.YearlyProgressTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    installSplashScreen()
    enableEdgeToEdge()
    setContent {
      YearlyProgressTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
          LazyColumn(
            contentPadding = innerPadding,
            verticalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            item { ProgressCard(timePeriod = TimePeriod.YEAR, refreshInterval = 200) }
            item { ProgressCard(timePeriod = TimePeriod.WEEK) }
            item { ProgressCard(timePeriod = TimePeriod.MONTH) }
            item { ProgressCard(timePeriod = TimePeriod.DAY, refreshInterval = 100) }
          }
        }
      }
    }
  }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
  Text(text = "Hello $name!", modifier = modifier)
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
  YearlyProgressTheme { Greeting("Android") }
}
