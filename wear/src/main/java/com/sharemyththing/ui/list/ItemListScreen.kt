package com.sharemyththing.ui.list

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.EdgeButton
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.SurfaceTransformation
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.lazy.rememberTransformationSpec
import androidx.wear.compose.material3.lazy.transformedHeight
import com.sharemyththing.R
import com.sharemyththing.data.DisplayItem
import com.sharemyththing.ui.edgeButtonBottomScrollSpacer
import com.sharemyththing.ui.edgeButtonTopScrollSpacer

@Composable
fun ItemListScreen(
    items: List<DisplayItem>,
    onItemClick: (DisplayItem) -> Unit,
    onAddClick: () -> Unit,
    onTilesComplicationsClick: () -> Unit,
    onCommitItemOrder: (List<Long>) -> Unit,
    onSyncClick: () -> Unit,
) {
    var listItems by remember { mutableStateOf(items) }

    val listState = rememberTransformingLazyColumnState()
    val transformationSpec = rememberTransformationSpec()
    val reorderState = rememberWearListReorderState(
        itemCount = { listItems.size },
        onMove = { from, to ->
            listItems = listItems.toMutableList().apply {
                add(to, removeAt(from))
            }
        },
        onDragEnd = {
            onCommitItemOrder(listItems.map { it.id })
        },
    )

    LaunchedEffect(items, reorderState.isDragging) {
        if (!reorderState.isDragging) {
            listItems = items
        }
    }

    AppScaffold {
        ScreenScaffold(
            scrollState = listState,
            edgeButton = {
                EdgeButton(
                    onClick = onAddClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                ) {
                    Text(stringResource(R.string.add_item))
                }
            },
        ) { contentPadding ->
            TransformingLazyColumn(
                contentPadding = contentPadding,
                state = listState,
            ) {
                edgeButtonTopScrollSpacer(transformationSpec = transformationSpec)

                item(key = "sync") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp, bottom = 16.dp)
                            .transformedHeight(this, transformationSpec),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Button(
                            onClick = onSyncClick,
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape),
                            transformation = SurfaceTransformation(transformationSpec),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            ),
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Sync,
                                contentDescription = stringResource(R.string.sync_with_watch),
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    }
                }

                if (listItems.isEmpty()) {
                    item(key = "empty") {
                        Text(
                            text = stringResource(R.string.empty_items),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                                .transformedHeight(this, transformationSpec),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )
                    }
                } else {
                    listItems.forEachIndexed { index, item ->
                        item(key = item.id) {
                            val isDragging = reorderState.draggingIndex == index
                            val scale by animateFloatAsState(
                                targetValue = if (isDragging) DRAGGING_ITEM_SCALE else 1f,
                                animationSpec = tween(durationMillis = 150),
                                label = "dragScale",
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .zIndex(if (isDragging) DRAGGING_ITEM_Z_INDEX else 0f)
                                    .animateItem()
                                    .transformedHeight(this, transformationSpec)
                                    .graphicsLayer {
                                        scaleX = scale
                                        scaleY = scale
                                        clip = false
                                        if (isDragging) {
                                            translationY = reorderState.dragOffset
                                        }
                                    },
                            ) {
                                Button(
                                    onClick = { onItemClick(item) },
                                    modifier = Modifier.fillMaxWidth(),
                                    transformation = SurfaceTransformation(transformationSpec),
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.DragHandle,
                                            contentDescription = stringResource(R.string.reorder_drag_handle),
                                            modifier = Modifier
                                                .wearListReorderHandle(reorderState, index)
                                                .padding(start = 4.dp, end = 8.dp)
                                                .size(18.dp),
                                            tint = MaterialTheme.colorScheme.onPrimary,
                                        )
                                        Text(
                                            text = item.title,
                                            modifier = Modifier.weight(1f),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                item(key = "tiles") {
                    Button(
                        onClick = onTilesComplicationsClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp)
                            .transformedHeight(this, transformationSpec),
                        transformation = SurfaceTransformation(transformationSpec),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        ),
                    ) {
                        Text(stringResource(R.string.tiles_and_complications))
                    }
                }

                edgeButtonBottomScrollSpacer(transformationSpec = transformationSpec)
            }
        }
    }
}

private const val DRAGGING_ITEM_SCALE = 0.92f
private const val DRAGGING_ITEM_Z_INDEX = 100f
