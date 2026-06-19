package com.sharemyththing.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.sharemyththing.data.DisplayItem
import com.sharemyththing.data.ItemType
import com.sharemyththing.data.SurfaceSlot
import com.sharemyththing.ui.ItemsViewModel
import com.sharemyththing.ui.detail.QrDetailScreen
import com.sharemyththing.ui.detail.TextDetailScreen
import com.sharemyththing.ui.edit.EditItemScreen
import com.sharemyththing.ui.list.ItemListScreen
import com.sharemyththing.ui.settings.SlotItemPickerScreen
import com.sharemyththing.ui.settings.TilesComplicationsScreen

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

    val items by viewModel.items.collectAsState()
    val slotAssignments by viewModel.slotAssignments.collectAsState()
    val surfacesPlacedOnWatch by viewModel.surfacesPlacedOnWatch.collectAsState()

    fun openItem(item: DisplayItem) {
        detailItem = item
        screen = when (item.type) {
            ItemType.QR_CODE -> AppScreen.QrDetail(item.id)
            ItemType.TEXT -> AppScreen.TextDetail(item.id)
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
                ItemType.QR_CODE -> AppScreen.QrDetail(item.id)
                ItemType.TEXT -> AppScreen.TextDetail(item.id)
            }
        } else {
            AppScreen.List
        }
    }

    BackHandler(enabled = screen != AppScreen.List) {
        screen = when (screen) {
            is AppScreen.PickSlotItem -> AppScreen.TilesComplications
            AppScreen.TilesComplications -> AppScreen.List
            is AppScreen.Edit -> screenAfterEditCancel()
            else -> AppScreen.List
        }
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
        screen = AppScreen.PickSlotItem(slot)
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
            ItemListScreen(
                items = items,
                onItemClick = ::openItem,
                onAddClick = {
                    editItem = null
                    screen = AppScreen.Edit()
                },
                onTilesComplicationsClick = { screen = AppScreen.TilesComplications },
            )
        }

        is AppScreen.QrDetail -> {
            detailItem?.let { item ->
                QrDetailScreen(
                    item = item,
                    onEditClick = { openEdit(item) },
                )
            } ?: run { screen = AppScreen.List }
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
            EditItemScreen(
                existingItem = editItem,
                onSave = { title, content, type ->
                    viewModel.saveItem(
                        id = editItem?.id,
                        title = title,
                        content = content,
                        type = type,
                    ) { savedId ->
                        screen = when (type) {
                            ItemType.QR_CODE -> AppScreen.QrDetail(savedId)
                            ItemType.TEXT -> AppScreen.TextDetail(savedId)
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
                items = items,
                surfacesPlacedOnWatch = surfacesPlacedOnWatch,
                slotAssignments = slotAssignments,
                onSlotClick = { slot -> screen = AppScreen.PickSlotItem(slot) },
            )
        }

        is AppScreen.PickSlotItem -> {
            val pickSlotScreen = screen as AppScreen.PickSlotItem
            SlotItemPickerScreen(
                slot = pickSlotScreen.slot,
                items = items,
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
