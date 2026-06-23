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
import kattcrazy.sharemything.ui.about.AboutScreen
import kattcrazy.sharemything.ui.detail.QrDetailScreen
import kattcrazy.sharemything.ui.detail.QrTipsScreen
import kattcrazy.sharemything.ui.detail.TextDetailScreen
import kattcrazy.sharemything.ui.edit.EditItemScreen
import kattcrazy.sharemything.ui.list.ItemListScreen
import kattcrazy.sharemything.ui.settings.PhoneWidgetsScreen
import kattcrazy.sharemything.ui.settings.SlotItemPickerScreen
import kattcrazy.sharemything.ui.settings.TilesComplicationsScreen

@Composable
fun AppNavHost(
    viewModel: ItemsViewModel,
    startItemId: Long? = null,
    onStartItemHandled: () -> Unit = {},
    startSurfaceSlot: SurfaceSlot? = null,
    onStartSurfaceSlotHandled: () -> Unit = {},
) {
    var screen by remember { mutableStateOf<AppScreen>(AppScreen.List) }
    var detailItem by remember { mutableStateOf<DisplayItem?>(null) }
    var editItem by remember { mutableStateOf<DisplayItem?>(null) }

    val items by viewModel.items.collectAsState()
    val watchVisibleItems by viewModel.watchVisibleItems.collectAsState()
    val slotAssignments by viewModel.slotAssignments.collectAsState()

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

    fun parentScreenForSlot(slot: SurfaceSlot): AppScreen =
        if (slot.isPhoneWidget) AppScreen.PhoneWidgets else AppScreen.TilesComplications

    fun navigateBack() {
        screen = when (val current = screen) {
            AppScreen.QrTips -> detailItem?.let { AppScreen.QrDetail(it.id) } ?: AppScreen.List
            is AppScreen.PickSlotItem -> parentScreenForSlot(current.slot)
            AppScreen.About -> AppScreen.List
            AppScreen.TilesComplications, AppScreen.PhoneWidgets -> AppScreen.List
            is AppScreen.Edit -> screenAfterEditCancel()
            is AppScreen.QrDetail, is AppScreen.TextDetail -> AppScreen.List
            AppScreen.List -> AppScreen.List
        }
    }

    BackHandler(enabled = screen != AppScreen.List) {
        navigateBack()
    }

    LaunchedEffect(startItemId, items) {
        val itemId = startItemId ?: return@LaunchedEffect
        items.firstOrNull { it.id == itemId }?.let { item ->
            openItem(item)
            onStartItemHandled()
        }
    }

    LaunchedEffect(startSurfaceSlot) {
        val slot = startSurfaceSlot ?: return@LaunchedEffect
        screen = if (slot.isPhoneWidget) {
            AppScreen.PickSlotItem(slot)
        } else {
            AppScreen.PickSlotItem(slot)
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
                items = items,
                isPeerAvailable = isPeerAvailable,
                onItemClick = ::openItem,
                onAddClick = {
                    editItem = null
                    screen = AppScreen.Edit()
                },
                onPhoneWidgetsClick = { screen = AppScreen.PhoneWidgets },
                onTilesComplicationsClick = { screen = AppScreen.TilesComplications },
                onAboutClick = { screen = AppScreen.About },
                onSetVisibleOnWatch = viewModel::setVisibleOnWatch,
                onCommitItemOrder = viewModel::commitItemOrder,
                onSyncClick = { viewModel.syncWithWatch(manual = true) },
                syncFeedback = viewModel.syncFeedback,
                onSyncFeedbackShown = viewModel::clearSyncFeedback,
            )
        }

        AppScreen.About -> {
            AboutScreen(onBack = ::navigateBack)
        }

        is AppScreen.QrDetail -> {
            detailItem?.let { item ->
                QrDetailScreen(
                    item = item,
                    onEditClick = { openEdit(item) },
                    onTipsClick = { screen = AppScreen.QrTips },
                    onBack = ::navigateBack,
                )
            } ?: run { screen = AppScreen.List }
        }

        AppScreen.QrTips -> {
            QrTipsScreen(onBack = ::navigateBack)
        }

        is AppScreen.TextDetail -> {
            detailItem?.let { item ->
                TextDetailScreen(
                    item = item,
                    onEditClick = { openEdit(item) },
                    onBack = ::navigateBack,
                )
            } ?: run { screen = AppScreen.List }
        }

        is AppScreen.Edit -> {
            val editingItemId = (screen as AppScreen.Edit).itemId
            EditItemScreen(
                existingItem = editItem,
                onSave = { title, content, type ->
                    viewModel.saveItem(
                        id = editingItemId ?: editItem?.id,
                        title = title,
                        content = content,
                        type = type,
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
                slotAssignments = slotAssignments,
                onSlotClick = { slot -> screen = AppScreen.PickSlotItem(slot) },
                onBack = ::navigateBack,
            )
        }

        AppScreen.PhoneWidgets -> {
            PhoneWidgetsScreen(
                items = items,
                slotAssignments = slotAssignments,
                onSlotClick = { slot -> screen = AppScreen.PickSlotItem(slot) },
                onBack = ::navigateBack,
            )
        }

        is AppScreen.PickSlotItem -> {
            val pickSlotScreen = screen as AppScreen.PickSlotItem
            val pickerItems = if (pickSlotScreen.slot.isPhoneWidget) items else watchVisibleItems
            SlotItemPickerScreen(
                slot = pickSlotScreen.slot,
                items = pickerItems,
                selectedItemId = slotAssignments[pickSlotScreen.slot],
                onSelectItem = { itemId ->
                    viewModel.setSlotItemId(pickSlotScreen.slot, itemId)
                    screen = parentScreenForSlot(pickSlotScreen.slot)
                },
                onClear = {
                    viewModel.setSlotItemId(pickSlotScreen.slot, null)
                    screen = parentScreenForSlot(pickSlotScreen.slot)
                },
                onBack = ::navigateBack,
            )
        }
    }
}
