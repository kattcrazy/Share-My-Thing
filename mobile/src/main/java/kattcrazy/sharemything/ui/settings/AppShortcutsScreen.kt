package kattcrazy.sharemything.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kattcrazy.sharemything.R
import kattcrazy.sharemything.data.DisplayItem
import kattcrazy.sharemything.data.SurfaceSlot
import kattcrazy.sharemything.data.labelRes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppShortcutsScreen(
    items: List<DisplayItem>,
    slotAssignments: Map<SurfaceSlot, Long?>,
    onSlotClick: (SurfaceSlot) -> Unit,
    onBack: () -> Unit,
) {
    WatchSurfaceSlotsScreen(
        title = stringResource(R.string.app_shortcuts),
        help = stringResource(R.string.app_shortcuts_help),
        slots = SurfaceSlot.phoneShortcuts,
        items = items,
        slotAssignments = slotAssignments,
        onSlotClick = onSlotClick,
        onBack = onBack,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WatchTilesScreen(
    items: List<DisplayItem>,
    slotAssignments: Map<SurfaceSlot, Long?>,
    onSlotClick: (SurfaceSlot) -> Unit,
    onBack: () -> Unit,
) {
    WatchSurfaceSlotsScreen(
        title = stringResource(R.string.watch_tiles),
        help = stringResource(R.string.watch_tiles_help),
        slots = SurfaceSlot.tiles,
        items = items,
        slotAssignments = slotAssignments,
        onSlotClick = onSlotClick,
        onBack = onBack,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WatchComplicationsScreen(
    items: List<DisplayItem>,
    slotAssignments: Map<SurfaceSlot, Long?>,
    onSlotClick: (SurfaceSlot) -> Unit,
    onBack: () -> Unit,
) {
    WatchSurfaceSlotsScreen(
        title = stringResource(R.string.watch_complications),
        help = stringResource(R.string.watch_complications_help),
        slots = SurfaceSlot.complications,
        items = items,
        slotAssignments = slotAssignments,
        onSlotClick = onSlotClick,
        onBack = onBack,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun WatchSurfaceSlotsScreen(
    title: String,
    help: String,
    slots: List<SurfaceSlot>,
    items: List<DisplayItem>,
    slotAssignments: Map<SurfaceSlot, Long?>,
    onSlotClick: (SurfaceSlot) -> Unit,
    onBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
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
                    text = help,
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }

            items(slots, key = { it.name }) { slot ->
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
