package com.sharemyththing.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.sharemyththing.R
import com.sharemyththing.data.DisplayItem
import com.sharemyththing.data.SurfaceSlot
import com.sharemyththing.data.labelRes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TilesComplicationsScreen(
    items: List<DisplayItem>,
    slotAssignments: Map<SurfaceSlot, Long?>,
    onSlotClick: (SurfaceSlot) -> Unit,
    onBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.tiles_and_complications)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                        )
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                TooltipBox(
                    positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
                    tooltip = { PlainTooltip { Text(stringResource(R.string.tooltip_tiles)) } },
                    state = rememberTooltipState(),
                ) {
                    Text(
                        text = stringResource(R.string.tiles_and_complications_help),
                        modifier = Modifier.fillMaxWidth(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }
            }

            item {
                Text(
                    text = stringResource(R.string.tiles_section),
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }

            items(SurfaceSlot.tiles, key = { it.name }) { slot ->
                SlotRow(
                    slot = slot,
                    items = items,
                    slotAssignments = slotAssignments,
                    onSlotClick = onSlotClick,
                )
            }

            item {
                Text(
                    text = stringResource(R.string.complications_section),
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }

            items(SurfaceSlot.complications, key = { it.name }) { slot ->
                SlotRow(
                    slot = slot,
                    items = items,
                    slotAssignments = slotAssignments,
                    onSlotClick = onSlotClick,
                )
            }
        }
    }
}

@Composable
internal fun SlotRow(
    slot: SurfaceSlot,
    items: List<DisplayItem>,
    slotAssignments: Map<SurfaceSlot, Long?>,
    onSlotClick: (SurfaceSlot) -> Unit,
) {
    val assignedItem = slotAssignments[slot]?.let { itemId ->
        items.firstOrNull { it.id == itemId }
    }
    val slotLabel = stringResource(slot.labelRes)
    val buttonLabel = assignedItem?.title ?: slotLabel

    val isAssigned = assignedItem != null

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSlotClick(slot) },
        colors = CardDefaults.cardColors(
            containerColor = if (isAssigned) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.secondaryContainer
            },
            contentColor = if (isAssigned) {
                MaterialTheme.colorScheme.onPrimary
            } else {
                MaterialTheme.colorScheme.onSecondaryContainer
            },
        ),
    ) {
        Text(
            text = buttonLabel,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            textAlign = TextAlign.Center,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SlotItemPickerScreen(
    slot: SurfaceSlot,
    items: List<DisplayItem>,
    selectedItemId: Long?,
    onSelectItem: (Long) -> Unit,
    onClear: () -> Unit,
    onBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(slot.labelRes)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                        )
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                Text(
                    text = stringResource(R.string.choose_item_for_surface),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            if (items.isEmpty()) {
                item {
                    Text(text = stringResource(R.string.empty_items))
                }
            } else {
                items(items, key = { it.id }) { item ->
                    val isSelected = selectedItemId == item.id
                    Card(
                        onClick = { onSelectItem(item.id) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.secondaryContainer
                            },
                            contentColor = if (isSelected) {
                                MaterialTheme.colorScheme.onPrimary
                            } else {
                                MaterialTheme.colorScheme.onSecondaryContainer
                            },
                        ),
                    ) {
                        Text(
                            text = item.title,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.titleMedium,
                        )
                    }
                }
            }

            item {
                Card(
                    onClick = onClear,
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer,
                    ),
                ) {
                    Text(
                        text = stringResource(R.string.clear_surface),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
            }
        }
    }
}
