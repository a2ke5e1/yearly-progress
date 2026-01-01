package com.a3.yearlyprogess.feature.settings.ui

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.a3.yearlyprogess.R
import com.a3.yearlyprogess.core.ui.components.Switch
import com.a3.yearlyprogess.domain.model.City
import com.a3.yearlyprogess.feature.settings.ui.domain.model.LocationScreen
import com.a3.yearlyprogess.feature.settings.ui.domain.model.PermissionState
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun SettingsLocationScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val settings by viewModel.settings.collectAsState()
    val locationUiState by viewModel.locationUiState.collectAsState()
    val savedLocation by viewModel.savedLocation.collectAsState()
    val cities by viewModel.cities.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    val locationPermissionState = rememberPermissionState(
        permission = Manifest.permission.ACCESS_COARSE_LOCATION
    )

    // Handle permission state changes
    LaunchedEffect(locationPermissionState.status) {
        viewModel.refreshPermissionState()

        // If user requested permission and it's now granted, enable auto-detection
        if (locationUiState.permission.requestedByUser &&
            locationPermissionState.status.isGranted
        ) {
            viewModel.setAutomaticDetection(true)
            viewModel.detectCurrentLocation()
            viewModel.setPermissionRequestedByUser(false)
        }
    }

    // Auto-detect location when enabled and permission is granted
    LaunchedEffect(settings.automaticallyDetectLocation) {
        if (settings.automaticallyDetectLocation) {
            if (!locationPermissionState.status.isGranted) {
                viewModel.setAutomaticDetection(false)
            } else {
                viewModel.detectCurrentLocation()
            }
        }
    }

    // Handle error snackbar
    LaunchedEffect(locationUiState.screen) {
        if (locationUiState.screen is LocationScreen.Error) {
            snackbarHostState.showSnackbar(
                message = (locationUiState.screen as LocationScreen.Error).message
            )
            viewModel.clearError()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {

            // Current location display
            item {
                CurrentLocationCard(
                    savedLocation = savedLocation,
                    isDetecting = locationUiState.screen == LocationScreen.DetectingLocation,
                    onDetectLocation = { viewModel.detectCurrentLocation() },
                    onClearLocation = { viewModel.clearLocation() }
                )
            }

            // Auto-detect toggle
            item {
                AutoDetectLocationSwitch(
                    checked = settings.automaticallyDetectLocation,
                    permission = locationUiState.permission,
                    onCheckedChange = { enabled ->
                        if (enabled && !locationPermissionState.status.isGranted) {
                            viewModel.setPermissionRequestedByUser(true)
                            locationPermissionState.launchPermissionRequest()
                        } else {
                            viewModel.setAutomaticDetection(enabled)
                        }
                    }
                )
            }

            // Permission warnings
            item {
                PermissionWarnings(
                    permissionStatus = locationPermissionState.status,
                    permissionState = locationUiState.permission,
                    onOpenSettings = {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", context.packageName, null)
                        }
                        context.startActivity(intent)
                    }
                )
            }

            // Manual location section
            item {
                ManualLocationSection(
                    onSelectCity = { viewModel.openManualCityPicker() },
                    onEnterCoordinates = { viewModel.openManualCoordinates() }
                )
            }

            // Info text
            item {
                PoweredByInfoText()
            }
        }

        // Snackbar for errors
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        )
    }

    // Dialogs based on screen state
    when (locationUiState.screen) {
        LocationScreen.ManualCityPicker -> {
            CitySearchDialog(
                cities = cities,
                searchQuery = locationUiState.input.searchQuery,
                onSearchQueryChange = { query ->
                    viewModel.updateManualInput(searchQuery = query)
                },
                onCitySelected = { city ->
                    viewModel.saveCityLocation(city)
                },
                onDismiss = { viewModel.dismissDialog() },
                onSearchCities = { query -> viewModel.searchCities(query) }
            )
        }

        LocationScreen.ManualCoordinates -> {
            ManualCoordinatesDialog(
                latitude = locationUiState.input.latitude,
                longitude = locationUiState.input.longitude,
                onLatitudeChange = { lat ->
                    viewModel.updateManualInput(latitude = lat)
                },
                onLongitudeChange = { lon ->
                    viewModel.updateManualInput(longitude = lon)
                },
                onSave = { viewModel.saveManualCoordinates() },
                onDismiss = { viewModel.dismissDialog() },
                isValid = locationUiState.input.isValidCoordinates()
            )
        }

        else -> {
            // No dialog
        }
    }
}

@Composable
private fun AutoDetectLocationSwitch(
    checked: Boolean,
    permission: PermissionState,
    onCheckedChange: (Boolean) -> Unit
) {
    Switch(
        modifier = Modifier.padding(top = 8.dp),
        title = stringResource(R.string.automatically_detect_location),
        description = when {
            !permission.isGranted -> "Location permission is required to auto-detect your location"
            !permission.isLocationServiceEnabled -> "Location services are disabled on your device"
            else -> "Automatically detect your current location"
        },
        checked = checked,
        disabled = !permission.isGranted || !permission.isLocationServiceEnabled,
        onCheckedChange = onCheckedChange
    )
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun PermissionWarnings(
    permissionStatus: PermissionStatus,
    permissionState: PermissionState,
    onOpenSettings: () -> Unit
) {
    when (permissionStatus) {
        is PermissionStatus.Denied -> {
            if (!permissionStatus.shouldShowRationale) {
                // Permission permanently denied
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            "Location permission is denied. To enable auto-detection, please grant location permission in app settings.",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onErrorContainer
                            ),
                            textAlign = TextAlign.Center
                        )
                        Button(
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = MaterialTheme.colorScheme.onError
                            ),
                            onClick = onOpenSettings
                        ) {
                            Text("Open Settings")
                        }
                    }
                }
            }
        }

        is PermissionStatus.Granted -> {
            if (!permissionState.isLocationServiceEnabled) {
                // Permission granted but location service disabled
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            stringResource(R.string.location_settings_permission_disabled_message),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onErrorContainer
                            ),
                            textAlign = TextAlign.Center
                        )
                        Button(
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = MaterialTheme.colorScheme.onError
                            ),
                            onClick = onOpenSettings
                        ) {
                            Text(stringResource(R.string.open_settings))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CurrentLocationCard(
    savedLocation: com.a3.yearlyprogess.domain.model.Location?,
    isDetecting: Boolean,
    onDetectLocation: () -> Unit,
    onClearLocation: () -> Unit
) {
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = stringResource(R.string.current_location),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (isDetecting) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Detecting location...")
                }
            } else if (savedLocation != null) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (savedLocation.isManual) stringResource(R.string.manual_location) else stringResource(
                                    R.string.auto_detected
                                ),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${String.format("%.4f", savedLocation.latitude)}, ${
                                    String.format(
                                        "%.4f",
                                        savedLocation.longitude
                                    )
                                }",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        IconButton(onClick = onClearLocation) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "Clear location",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            } else {
                Text(
                    text = stringResource(R.string.no_location_set),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onDetectLocation,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isDetecting
            ) {
                Icon(
                    imageVector = Icons.Default.MyLocation,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (isDetecting) stringResource(R.string.detecting) else stringResource(R.string.detect_current_location))
            }
        }
    }
}

@Composable
private fun ManualLocationSection(
    onSelectCity: () -> Unit,
    onEnterCoordinates: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(R.string.manual_location),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.suggest_location_manually_message),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onSelectCity,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.select_city))
            }

            OutlinedButton(
                onClick = onEnterCoordinates,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.coordinates))
            }
        }
    }
}

@Composable
private fun CitySearchDialog(
    cities: List<City>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onCitySelected: (City) -> Unit,
    onDismiss: () -> Unit,
    onSearchCities: (String) -> List<City>
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    var searchResults by remember { mutableStateOf<List<City>>(emptyList()) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 600.dp),
            shape = AlertDialogDefaults.shape,
            color = AlertDialogDefaults.containerColor,
            tonalElevation = AlertDialogDefaults.TonalElevation,
        ) {
            Column(
            ) {
                Text(
                    text = stringResource(R.string.select_city),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { query ->
                        onSearchQueryChange(query)
                        searchResults = onSearchCities(query)
                    },
                    label = { Text(stringResource(R.string.search_city)) },
                    placeholder = { Text(stringResource(R.string.enter_city_name)) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = stringResource(R.string.search)
                        )
                    },
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Search
                    ),
                    keyboardActions = KeyboardActions(
                        onSearch = {
                            keyboardController?.hide()
                        }
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (searchQuery.isNotEmpty()) {
                    LazyColumn(
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        if (searchResults.isEmpty()) {
                            item {
                                Text(
                                    text = stringResource(R.string.no_cities_found),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(vertical = 16.dp)
                                )
                            }
                        } else {
                            itemsIndexed(searchResults) { index, city ->
                                CityListItem(
                                    city = city,
                                    onClick = { onCitySelected(city) }
                                )
                                if (index < searchResults.lastIndex) {
                                    HorizontalDivider()
                                }
                            }
                        }
                    }
                } else {
                    Text(
                        text = stringResource(R.string.start_typing_to_search_for_a_city),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(all = 16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.cancel))
                    }
                }
            }
        }
    }
}

@Composable
private fun ManualCoordinatesDialog(
    latitude: String,
    longitude: String,
    onLatitudeChange: (String) -> Unit,
    onLongitudeChange: (String) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit,
    isValid: Boolean
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = AlertDialogDefaults.shape,
            color = AlertDialogDefaults.containerColor,
            tonalElevation = AlertDialogDefaults.TonalElevation,
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = stringResource(R.string.enter_coordinates),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.coordinates_validation_message),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = latitude,
                    onValueChange = onLatitudeChange,
                    label = { Text(stringResource(R.string.latitude)) },
                    placeholder = { Text(stringResource(R.string.lat_example)) },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = latitude.isNotEmpty() && (latitude.toDoubleOrNull() == null ||
                            latitude.toDouble() !in -90.0..90.0)
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = longitude,
                    onValueChange = onLongitudeChange,
                    label = { Text(stringResource(R.string.longitude)) },
                    placeholder = { Text(stringResource(R.string.lon_example)) },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { if (isValid) onSave() }
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = longitude.isNotEmpty() && (longitude.toDoubleOrNull() == null ||
                            longitude.toDouble() !in -180.0..180.0)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(stringResource(R.string.cancel))
                    }
                    Button(
                        onClick = onSave,
                        enabled = isValid,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(stringResource(R.string.save))
                    }
                }
            }
        }
    }
}

@Composable
private fun CityListItem(
    city: City,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 16.dp)
    ) {
        Text(
            text = city.nameAscii,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = buildString {
                append(city.country)
                if (city.adminName.isNotEmpty() && city.adminName != city.nameAscii) {
                    append(" • ${city.adminName}")
                }
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = "${String.format("%.4f", city.latitude)}, ${
                String.format(
                    "%.4f",
                    city.longitude
                )
            }",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
    }
}

@Composable
fun PoweredByInfoText(
    textAlignment: TextAlign = TextAlign.Start,
    style: TextStyle = MaterialTheme.typography.bodyMedium
) {
    val linkColor = MaterialTheme.colorScheme.primary
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = Icons.Outlined.Info,
            contentDescription = "Info",
            tint = linkColor,
            modifier = Modifier.padding(top = 4.dp, end = 8.dp)
        )
        Text(
            text = buildAnnotatedString {
                append("Location data is from the World Cities Database. ")
                append("Sunrise/sunset data is provided by ")
                withLink(
                    LinkAnnotation.Url(
                        "https://api.sunrisesunset.io/",
                        TextLinkStyles(
                            style = SpanStyle(
                                color = linkColor,
                                textDecoration = TextDecoration.Underline
                            )
                        )
                    )
                ) {
                    append("SunriseSunset API")
                }
                append(".")
            },
            style = style,
            textAlign = textAlignment
        )
    }
}