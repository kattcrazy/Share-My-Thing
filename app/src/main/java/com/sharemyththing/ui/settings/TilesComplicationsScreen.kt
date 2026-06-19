package com.sharemyththing.ui.settings

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
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
import com.sharemyththing.data.SurfaceSlot

@Composable
fun TilesComplicationsScreen(
    items: List<DisplayItem>,
    surfacesPlacedOnWatch: Set<SurfaceSlot>,
    slotAssignments: Map<SurfaceSlot, Long?>,
    onSlotClick: (SurfaceSlot) -> Unit,
) {
    val placedTiles = SurfaceSlot.tiles.filter { it in surfacesPlacedOnWatch }
    val placedComplications = SurfaceSlot.complications.filter { it in surfacesPlacedOnWatch }

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

                if (placedTiles.isEmpty() && placedComplications.isEmpty()) {
                    item {
                        Text(
                            text = stringResource(R.string.tiles_and_complications_empty),
                            modifier = Modifier
                                .fillMaxWidth()
                                .transformedHeight(this, transformationSpec),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )
                    }
                } else {
                    if (placedTiles.isNotEmpty()) {
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
                        placedTiles.forEach { slot ->
                            placedSlotItem(
                                slot = slot,
                                items = items,
                                slotAssignments = slotAssignments,
                                onSlotClick = onSlotClick,
                                transformationSpec = transformationSpec,
                            )
                        }
                    }

                    if (placedComplications.isNotEmpty()) {
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
                        placedComplications.forEach { slot ->
                            placedSlotItem(
                                slot = slot,
                                items = items,
                                slotAssignments = slotAssignments,
                                onSlotClick = onSlotClick,
                                transformationSpec = transformationSpec,
                            )
                        }
                    }
                }

                item {
                    Text(
                        text = stringResource(R.string.tiles_and_complications_help),
                        modifier = Modifier
                            .fillMaxWidth()
                            .transformedHeight(this, transformationSpec),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}

private fun TransformingLazyColumnScope.placedSlotItem(
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
        val buttonLabel = assignedItem?.title ?: stringResource(R.string.surface_not_set)

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
