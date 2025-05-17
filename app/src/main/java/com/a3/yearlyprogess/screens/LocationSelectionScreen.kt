package com.a3.yearlyprogess.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.a3.yearlyprogess.R
import com.a3.yearlyprogess.components.dialogbox.PermissionRationalDialog
import com.a3.yearlyprogess.invalidateCachedSunriseSunset
import com.a3.yearlyprogess.ui.theme.YearlyProgressTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.gson.Gson
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

data class NominatimPlace(val display_name: String, val lat: String, val lon: String)

data class UserLocationPref(
    val automaticallyDetectLocation: Boolean = true,
    val userLocationPref: NominatimPlace? = null
) {

  companion object {

    private const val USER_LOCATION_PREF = "USER_LOCATION_PREF"

    fun load(context: Context): UserLocationPref {
      val sharedPreferences = context.getSharedPreferences(USER_LOCATION_PREF, Context.MODE_PRIVATE)
      val jsonString =
          sharedPreferences.getString(USER_LOCATION_PREF, null)
              ?: return UserLocationPref(true, null)
      return Gson().fromJson(jsonString, UserLocationPref::class.java)
    }

    fun save(context: Context, config: UserLocationPref) {
      val sharedPreferences = context.getSharedPreferences(USER_LOCATION_PREF, Context.MODE_PRIVATE)
      val edit = sharedPreferences.edit()
      val jsonString = Gson().toJson(config)
      edit.putString(USER_LOCATION_PREF, jsonString).apply()
    }
  }
}

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
  @OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      val locationPermissionState =
          rememberPermissionState(permission = Manifest.permission.ACCESS_COARSE_LOCATION)
      var showPermissionRationalMessage by rememberSaveable { mutableStateOf(true) }
      val context = LocalContext.current
      val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
      var automaticallyDetectLocation by rememberSaveable { mutableStateOf(false) }
      var requestedLocationPermissionByUser by rememberSaveable { mutableStateOf(false) }
      var selectedLocation by rememberSaveable { mutableStateOf<NominatimPlace?>(null) }
      var showManualSelectionDialogBox by rememberSaveable { mutableStateOf(false) }

      LaunchedEffect(Unit) {
        val pref = UserLocationPref.load(context)
        automaticallyDetectLocation = pref.automaticallyDetectLocation
        selectedLocation = pref.userLocationPref

        if (!locationPermissionState.status.isGranted) {
          automaticallyDetectLocation = false
        }
      }

      LaunchedEffect(locationPermissionState, requestedLocationPermissionByUser) {
        if (requestedLocationPermissionByUser && locationPermissionState.status.isGranted) {
          automaticallyDetectLocation = true
        }
      }

      LaunchedEffect(automaticallyDetectLocation) {
        UserLocationPref.save(
            context,
            UserLocationPref(
                automaticallyDetectLocation = automaticallyDetectLocation,
                userLocationPref = selectedLocation))
        if (automaticallyDetectLocation && !locationPermissionState.status.isGranted) {
          automaticallyDetectLocation = false
          locationPermissionState.launchPermissionRequest()
        }
      }

      YearlyProgressTheme {
        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection).fillMaxSize(),
            topBar = {
              CenterAlignedTopAppBar(
                  title = {
                    Text(
                        text = stringResource(R.string.manage_location_title),
                    )
                  },
                  scrollBehavior = scrollBehavior,
                  navigationIcon = {
                    IconButton(onClick = { finish() }) {
                      Icon(
                          Icons.AutoMirrored.Default.ArrowBack,
                          contentDescription = stringResource(R.string.go_back))
                    }
                  })
            },
            contentWindowInsets = WindowInsets.safeDrawing,
        ) { innerPadding ->
          LazyColumn(
              contentPadding = innerPadding,
              verticalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            item {
              val automaticLocationInteractionSource = remember { MutableInteractionSource() }
              Row(
                  modifier =
                      Modifier.fillMaxWidth().padding(horizontal = 16.dp).clickable(
                          interactionSource = automaticLocationInteractionSource,
                          indication = null) {
                            automaticallyDetectLocation = !automaticallyDetectLocation
                            if (automaticallyDetectLocation) {
                              requestedLocationPermissionByUser = true
                            }
                          },
                  verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        stringResource(R.string.automatically_detect_location),
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyLarge)

                    Switch(
                        checked = automaticallyDetectLocation,
                        onCheckedChange = {
                          automaticallyDetectLocation = it
                          if (it) {
                            requestedLocationPermissionByUser = true
                          }
                        },
                        interactionSource = automaticLocationInteractionSource)
                  }
            }

            when (val status = locationPermissionState.status) {
              is PermissionStatus.Denied -> {
                item {
                  if (status.shouldShowRationale) {
                    if (showPermissionRationalMessage) {
                      PermissionRationalDialog(
                          onDismiss = {
                            showPermissionRationalMessage = false
                            automaticallyDetectLocation = false
                          },
                          onConfirm = { locationPermissionState.launchPermissionRequest() },
                          iconPainter = painterResource(R.drawable.ic_location_on_24),
                          title = stringResource(R.string.location_permission_title),
                          body = stringResource(R.string.location_permission_message))
                    }
                  } else {
                    Card(
                        colors =
                            CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer),
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                          Column(
                              modifier = Modifier.padding(16.dp).fillMaxWidth(),
                              horizontalAlignment = Alignment.CenterHorizontally,
                              verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                Text(
                                    stringResource(R.string.auto_location_message),
                                    style =
                                        MaterialTheme.typography.bodyLarge.copy(
                                            color = MaterialTheme.colorScheme.onErrorContainer))
                                Button(
                                    colors =
                                        ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.error,
                                            contentColor = MaterialTheme.colorScheme.onError),
                                    onClick = {
                                      val intent =
                                          Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                              .apply {
                                                data =
                                                    Uri.fromParts(
                                                        "package", context.packageName, null)
                                              }
                                      context.startActivity(intent)
                                    }) {
                                      Text(stringResource(R.string.open_settings))
                                    }
                              }
                        }
                  }
                }
              }

              is PermissionStatus.Granted -> {}
            }

            item {
              val isClickable = !automaticallyDetectLocation
              Column(
                modifier = Modifier
                  .fillMaxWidth()
                  .then(
                    if (isClickable) Modifier.clickable { showManualSelectionDialogBox = true }
                    else Modifier
                  )
                  .alpha(if (isClickable) 1f else 0.5f)
              ) {
                Column(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                  Text(
                    text = stringResource(R.string.manual_location),
                    style = MaterialTheme.typography.labelLarge.copy(
                      color = MaterialTheme.colorScheme.primary
                    )
                  )
                  Text(
                    text = selectedLocation?.display_name
                      ?: stringResource(R.string.enter_a_location),
                    style = MaterialTheme.typography.bodyLarge
                  )
                }
              }
            }
          }

          if (showManualSelectionDialogBox) {
            LocationSearchWithNominatim(
                innerPadding = PaddingValues(8.dp),
                onSelected = { place ->
                  selectedLocation = place
                  invalidateCachedSunriseSunset(context)
                  UserLocationPref.save(
                      context,
                      UserLocationPref(
                          automaticallyDetectLocation = automaticallyDetectLocation,
                          userLocationPref = place))
                },
                onDismiss = { showManualSelectionDialogBox = false })
          }
        }
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationSearchWithNominatim(
    innerPadding: PaddingValues,
    onSelected: (NominatimPlace) -> Unit,
    onDismiss: () -> Unit
) {
  var query by remember { mutableStateOf("") }
  var suggestions by remember { mutableStateOf<List<NominatimPlace>>(emptyList()) }
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

  Dialog(
      onDismissRequest = onDismiss,
      content = {
        Column(
            modifier =
                Modifier.padding(innerPadding)
                    .heightIn(min = 400.dp)
                    .background(
                        color = AlertDialogDefaults.containerColor,
                        shape = AlertDialogDefaults.shape),
            verticalArrangement = Arrangement.SpaceBetween) {
              Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp)) {
                      OutlinedTextField(
                          value = query,
                          onValueChange = { query = it },
                          label = { Text("Search Location") },
                          modifier = Modifier.weight(1f),
                          keyboardOptions =
                              KeyboardOptions.Default.copy(imeAction = ImeAction.Search),
                          keyboardActions =
                              KeyboardActions(
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
                                  }))

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
                          enabled = query.isNotEmpty() && !isLoading) {
                            Icon(Icons.Default.Search, contentDescription = "Search")
                          }
                    }

                LazyColumn(modifier = Modifier.padding(horizontal = 16.dp)) {
                  itemsIndexed(suggestions) { _, place ->
                    Text(
                        text = place.display_name,
                        modifier =
                            Modifier.fillMaxWidth()
                                .clickable {
                                  query = place.display_name
                                  onSelected(place)
                                  suggestions = emptyList()
                                  onDismiss()
                                }
                                .padding(8.dp))
                  }
                }
              }
              if (isLoading) {
                // Show loading indication
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center) {
                      CircularProgressIndicator()
                    }
              }

              FilledTonalButton(
                  onClick = { onDismiss() },
                  modifier = Modifier.padding(all = 16.dp).fillMaxWidth()) {
                    Text(stringResource(R.string.dismiss))
                  }
            }
      },
  )
}
