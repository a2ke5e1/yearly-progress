package com.a3.yearlyprogess.feature.events.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.a3.yearlyprogess.core.ui.components.Switch
import com.a3.yearlyprogess.feature.events.domain.model.Event
import com.a3.yearlyprogess.feature.events.domain.model.RepeatDays
import com.a3.yearlyprogess.feature.events.presentation.EventViewModel
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventCreateScreen(
    eventId: Int? = null,
    viewModel: EventViewModel = hiltViewModel(),
    onNavigateUp: () -> Unit,
) {
    val context = LocalContext.current

    var eventTitle by remember { mutableStateOf("") }
    var eventDescription by remember { mutableStateOf("") }
    var isAllDay by remember { mutableStateOf(false) }
    var startDateTime by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var endDateTime by remember { mutableLongStateOf(System.currentTimeMillis() + 24 * 60 * 60 * 1000L) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var isStartDateTime by remember { mutableStateOf(true) }
    var repeatEveryYear by remember { mutableStateOf(false) }
    var repeatEveryMonth by remember { mutableStateOf(false) }
    var repeatWeekdays by remember { mutableStateOf(false) }
    var selectedWeekdays by remember { mutableStateOf(setOf<RepeatDays>()) }
    var showError by remember { mutableStateOf(false) }
    var savedImagePath by remember { mutableStateOf<String?>(null) }
//    var selectedImageBitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }

    val isEditMode = eventId != null

    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                inputStream?.use { stream ->
                    val fileName = "image_${System.currentTimeMillis()}.jpg"
                    val file = File(context.filesDir, fileName)
                    FileOutputStream(file).use { output ->
                        stream.copyTo(output)
                    }

                    savedImagePath = file.absolutePath
//                    selectedImageBitmap = BitmapFactory.decodeFile(savedImagePath)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Load event data if editing
    LaunchedEffect(eventId) {
        if (eventId != null) {
            viewModel.getEvent(eventId)?.let { event ->
                eventTitle = event.eventTitle
                eventDescription = event.eventDescription
                isAllDay = event.allDayEvent
                startDateTime = event.eventStartTime.time
                endDateTime = event.eventEndTime.time
                repeatEveryYear = event.repeatEventDays.contains(RepeatDays.EVERY_YEAR)
                repeatEveryMonth = event.repeatEventDays.contains(RepeatDays.EVERY_MONTH)
                repeatWeekdays = event.hasWeekDays
                selectedWeekdays = event.repeatEventDays.filter {
                    it != RepeatDays.EVERY_YEAR && it != RepeatDays.EVERY_MONTH
                }.toSet()

                event.backgroundImageUri?.let { path ->
                    val file = File(path)
                    if (file.exists()) {
                        savedImagePath = path
//                        selectedImageBitmap = BitmapFactory.decodeFile(path)
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditMode) "Edit Event" else "Create Event") },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (eventTitle.isBlank()) {
                        showError = true
                        return@FloatingActionButton
                    }

                    val repeatDays = mutableListOf<RepeatDays>()
                    if (repeatEveryYear) repeatDays.add(RepeatDays.EVERY_YEAR)
                    if (repeatEveryMonth) repeatDays.add(RepeatDays.EVERY_MONTH)
                    if (repeatWeekdays) repeatDays.addAll(selectedWeekdays)

                    val event = Event(
                        id = eventId ?: 0,
                        eventTitle = eventTitle,
                        eventDescription = eventDescription,
                        allDayEvent = isAllDay,
                        eventStartTime = Date(startDateTime),
                        eventEndTime = Date(endDateTime),
                        repeatEventDays = repeatDays,
                        hasWeekDays = repeatWeekdays,
                        backgroundImageUri = savedImagePath
                    )

                    if (isEditMode) {
                        viewModel.updateEvent(event)
                    } else {
                        viewModel.addEvent(event)
                    }
                    onNavigateUp()
                }
            ) {
                Icon(Icons.Filled.Save, contentDescription = "Save")
            }
        },
        contentWindowInsets = WindowInsets.safeDrawing,
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Title Section
            OutlinedTextField(
                value = eventTitle,
                onValueChange = {
                    eventTitle = it
                    showError = false
                },
                label = { Text("Event Title") },
                placeholder = { Text("Enter event title") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                isError = showError,
                supportingText = if (showError) {
                    { Text("Event title is required") }
                } else null,
                singleLine = true
            )

            // Description Section
            OutlinedTextField(
                value = eventDescription,
                onValueChange = { eventDescription = it },
                label = { Text("Description") },
                placeholder = { Text("Enter event description (optional)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(120.dp),
                maxLines = 5
            )

            // Background Image Section
            Text(
                text = "Background Image (Optional)",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            if (savedImagePath != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .height(200.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(savedImagePath)
                                .crossfade(true)
                                .build(),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(12.dp))
                        )

                        // Remove image button
                        IconButton(
                            onClick = {
                                savedImagePath = null
//                                selectedImageBitmap = null
                            },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Remove image",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            } else {
                OutlinedCard(
                    onClick = {
                        imagePickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .height(120.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AddPhotoAlternate,
                            contentDescription = "Add image",
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Tap to select background image",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // All Day Switch
                Switch(
                    title = "All Day Event",
                    checked = isAllDay,
                    onCheckedChange = { isAllDay = it }
                )


            // Date & Time Section
            Text(
                text = "Date & Time",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            // Start Date/Time
            OutlinedCard(
                onClick = {
                    isStartDateTime = true
                    showDatePicker = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Start", style = MaterialTheme.typography.labelMedium)
                        Text(
                            formatDateTime(startDateTime, isAllDay),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }

            // End Date/Time
            OutlinedCard(
                onClick = {
                    isStartDateTime = false
                    showDatePicker = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("End", style = MaterialTheme.typography.labelMedium)
                        Text(
                            formatDateTime(endDateTime, isAllDay),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }

            // Repeat Section
            Text(
                text = "Repeat",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Switch(
                title = "Every Year", checked = repeatEveryYear, onCheckedChange = {
                    repeatEveryYear = it
                    if (it) {
                        repeatEveryMonth = false
                        repeatWeekdays = false
                    }
                })

            Switch(
                title = "Every Month", checked = repeatEveryMonth, onCheckedChange = {
                    repeatEveryMonth = it
                    if (it) {
                        repeatEveryYear = false
                        repeatWeekdays = false
                    }
                })

            Switch(
                title = "On Weekdays", checked = repeatWeekdays, onCheckedChange = {
                    repeatWeekdays = it
                    if (it) {
                        repeatEveryYear = false
                        repeatEveryMonth = false
                    }
                })


            // Weekday Selection
            AnimatedVisibility(
                visible = repeatWeekdays,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                WeekdaySelector(
                    selectedWeekdays = selectedWeekdays,
                    onWeekdayToggle = { day ->
                        selectedWeekdays = if (selectedWeekdays.contains(day)) {
                            selectedWeekdays - day
                        } else {
                            selectedWeekdays + day
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    // Date Picker Dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = if (isStartDateTime) startDateTime else endDateTime
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { selected ->
                            if (isStartDateTime) {
                                startDateTime = selected
                            } else {
                                endDateTime = selected
                            }
                        }
                        showDatePicker = false
                        if (!isAllDay) {
                            showTimePicker = true
                        }
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // Time Picker Dialog
    if (showTimePicker && !isAllDay) {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = if (isStartDateTime) startDateTime else endDateTime

        val timePickerState = rememberTimePickerState(
            initialHour = calendar.get(Calendar.HOUR_OF_DAY),
            initialMinute = calendar.get(Calendar.MINUTE),
            is24Hour = true
        )

        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        calendar.set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                        calendar.set(Calendar.MINUTE, timePickerState.minute)

                        if (isStartDateTime) {
                            startDateTime = calendar.timeInMillis
                        } else {
                            endDateTime = calendar.timeInMillis
                        }
                        showTimePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("Cancel")
                }
            },
            text = {
                TimePicker(state = timePickerState)
            }
        )
    }
}

@Composable
fun WeekdaySelector(
    selectedWeekdays: Set<RepeatDays>,
    onWeekdayToggle: (RepeatDays) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            WeekdayChip("S", RepeatDays.SUNDAY, selectedWeekdays, onWeekdayToggle, Modifier.weight(1f))
            WeekdayChip("M", RepeatDays.MONDAY, selectedWeekdays, onWeekdayToggle, Modifier.weight(1f))
            WeekdayChip("T", RepeatDays.TUESDAY, selectedWeekdays, onWeekdayToggle, Modifier.weight(1f))
            WeekdayChip("W", RepeatDays.WEDNESDAY, selectedWeekdays, onWeekdayToggle, Modifier.weight(1f))
            WeekdayChip("T", RepeatDays.THURSDAY, selectedWeekdays, onWeekdayToggle, Modifier.weight(1f))
            WeekdayChip("F", RepeatDays.FRIDAY, selectedWeekdays, onWeekdayToggle, Modifier.weight(1f))
            WeekdayChip("S", RepeatDays.SATURDAY, selectedWeekdays, onWeekdayToggle, Modifier.weight(1f))
        }
    }
}

@Composable
fun WeekdayChip(
    label: String,
    day: RepeatDays,
    selectedWeekdays: Set<RepeatDays>,
    onWeekdayToggle: (RepeatDays) -> Unit,
    modifier: Modifier = Modifier
) {
    FilterChip(
        selected = selectedWeekdays.contains(day),
        onClick = { onWeekdayToggle(day) },
        label = { Text(label) },
        modifier = modifier
    )
}

private fun formatDateTime(timeInMillis: Long, isAllDay: Boolean): String {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())

    return if (isAllDay) {
        dateFormat.format(Date(timeInMillis))
    } else {
        "${dateFormat.format(Date(timeInMillis))} at ${timeFormat.format(Date(timeInMillis))}"
    }
}