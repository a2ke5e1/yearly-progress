package com.a3.yearlyprogess.screens

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.a3.yearlyprogess.ui.theme.YearlyProgressTheme
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query


data class NominatimPlace(
  val display_name: String,
  val lat: String,
  val lon: String
)


interface NominatimApi {
  @GET("search")
  suspend fun searchPlaces(
    @Query("q") query: String,
    @Query("format") format: String = "json",
    @Query("limit") limit: Int = 5
  ): List<NominatimPlace>
}

object NominatimService {
  private const val BASE_URL = "https://nominatim.openstreetmap.org/"

  val api: NominatimApi by lazy {
    Retrofit.Builder()
      .baseUrl(BASE_URL)
      .addConverterFactory(GsonConverterFactory.create())
      .build()
      .create(NominatimApi::class.java)
  }
}

class LocationSelectionScreen : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      YearlyProgressTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
          LocationSearchWithNominatim(
            innerPadding = innerPadding
          )
        }
      }
    }
  }
}

@Composable
fun LocationSearchWithNominatim(innerPadding: PaddingValues) {
  var query by remember { mutableStateOf("") }
  var suggestions by remember { mutableStateOf<List<NominatimPlace>>(emptyList()) }
  var selectedLocation by remember { mutableStateOf<NominatimPlace?>(null) }
  var isLoading by remember { mutableStateOf(false) } // Track loading state

  // Use a state to keep track of the last query value
  val queryState = rememberUpdatedState(query)
  val coroutineScope = rememberCoroutineScope()
  val keyboardController = LocalSoftwareKeyboardController.current


  // Handle debouncing with delay
  LaunchedEffect(query) {
    if (query.isNotEmpty()) {
      // Delay the search for 500 milliseconds after the user stops typing
      kotlinx.coroutines.delay(500)
      if (query == queryState.value) {
        try {
          isLoading = true // Start loading
          suggestions = NominatimService.api.searchPlaces(query)
        } catch (e: Exception) {
          suggestions = emptyList()
        } finally {
          isLoading = false // Stop loading
        }
      }
    } else {
      suggestions = emptyList()
    }
  }

  Column(modifier = Modifier.padding(innerPadding)) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      OutlinedTextField(
        value = query,
        onValueChange = { query = it },
        label = { Text("Search Location") },
        modifier = Modifier.weight(1f),
        keyboardOptions = KeyboardOptions.Default.copy(
          imeAction = ImeAction.Search
        ),
        keyboardActions = KeyboardActions(
          onSearch = {
            coroutineScope.launch {
              isLoading = true
              try {
                suggestions = NominatimService.api.searchPlaces(query)
              } catch (e: Exception) {
                suggestions = emptyList()
              } finally {
                isLoading = false
              }
            }
            keyboardController?.hide()
          }
        )
      )

      Spacer(modifier = Modifier.width(8.dp))

      FilledIconButton(
        onClick = {
          coroutineScope.launch {
            isLoading = true
            try {
              suggestions = NominatimService.api.searchPlaces(query)
            } catch (e: Exception) {
              suggestions = emptyList()
            } finally {
              isLoading = false
            }
          }
        },
        enabled = query.isNotEmpty() && !isLoading
      ) {
        Icon(Icons.Default.Search, contentDescription = "Search")
      }
    }

    if (isLoading) {
      // Show loading indication
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
      ) {
        CircularProgressIndicator()
      }
    }

    LazyColumn {
      itemsIndexed(suggestions) { _, place ->
        Text(
          text = place.display_name,
          modifier = Modifier
            .fillMaxWidth()
            .clickable {
              query = place.display_name
              selectedLocation = place
              suggestions = emptyList()
            }
            .padding(8.dp)
        )
      }
    }

    if (selectedLocation != null) {
      Spacer(modifier = Modifier.height(16.dp))
      Text(text = selectedLocation.toString(), fontWeight = FontWeight.Bold)
    }
  }
}



