package com.a3.yearlyprogess.feature.events.ui

import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.a3.yearlyprogess.R
import com.a3.yearlyprogess.core.ui.components.ExpandableSection
import com.a3.yearlyprogess.core.ui.components.Switch
import com.a3.yearlyprogess.core.util.resizeImageForAppStorage
import com.a3.yearlyprogess.feature.events.domain.model.Event
import com.a3.yearlyprogess.feature.events.domain.model.RepeatDays
import com.a3.yearlyprogess.feature.events.domain.model.Weekday
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
    val eventScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()


    var eventTitle by remember { mutableStateOf("") }
    var eventDescription by remember { mutableStateOf("") }
    var isAllDay by remember { mutableStateOf(false) }
    var startDateTime by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var endDateTime by remember { mutableLongStateOf(System.currentTimeMillis() + 24 * 60 * 60 * 1000L) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var isStartDateTime by remember { mutableStateOf(true) }
    var isEditingDate by remember { mutableStateOf(false) }
    var isEditingTime by remember { mutableStateOf(false) }
    var repeatEveryYear by remember { mutableStateOf(false) }
    var repeatEveryMonth by remember { mutableStateOf(false) }
    var repeatWeekdays by remember { mutableStateOf(false) }
    var selectedWeekdays by remember { mutableStateOf(setOf<RepeatDays>()) }
    var showError by remember { mutableStateOf(false) }
    var savedImagePath by remember { mutableStateOf<String?>(null) }
    val isEditMode = eventId != null

    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let {
            try {
                // Decode and resize the image
                val resizedBitmap = resizeImageForAppStorage(context, uri)

                // Save the resized bitmap
                val fileName = "image_${System.currentTimeMillis()}.jpg"
                val file = File(context.filesDir, fileName)
                FileOutputStream(file).use { output ->
                    resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 85, output)
                }
                savedImagePath = file.absolutePath

                // Clean up
                resizedBitmap.recycle()
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
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (isEditMode) stringResource(R.string.edit_event)
                        else stringResource(R.string.create_event)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                scrollBehavior = eventScrollBehavior
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
                Icon(Icons.Filled.Save, contentDescription = stringResource(R.string.save))
            }
        },
        contentWindowInsets = WindowInsets.safeDrawing,
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .nestedScroll(eventScrollBehavior.nestedScrollConnection)
                .verticalScroll(rememberScrollState())
            ,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {

            if (savedImagePath != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .aspectRatio(16f / 9f)
                        .clip(RoundedCornerShape(12.dp))
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(savedImagePath)
                            .crossfade(true)
                            .build(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    FilledTonalIconButton(
                        onClick = { savedImagePath = null },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Remove image",
                        )
                    }
                }
            } else {
                Card(
                    onClick = {
                        imagePickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .aspectRatio(16f / 9f)
                    ,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow   // no background
                    ),
                    elevation = CardDefaults.cardElevation(0.dp), // no shadow
                    shape = RoundedCornerShape(12.dp) // optional
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
                            text = "Add Image",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            EventDetailsSection(
                eventTitle = eventTitle,
                showError = showError,
                eventDescription = eventDescription,
                isAllDay = isAllDay,
                onTitleChange = {
                    eventTitle = it
                    showError = false
                },
                onDescriptionChange = { eventDescription = it },
                onAllDayChange = { isAllDay = it }
            )

            // All Day Switch

            ExpandableSection(title = stringResource(R.string.schedule), collapsible = false) {

                // Start Date/Time - Material Design Style
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.start),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Date Chip
                        MaterialDateTimeChip(
                            label = formatDateForChip(startDateTime), onClick = {
                                isStartDateTime = true
                                isEditingDate = true
                                isEditingTime = false
                                showDatePicker = true
                            }, modifier = Modifier.weight(1f)
                        )

                        // Time Chip (only if not all-day)
                        AnimatedVisibility(
                            visible = !isAllDay, modifier = Modifier.weight(1f)
                        ) {
                            MaterialDateTimeChip(
                                label = formatTimeForChip(startDateTime), onClick = {
                                    isStartDateTime = true
                                    isEditingDate = false
                                    isEditingTime = true
                                    showTimePicker = true
                                })
                        }
                    }
                }

                // End Date/Time - Material Design Style
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.end),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Date Chip
                        MaterialDateTimeChip(
                            label = formatDateForChip(endDateTime), onClick = {
                                isStartDateTime = false
                                isEditingDate = true
                                isEditingTime = false
                                showDatePicker = true
                            }, modifier = Modifier.weight(1f)
                        )

                        // Time Chip (only if not all-day)
                        AnimatedVisibility(
                            visible = !isAllDay, modifier = Modifier.weight(1f)
                        ) {
                            MaterialDateTimeChip(
                                label = formatTimeForChip(endDateTime), onClick = {
                                    isStartDateTime = false
                                    isEditingDate = false
                                    isEditingTime = true
                                    showTimePicker = true
                                }
                            )
                        }
                    }
                }
            }

            ExpandableSection(title = stringResource(R.string.repeat)) {

                Switch(
                    title = stringResource(R.string.every_year), checked = repeatEveryYear, onCheckedChange = {
                        repeatEveryYear = it
                        if (it) {
                            repeatEveryMonth = false
                            repeatWeekdays = false
                        }
                    })

                Switch(
                    title = stringResource(R.string.every_month), checked = repeatEveryMonth, onCheckedChange = {
                        repeatEveryMonth = it
                        if (it) {
                            repeatEveryYear = false
                            repeatWeekdays = false
                        }
                    })

                Switch(
                    title = stringResource(R.string.on_weekdays), checked = repeatWeekdays, onCheckedChange = {
                        repeatWeekdays = it
                        if (it) {
                            repeatEveryYear = false
                            repeatEveryMonth = false
                        }
                    })
            }

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
            onDismissRequest = {
                showDatePicker = false
                isEditingDate = false
            },
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

                        if (!isAllDay && !isEditingTime) {
                            showTimePicker = true
                        }
                        isEditingDate = false
                    }
                ) {
                   Text(stringResource(R.string.okay))
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDatePicker = false
                    isEditingDate = false
                }) {
                    Text(stringResource(R.string.cancel))
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
            is24Hour = android.text.format.DateFormat.is24HourFormat(context)

        )

        AlertDialog(
            onDismissRequest = {
                showTimePicker = false
                isEditingTime = false
            },
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
                        isEditingTime = false
                    }
                ) {
                   Text(stringResource(R.string.okay))
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showTimePicker = false
                    isEditingTime = false
                }) {
                    Text(stringResource(R.string.cancel))
                }
            },
            text = {
                TimePicker(state = timePickerState)
            }
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MaterialDateTimeChip(
    label: String, onClick: () -> Unit, modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(64.dp),
        shapes = ButtonDefaults.shapes(
            pressedShape = ButtonDefaults.shapes().shape,
            shape = ButtonDefaults.shapes().pressedShape
        ),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge
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
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            WeekdayChipRow(
                selectedWeekdays = selectedWeekdays,
                onWeekdayToggle = onWeekdayToggle
            )
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
    val isSelected = day in selectedWeekdays

    val animatedCorner by animateDpAsState(
        targetValue = if (!isSelected) 8.dp else 18.dp,
        animationSpec = tween(
            durationMillis = 100,
            easing = LinearOutSlowInEasing
        ),
        label = ""
    )

    val containerColor by animateColorAsState(
        targetValue = if (isSelected)
            MaterialTheme.colorScheme.primary
        else
            MaterialTheme.colorScheme.surfaceContainerLow,
        animationSpec = tween(
            durationMillis = 100,
            easing = LinearOutSlowInEasing
        ),
        label = ""
    )

    val labelColor by animateColorAsState(
        targetValue = if (isSelected)
            MaterialTheme.colorScheme.onPrimary
        else
            MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(
            durationMillis = 100,
            easing = LinearOutSlowInEasing
        ),
        label = ""
    )

    Surface(
        onClick = { onWeekdayToggle(day) },
        shape = RoundedCornerShape(animatedCorner),
        color = containerColor,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        modifier = modifier
            .height(40.dp)
            .clip(RoundedCornerShape(animatedCorner))
            .semantics {
                role = Role.Checkbox
            }
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                color = labelColor,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

// Format date for chip display
private fun formatDateForChip(timeInMillis: Long): String {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    return dateFormat.format(Date(timeInMillis))
}

// Format time for chip display
private fun formatTimeForChip(timeInMillis: Long): String {
    val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
    return timeFormat.format(Date(timeInMillis))
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

@Composable
private fun EventTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    isError: Boolean = false,
    errorText: String? = null,
    singleLine: Boolean = false,
    maxLines: Int = Int.MAX_VALUE,
    maxHeight: Dp? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .animateContentSize()
            .let {
                if (maxHeight != null) it.heightIn(max = maxHeight) else it
            },
        isError = isError,
        supportingText = errorText?.let { { Text(it) } },
        singleLine = singleLine,
        maxLines = maxLines
    )
}
@Composable
private fun EventDetailsSection(
    eventTitle: String,
    showError: Boolean,
    eventDescription: String,
    isAllDay: Boolean,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onAllDayChange: (Boolean) -> Unit
) {
    ExpandableSection(title = "Event Details", collapsible = false) {

        EventTextField(
            value = eventTitle,
            onValueChange = { onTitleChange(it) },
            label = stringResource(R.string.event_title),
            placeholder = stringResource(R.string.enter_event_title),
            isError = showError,
            errorText = if (showError) "Event title is required" else null,
            singleLine = true
        )

        Spacer(Modifier.height(12.dp))

        EventTextField(
            value = eventDescription,
            onValueChange = onDescriptionChange,
            label = stringResource(R.string.description),
            placeholder = stringResource(R.string.enter_event_description_optional),
            maxLines = 5,
            maxHeight = 120.dp
        )
        Spacer(Modifier.height(12.dp))
        Switch(
            title = stringResource(R.string.all_day_event),
            checked = isAllDay,
            onCheckedChange = onAllDayChange
        )
    }
}

@Composable
private fun WeekdayChipRow(
    selectedWeekdays: Set<RepeatDays>,
    onWeekdayToggle: (RepeatDays) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Weekday.entries.forEach { weekday ->
            WeekdayChip(
                label = weekday.label,
                day = weekday.repeatDay,
                selectedWeekdays = selectedWeekdays,
                onWeekdayToggle = onWeekdayToggle,
                modifier = Modifier.weight(1f)
            )
        }
    }
}




