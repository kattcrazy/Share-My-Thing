package kattcrazy.sharemything.ui.list

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.net.Uri
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.FileUpload
import androidx.compose.material.icons.outlined.Watch
import androidx.compose.material.icons.outlined.WatchOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FabPosition
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
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
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
import kattcrazy.sharemything.ui.ImportExportFeedback
import kattcrazy.sharemything.ui.SyncFeedback
import kattcrazy.sharemything.ui.components.ExportBackupDialog
import kattcrazy.sharemything.ui.components.ImportBackupDialog
import kattcrazy.sharemything.ui.components.SupportBanner
import kattcrazy.sharemything.sync.BackupPayload
import kattcrazy.sharemything.sync.ImportMode
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
    onAppShortcutsClick: () -> Unit,
    onWatchTilesClick: () -> Unit,
    onWatchComplicationsClick: () -> Unit,
    onAboutClick: () -> Unit,
    onSetVisibleOnWatch: (DisplayItem, Boolean) -> Unit,
    onCommitItemOrder: (List<Long>) -> Unit,
    onSyncClick: () -> Unit,
    syncFeedback: StateFlow<SyncFeedback?>,
    onSyncFeedbackShown: () -> Unit,
    onExportBackup: (Uri, Boolean) -> Unit,
    onImportBackupSelected: (Uri) -> Unit,
    onConfirmImport: (ImportMode) -> Unit,
    onCancelPendingImport: () -> Unit,
    importExportFeedback: StateFlow<ImportExportFeedback?>,
    onImportExportFeedbackShown: () -> Unit,
) {
    val lazyListState = rememberLazyListState()
    var listItems by remember { mutableStateOf(items) }
    var pendingOrderCommit by remember { mutableStateOf<List<Long>?>(null) }
    val reorderableState = rememberReorderableLazyListState(lazyListState) { from, to ->
        val reordered = reorderListItems(listItems, from.key, to.key) ?: return@rememberReorderableLazyListState
        listItems = reordered
    }
    LaunchedEffect(items) {
        if (reorderableState.isAnyItemDragging) return@LaunchedEffect
        val pending = pendingOrderCommit
        when {
            pending != null -> {
                if (items.map { it.id } == pending) {
                    pendingOrderCommit = null
                    listItems = items
                }
            }
            listItems.map { it.id } != items.map { it.id } -> {
                listItems = items
            }
        }
    }
    var wasDragging by remember { mutableStateOf(false) }
    LaunchedEffect(reorderableState.isAnyItemDragging) {
        if (wasDragging && !reorderableState.isAnyItemDragging) {
            val order = listItems.map { it.id }
            val currentOrder = items.map { it.id }
            if (order != currentOrder) {
                pendingOrderCommit = order
                onCommitItemOrder(order)
            }
        }
        wasDragging = reorderableState.isAnyItemDragging
    }
    val snackbarHostState = remember { SnackbarHostState() }
    val feedback by syncFeedback.collectAsState()
    val importExportFeedbackState by importExportFeedback.collectAsState()
    val syncInProgressMessage = stringResource(R.string.sync_in_progress)
    val syncSuccessMessage = stringResource(R.string.sync_success)
    val syncNoWatchMessage = stringResource(R.string.sync_no_watch)
    val syncAutoFailedMessage = stringResource(R.string.sync_auto_failed)
    val exportSuccessMessage = stringResource(R.string.export_success)
    val importSuccessReplaceMessage = stringResource(R.string.import_success_replace)
    val importSuccessMergeMessage = stringResource(R.string.import_success_merge)
    val importSuccessAddMessage = stringResource(R.string.import_success_add)
    val isRefreshing = feedback == SyncFeedback.Syncing
    val context = LocalContext.current
    var showExportDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    var pendingExportIncludeAll by remember { mutableStateOf<Boolean?>(null) }

    val exportDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json"),
    ) { uri ->
        val includeAll = pendingExportIncludeAll
        pendingExportIncludeAll = null
        if (uri != null && includeAll != null) {
            onExportBackup(uri, includeAll)
        }
    }

    val importDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri != null) {
            onImportBackupSelected(uri)
        }
    }

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

    LaunchedEffect(importExportFeedbackState) {
        when (val current = importExportFeedbackState) {
            null -> Unit
            ImportExportFeedback.ImportReady -> {
                showImportDialog = true
                onImportExportFeedbackShown()
            }
            ImportExportFeedback.ExportSuccess -> {
                snackbarHostState.showSnackbar(
                    message = exportSuccessMessage,
                    duration = SnackbarDuration.Short,
                )
                onImportExportFeedbackShown()
            }
            is ImportExportFeedback.ImportSuccess -> {
                snackbarHostState.showSnackbar(
                    message = when (current.mode) {
                        ImportMode.REPLACE -> importSuccessReplaceMessage
                        ImportMode.MERGE -> importSuccessMergeMessage
                        ImportMode.ADD -> importSuccessAddMessage
                    },
                    duration = SnackbarDuration.Short,
                )
                onImportExportFeedbackShown()
            }
            is ImportExportFeedback.Error -> {
                snackbarHostState.showSnackbar(
                    message = context.getString(R.string.import_export_error, current.message),
                    duration = SnackbarDuration.Long,
                )
                onImportExportFeedbackShown()
            }
        }
    }

    if (showExportDialog) {
        ExportBackupDialog(
            onDismiss = { showExportDialog = false },
            onConfirm = { includeAll ->
                showExportDialog = false
                pendingExportIncludeAll = includeAll
                exportDocumentLauncher.launch(BackupPayload.DEFAULT_FILENAME)
            },
        )
    }

    if (showImportDialog) {
        ImportBackupDialog(
            onDismiss = {
                showImportDialog = false
                onCancelPendingImport()
            },
            onConfirm = { mode ->
                showImportDialog = false
                onConfirmImport(mode)
            },
        )
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
                        IconButton(onClick = { importDocumentLauncher.launch(arrayOf("application/json", "text/plain", "*/*")) }) {
                            Icon(
                                imageVector = Icons.Outlined.FileDownload,
                                contentDescription = stringResource(R.string.import_backup),
                            )
                        }
                        IconButton(onClick = { showExportDialog = true }) {
                            Icon(
                                imageVector = Icons.Outlined.FileUpload,
                                contentDescription = stringResource(R.string.export_backup),
                            )
                        }
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
        floatingActionButtonPosition = FabPosition.Center,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddClick,
                modifier = Modifier.fillMaxWidth(1f / 3f),
                icon = {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = null,
                    )
                },
                text = {
                    Text(text = stringResource(R.string.add_item))
                },
            )
        },
    ) { padding ->
        val listModifier = Modifier
            .fillMaxSize()
            .padding(padding)

        if (isPeerAvailable) {
            val pullRefreshState = rememberPullToRefreshState()
            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = onSyncClick,
                modifier = listModifier,
                state = pullRefreshState,
                indicator = {
                    PullToRefreshDefaults.Indicator(
                        state = pullRefreshState,
                        isRefreshing = isRefreshing,
                        modifier = Modifier.align(Alignment.TopCenter),
                        containerColor = MaterialTheme.colorScheme.surface,
                        color = MaterialTheme.colorScheme.primary,
                    )
                },
            ) {
                ItemListContent(
                    listItems = listItems,
                    lazyListState = lazyListState,
                    isPeerAvailable = isPeerAvailable,
                    reorderableState = reorderableState,
                    onItemClick = onItemClick,
                    onSetVisibleOnWatch = onSetVisibleOnWatch,
                    onPhoneWidgetsClick = onPhoneWidgetsClick,
                    onAppShortcutsClick = onAppShortcutsClick,
                    onWatchTilesClick = onWatchTilesClick,
                    onWatchComplicationsClick = onWatchComplicationsClick,
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
                onAppShortcutsClick = onAppShortcutsClick,
                onWatchTilesClick = onWatchTilesClick,
                onWatchComplicationsClick = onWatchComplicationsClick,
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
    onAppShortcutsClick: () -> Unit,
    onWatchTilesClick: () -> Unit,
    onWatchComplicationsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        state = lazyListState,
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 16.dp,
            bottom = 16.dp + ExtendedFloatingActionButtonHeight,
        ),
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

        if (listItems.isNotEmpty()) {
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

            item(key = "app_shortcuts") {
                SectionCard(
                    title = stringResource(R.string.app_shortcuts),
                    onClick = onAppShortcutsClick,
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

                item(key = "watch_tiles") {
                    SectionCard(
                        title = stringResource(R.string.watch_tiles),
                        onClick = onWatchTilesClick,
                    )
                }

                item(key = "watch_complications") {
                    SectionCard(
                        title = stringResource(R.string.watch_complications),
                        onClick = onWatchComplicationsClick,
                    )
                }
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

/** Center FAB overlays list content; keep the last section scrollable above it. */
private val ExtendedFloatingActionButtonHeight = 56.dp

/** Map lazy-list keys to item indices; footer keys (strings) clamp to last row. */
private fun reorderListItems(
    current: List<DisplayItem>,
    fromKey: Any?,
    toKey: Any?,
): List<DisplayItem>? {
    if (current.isEmpty() || fromKey !is Long) return null
    val fromIndex = current.indexOfFirst { it.id == fromKey }
    if (fromIndex < 0) return null
    val toIndex = when (toKey) {
        is Long -> current.indexOfFirst { it.id == toKey }
        else -> current.lastIndex
    }
    if (toIndex < 0) return null
    if (fromIndex == toIndex) return current
    return current.toMutableList().apply {
        add(toIndex, removeAt(fromIndex))
    }
}
