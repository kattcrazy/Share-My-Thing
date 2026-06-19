package com.sharemyththing.ui.settings

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.TransformingLazyColumnScope
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.SurfaceTransformation
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.lazy.TransformationSpec
import androidx.wear.compose.material3.lazy.rememberTransformationSpec
import androidx.wear.compose.material3.lazy.transformedHeight
import com.sharemyththing.R
import com.sharemyththing.data.DisplayItem
import com.sharemyththing.data.labelRes
import com.sharemyththing.data.SurfaceSlot
import com.sharemyththing.ui.bottomScrollSpacer

@Composable
fun TilesComplicationsScreen(
    items: List<DisplayItem>,
    surfacesPlacedOnWatch: Set<SurfaceSlot>,
    slotAssignments: Map<SurfaceSlot, Long?>,
    onSlotClick: (SurfaceSlot) -> Unit,
) {
    AppScaffold {
        val listState = rememberTransformingLazyColumnState()
        val transformationSpec = rememberTransformationSpec()
        ScreenScaffold(scrollState = listState) { contentPadding ->
            TransformingLazyColumn(contentPadding = contentPadding, state = listState) {
                item {
                    ListHeader(
                        modifier = Modifier
                            .fillMaxWidth()
                            .transformedHeight(this, transformationSpec),
                        transformation = SurfaceTransformation(transformationSpec),
                    ) {
                        Text(stringResource(R.string.tiles_and_complications))
                    }
                }

                item {
                    Text(
                        text = stringResource(R.string.tiles_and_complications_help),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 4.dp)
                            .transformedHeight(this, transformationSpec),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }

                item {
                    Text(
                        text = stringResource(R.string.tiles_section),
                        modifier = Modifier
                            .fillMaxWidth()
                            .transformedHeight(this, transformationSpec),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }
                SurfaceSlot.tiles.forEach { slot ->
                    slotItem(
                        slot = slot,
                        items = items,
                        slotAssignments = slotAssignments,
                        onSlotClick = onSlotClick,
                        transformationSpec = transformationSpec,
                    )
                }

                item {
                    Text(
                        text = stringResource(R.string.complications_section),
                        modifier = Modifier
                            .fillMaxWidth()
                            .transformedHeight(this, transformationSpec),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }
                SurfaceSlot.complications.forEach { slot ->
                    slotItem(
                        slot = slot,
                        items = items,
                        slotAssignments = slotAssignments,
                        onSlotClick = onSlotClick,
                        transformationSpec = transformationSpec,
                    )
                }

                bottomScrollSpacer(transformationSpec = transformationSpec)
            }
        }
    }
}

private fun TransformingLazyColumnScope.slotItem(
    slot: SurfaceSlot,
    items: List<DisplayItem>,
    slotAssignments: Map<SurfaceSlot, Long?>,
    onSlotClick: (SurfaceSlot) -> Unit,
    transformationSpec: TransformationSpec,
) {
    item(key = slot.name) {
        val assignedItem = slotAssignments[slot]?.let { itemId ->
            items.firstOrNull { it.id == itemId }
        }
        val slotLabel = stringResource(slot.labelRes)
        val buttonLabel = assignedItem?.title ?: slotLabel

        Button(
            onClick = { onSlotClick(slot) },
            modifier = Modifier
                .fillMaxWidth()
                .transformedHeight(this, transformationSpec),
            transformation = SurfaceTransformation(transformationSpec),
            colors = if (assignedItem != null) {
                ButtonDefaults.buttonColors()
            } else {
                ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                )
            },
        ) {
            Text(
                text = buttonLabel,
                textAlign = TextAlign.Center,
            )
        }
    }
}
