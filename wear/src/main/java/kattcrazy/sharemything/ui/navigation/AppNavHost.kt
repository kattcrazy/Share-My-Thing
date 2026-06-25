package kattcrazy.sharemything.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kattcrazy.sharemything.data.DisplayItem
import kattcrazy.sharemything.data.ItemType
import kattcrazy.sharemything.data.SurfaceSlot
import kattcrazy.sharemything.ui.ItemsViewModel
import kattcrazy.sharemything.ui.detail.QrDetailScreen
import kattcrazy.sharemything.ui.detail.QrTipsScreen
import kattcrazy.sharemything.ui.detail.TextDetailScreen
import kattcrazy.sharemything.ui.edit.EditItemScreen
import kattcrazy.sharemything.ui.list.ItemListScreen
import kattcrazy.sharemything.ui.settings.SlotItemPickerScreen
import kattcrazy.sharemything.ui.settings.TilesComplicationsScreen

@Composable
fun AppNavHost(
    viewModel: ItemsViewModel,
    startItemId: Long?,
    onStartItemHandled: () -> Unit,
    startSurfaceSlot: SurfaceSlot?,
    onStartSurfaceSlotHandled: () -> Unit,
) {
    var screen by remember { mutableStateOf<AppScreen>(AppScreen.List) }
    var detailItem by remember { mutableStateOf<DisplayItem?>(null) }
    var editItem by remember { mutableStateOf<DisplayItem?>(null) }

    val watchVisibleItems by viewModel.watchVisibleItems.collectAsState()
    val slotAssignments by viewModel.slotAssignments.collectAsState()
    val surfacesPlacedOnWatch by viewModel.surfacesPlacedOnWatch.collectAsState()
    val syncFeedback by viewModel.syncFeedback.collectAsState()

    fun openItem(item: DisplayItem) {
        detailItem = item
        screen = when (item.type) {
            ItemType.TEXT -> AppScreen.TextDetail(item.id)
            ItemType.QR_CODE, ItemType.BOTH -> AppScreen.QrDetail(item.id)
        }
    }

    fun openEdit(item: DisplayItem) {
        editItem = item
        screen = AppScreen.Edit(item.id)
    }

    fun screenAfterEditCancel(): AppScreen {
        val item = editItem
        return if (item != null) {
            when (item.type) {
                ItemType.TEXT -> AppScreen.TextDetail(item.id)
                ItemType.QR_CODE, ItemType.BOTH -> AppScreen.QrDetail(item.id)
            }
        } else {
            AppScreen.List
        }
    }

    BackHandler(enabled = screen != AppScreen.List) {
        screen = when (screen) {
            is AppScreen.QrTips -> AppScreen.QrDetail((screen as AppScreen.QrTips).itemId)
            is AppScreen.PickSlotItem -> AppScreen.TilesComplications
            AppScreen.TilesComplications -> AppScreen.List
            is AppScreen.Edit -> screenAfterEditCancel()
            else -> AppScreen.List
        }
    }

    LaunchedEffect(startItemId, watchVisibleItems) {
        val itemId = startItemId ?: return@LaunchedEffect
        watchVisibleItems.firstOrNull { it.id == itemId }?.let { item ->
            openItem(item)
            onStartItemHandled()
        }
    }

    LaunchedEffect(startSurfaceSlot) {
        val slot = startSurfaceSlot ?: return@LaunchedEffect
        if (slot.isWatchSurface) {
            screen = AppScreen.PickSlotItem(slot)
        }
        onStartSurfaceSlotHandled()
    }

    LaunchedEffect(screen) {
        when (val current = screen) {
            is AppScreen.QrDetail -> detailItem = viewModel.getItem(current.itemId)
            is AppScreen.TextDetail -> detailItem = viewModel.getItem(current.itemId)
            is AppScreen.Edit -> {
                editItem = current.itemId?.let { viewModel.getItem(it) }
            }
            else -> Unit
        }
    }

    when (screen) {
        AppScreen.List -> {
            val isPeerAvailable by viewModel.isPeerAvailable.collectAsState()
            ItemListScreen(
                items = watchVisibleItems,
                isPeerAvailable = isPeerAvailable,
                onItemClick = ::openItem,
                onAddClick = {
                    editItem = null
                    screen = AppScreen.Edit()
                },
                onTilesComplicationsClick = { screen = AppScreen.TilesComplications },
                onSyncClick = { viewModel.syncWithWatch(manual = true) },
                syncFeedback = syncFeedback,
                onSyncFeedbackShown = viewModel::clearSyncFeedback,
            )
        }

        is AppScreen.QrDetail -> {
            detailItem?.let { item ->
                QrDetailScreen(
                    item = item,
                    onEditClick = { openEdit(item) },
                    onTipsClick = { screen = AppScreen.QrTips(item.id) },
                )
            } ?: run { screen = AppScreen.List }
        }

        is AppScreen.QrTips -> {
            QrTipsScreen()
        }

        is AppScreen.TextDetail -> {
            detailItem?.let { item ->
                TextDetailScreen(
                    item = item,
                    onEditClick = { openEdit(item) },
                )
            } ?: run { screen = AppScreen.List }
        }

        is AppScreen.Edit -> {
            val editingItemId = (screen as AppScreen.Edit).itemId
            EditItemScreen(
                existingItem = editItem,
                onSave = { title, content, type, icon ->
                    viewModel.saveItem(
                        id = editingItemId ?: editItem?.id,
                        title = title,
                        content = content,
                        type = type,
                        icon = icon,
                    ) { savedId ->
                        screen = when (type) {
                            ItemType.TEXT -> AppScreen.TextDetail(savedId)
                            ItemType.QR_CODE, ItemType.BOTH -> AppScreen.QrDetail(savedId)
                        }
                    }
                },
                onDelete = editItem?.let { item ->
                    {
                        viewModel.deleteItem(item) {
                            screen = AppScreen.List
                        }
                    }
                },
                onCancel = { screen = screenAfterEditCancel() },
            )
        }

        AppScreen.TilesComplications -> {
            TilesComplicationsScreen(
                items = watchVisibleItems,
                surfacesPlacedOnWatch = surfacesPlacedOnWatch,
                slotAssignments = slotAssignments,
                onSlotClick = { slot -> screen = AppScreen.PickSlotItem(slot) },
            )
        }

        is AppScreen.PickSlotItem -> {
            val pickSlotScreen = screen as AppScreen.PickSlotItem
            SlotItemPickerScreen(
                slot = pickSlotScreen.slot,
                items = watchVisibleItems,
                selectedItemId = slotAssignments[pickSlotScreen.slot],
                onSelectItem = { itemId ->
                    viewModel.setSlotItemId(pickSlotScreen.slot, itemId)
                    screen = AppScreen.TilesComplications
                },
                onClear = {
                    viewModel.setSlotItemId(pickSlotScreen.slot, null)
                    screen = AppScreen.TilesComplications
                },
            )
        }
    }
}
