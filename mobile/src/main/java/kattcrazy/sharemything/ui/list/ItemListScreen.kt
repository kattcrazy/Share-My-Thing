package kattcrazy.sharemything.ui.list

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Watch
import androidx.compose.material.icons.outlined.WatchOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kattcrazy.sharemything.R
import kattcrazy.sharemything.presentation.theme.appNameTextStyle
import kattcrazy.sharemything.data.DisplayItem
import kattcrazy.sharemything.ui.SyncFeedback
import kattcrazy.sharemything.ui.components.SupportBanner
import kotlinx.coroutines.flow.StateFlow
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemListScreen(
    items: List<DisplayItem>,
    isPeerAvailable: Boolean,
    onItemClick: (DisplayItem) -> Unit,
    onAddClick: () -> Unit,
    onPhoneWidgetsClick: () -> Unit,
    onTilesComplicationsClick: () -> Unit,
    onAboutClick: () -> Unit,
    onSetVisibleOnWatch: (DisplayItem, Boolean) -> Unit,
    onCommitItemOrder: (List<Long>) -> Unit,
    onSyncClick: () -> Unit,
    syncFeedback: StateFlow<SyncFeedback?>,
    onSyncFeedbackShown: () -> Unit,
) {
    val lazyListState = rememberLazyListState()
    var listItems by remember { mutableStateOf(items) }
    val reorderableState = rememberReorderableLazyListState(lazyListState) { from, to ->
        if (to.index >= listItems.size) return@rememberReorderableLazyListState
        listItems = listItems.toMutableList().apply {
            add(to.index, removeAt(from.index))
        }
    }
    LaunchedEffect(items) {
        if (
            !reorderableState.isAnyItemDragging &&
            listItems.map { it.id } != items.map { it.id }
        ) {
            listItems = items
        }
    }
    var wasDragging by remember { mutableStateOf(false) }
    LaunchedEffect(reorderableState.isAnyItemDragging) {
        if (wasDragging && !reorderableState.isAnyItemDragging) {
            onCommitItemOrder(listItems.map { it.id })
        }
        wasDragging = reorderableState.isAnyItemDragging
    }
    val snackbarHostState = remember { SnackbarHostState() }
    val feedback by syncFeedback.collectAsState()
    val syncInProgressMessage = stringResource(R.string.sync_in_progress)
    val syncSuccessMessage = stringResource(R.string.sync_success)
    val syncNoWatchMessage = stringResource(R.string.sync_no_watch)
    val syncAutoFailedMessage = stringResource(R.string.sync_auto_failed)
    val isRefreshing = feedback == SyncFeedback.Syncing
    val context = LocalContext.current

    LaunchedEffect(feedback) {
        when (val current = feedback) {
            null -> Unit
            SyncFeedback.Syncing -> {
                snackbarHostState.showSnackbar(
                    message = syncInProgressMessage,
                    duration = SnackbarDuration.Indefinite,
                )
            }
            SyncFeedback.Success -> {
                snackbarHostState.currentSnackbarData?.dismiss()
                snackbarHostState.showSnackbar(
                    message = syncSuccessMessage,
                    duration = SnackbarDuration.Short,
                )
                onSyncFeedbackShown()
            }
            SyncFeedback.NoWatchConnected -> {
                snackbarHostState.currentSnackbarData?.dismiss()
                snackbarHostState.showSnackbar(
                    message = syncNoWatchMessage,
                    duration = SnackbarDuration.Long,
                )
                onSyncFeedbackShown()
            }
            SyncFeedback.AutoFailed -> {
                snackbarHostState.currentSnackbarData?.dismiss()
                snackbarHostState.showSnackbar(
                    message = syncAutoFailedMessage,
                    duration = SnackbarDuration.Long,
                )
                onSyncFeedbackShown()
            }
            is SyncFeedback.Error -> {
                snackbarHostState.currentSnackbarData?.dismiss()
                snackbarHostState.showSnackbar(
                    message = context.getString(R.string.sync_error, current.message),
                    duration = SnackbarDuration.Long,
                )
                onSyncFeedbackShown()
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Text(
                            text = stringResource(R.string.app_name_short),
                            style = appNameTextStyle(),
                        )
                    },
                    actions = {
                        IconButton(onClick = onAboutClick) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.HelpOutline,
                                contentDescription = stringResource(R.string.about_title),
                            )
                        }
                    },
                )
                SupportBanner()
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddClick) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = stringResource(R.string.add_item),
                )
            }
        },
    ) { padding ->
        val listModifier = Modifier
            .fillMaxSize()
            .padding(padding)

        if (isPeerAvailable) {
            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = onSyncClick,
                modifier = listModifier,
            ) {
                ItemListContent(
                    listItems = listItems,
                    lazyListState = lazyListState,
                    isPeerAvailable = isPeerAvailable,
                    reorderableState = reorderableState,
                    onItemClick = onItemClick,
                    onSetVisibleOnWatch = onSetVisibleOnWatch,
                    onPhoneWidgetsClick = onPhoneWidgetsClick,
                    onTilesComplicationsClick = onTilesComplicationsClick,
                )
            }
        } else {
            ItemListContent(
                listItems = listItems,
                lazyListState = lazyListState,
                isPeerAvailable = isPeerAvailable,
                reorderableState = reorderableState,
                onItemClick = onItemClick,
                onSetVisibleOnWatch = onSetVisibleOnWatch,
                onPhoneWidgetsClick = onPhoneWidgetsClick,
                onTilesComplicationsClick = onTilesComplicationsClick,
                modifier = listModifier,
            )
        }
    }
}

@Composable
private fun ItemListContent(
    listItems: List<DisplayItem>,
    lazyListState: LazyListState,
    isPeerAvailable: Boolean,
    reorderableState: sh.calvin.reorderable.ReorderableLazyListState,
    onItemClick: (DisplayItem) -> Unit,
    onSetVisibleOnWatch: (DisplayItem, Boolean) -> Unit,
    onPhoneWidgetsClick: () -> Unit,
    onTilesComplicationsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        state = lazyListState,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (listItems.isEmpty()) {
            item(key = "empty") {
                Text(
                    text = stringResource(R.string.empty_items),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            items(listItems, key = { it.id }) { item ->
                ReorderableItem(reorderableState, key = item.id) { isDragging ->
                    val scale by animateFloatAsState(
                        targetValue = if (isDragging) DRAGGING_ITEM_SCALE else 1f,
                        animationSpec = tween(durationMillis = 150),
                        label = "dragScale",
                    )
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                            },
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                        ),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = Icons.Filled.DragHandle,
                                contentDescription = stringResource(R.string.reorder_drag_handle),
                                modifier = Modifier
                                    .draggableHandle()
                                    .padding(start = 8.dp, top = 16.dp, bottom = 16.dp),
                                tint = MaterialTheme.colorScheme.onPrimary,
                            )
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onItemClick(item) }
                                    .padding(start = 12.dp, end = 8.dp, top = 16.dp, bottom = 16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = item.title,
                                    modifier = Modifier.fillMaxWidth(),
                                    style = MaterialTheme.typography.titleMedium,
                                )
                            }
                            if (isPeerAvailable) {
                                IconButton(
                                    onClick = { onSetVisibleOnWatch(item, !item.visibleOnWatch) },
                                ) {
                                    Icon(
                                        imageVector = if (item.visibleOnWatch) {
                                            Icons.Outlined.Watch
                                        } else {
                                            Icons.Outlined.WatchOff
                                        },
                                        contentDescription = if (item.visibleOnWatch) {
                                            stringResource(R.string.watch_visible_on)
                                        } else {
                                            stringResource(R.string.watch_visible_off)
                                        },
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        item(key = "widgets_heading") {
            Text(
                text = stringResource(R.string.list_widgets_section),
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        item(key = "phone_widgets") {
            SectionCard(
                title = stringResource(R.string.phone_widgets),
                onClick = onPhoneWidgetsClick,
            )
        }

        if (isPeerAvailable) {
            item(key = "watch_heading") {
                Text(
                    text = stringResource(R.string.list_watch_section),
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            item(key = "tiles_complications") {
                SectionCard(
                    title = stringResource(R.string.tiles_and_complications),
                    onClick = onTilesComplicationsClick,
                )
            }
        }
    }
}

@Composable
private fun SectionCard(
    title: String,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f),
            )
            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
            )
        }
    }
}

private const val DRAGGING_ITEM_SCALE = 0.92f
