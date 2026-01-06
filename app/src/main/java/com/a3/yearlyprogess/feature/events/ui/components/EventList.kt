package com.a3.yearlyprogess.feature.events.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.a3.yearlyprogess.core.ui.style.CardCornerStyle
import com.a3.yearlyprogess.core.util.ProgressSettings
import com.a3.yearlyprogess.feature.events.domain.model.Event

@Composable
fun EventList(
    events: List<Event>,
    selectedIds: Set<Int>,
    modifier: Modifier = Modifier,
    emptyText: String,
    emptyPadding: PaddingValues = PaddingValues(8.dp),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    onItemClick: (Event) -> Unit,
    onItemLongPress: (Event) -> Unit,
    settings: ProgressSettings
) {
    if (events.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(emptyPadding),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = emptyText,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
        return
    }

    LazyColumn(
        modifier = modifier
            .padding(horizontal = 14.dp)
            .fillMaxSize(),
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        itemsIndexed(events, key = { _, e -> e.id }) { index, event ->
            val isSelected = selectedIds.contains(event.id)
            val isPreviousSelected =
                index > 0 && selectedIds.contains(events[index - 1].id)
            val isNextSelected =
                index < events.lastIndex && selectedIds.contains(events[index + 1].id)

            EventDetailCard(
                event = event,
                isSelected = isSelected,
                onClick = { onItemClick(event) },
                onLongPress = { onItemLongPress(event) },
                style = EventDetailCardDefaults.eventDetailCardStyle(
                    cornerStyle = resolveCornerStyle(
                        index = index,
                        size = events.size,
                        isPreviousSelected = isPreviousSelected,
                        isNextSelected = isNextSelected
                    )
                ),
                settings = settings
            )
        }

        item {
            Spacer(Modifier.height(4.dp))
        }
    }
}

private fun resolveCornerStyle(
    index: Int,
    size: Int,
    isPreviousSelected: Boolean,
    isNextSelected: Boolean
): CardCornerStyle =
    when {
        size <= 1 -> CardCornerStyle.Default

        isPreviousSelected && isNextSelected ->
            CardCornerStyle.Default

        isPreviousSelected && !isNextSelected ->
            CardCornerStyle.FirstInList

        !isPreviousSelected && isNextSelected ->
            if (index == 0) CardCornerStyle.Default
            else CardCornerStyle.LastInList

        else ->
            when (index) {
                0 -> CardCornerStyle.FirstInList
                size - 1 -> CardCornerStyle.LastInList
                else -> CardCornerStyle.MiddleInList
            }
    }

