package kattcrazy.sharemything.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import kattcrazy.sharemything.data.DisplayItem
import kattcrazy.sharemything.data.ItemType
import kattcrazy.sharemything.data.SurfaceSlot
import kattcrazy.sharemything.ui.ItemsViewModel
import kattcrazy.sharemything.ui.LocalMotionEnabled
import kattcrazy.sharemything.ui.about.AboutScreen
import kattcrazy.sharemything.ui.detail.QrDetailScreen
import kattcrazy.sharemything.ui.detail.QrTipsScreen
import kattcrazy.sharemything.ui.detail.TextDetailScreen
import kattcrazy.sharemything.ui.edit.EditItemScreen
import kattcrazy.sharemything.ui.list.ItemListScreen
import kattcrazy.sharemything.ui.settings.AppShortcutsScreen
import kattcrazy.sharemything.ui.settings.PhoneWidgetsScreen
import kattcrazy.sharemything.ui.settings.SlotItemPickerScreen
import kattcrazy.sharemything.ui.settings.WatchComplicationsScreen
import kattcrazy.sharemything.ui.settings.WatchTilesScreen

@OptIn(ExperimentalSharedTransitionApi::class)
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
    val motionEnabled = LocalMotionEnabled.current

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

    fun parentScreenForSlot(slot: SurfaceSlot): AppScreen = when {
        slot.isPhoneWidget -> AppScreen.PhoneWidgets
        slot.isShortcut -> AppScreen.AppShortcuts
        slot.isTile -> AppScreen.WatchTiles
        slot.isComplication -> AppScreen.WatchComplications
        else -> AppScreen.List
    }

    fun navigateBack() {
        screen = when (val current = screen) {
            AppScreen.QrTips -> detailItem?.let { AppScreen.QrDetail(it.id) } ?: AppScreen.List
            is AppScreen.PickSlotItem -> parentScreenForSlot(current.slot)
            AppScreen.About -> AppScreen.List
            AppScreen.PhoneWidgets, AppScreen.AppShortcuts, AppScreen.WatchTiles, AppScreen.WatchComplications -> AppScreen.List
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

    SharedTransitionLayout {
        AnimatedContent(
            targetState = screen,
            transitionSpec = {
                if (motionEnabled) {
                    fadeIn(animationSpec = tween(120)) togetherWith
                        fadeOut(animationSpec = tween(90))
                } else {
                    EnterTransition.None togetherWith ExitTransition.None
                }
            },
            contentKey = { it.contentKey() },
            label = "appScreen",
        ) { targetScreen ->
            CompositionLocalProvider(
                LocalSharedTransitionScope provides this@SharedTransitionLayout,
                LocalNavAnimatedVisibilityScope provides this,
            ) {
                val destinationKey = targetScreen.destinationSharedKey()
                val screenModifier = if (destinationKey != null) {
                    Modifier
                        .fillMaxSize()
                        .navSharedBounds(
                            key = destinationKey,
                            // Match FAB ScaleToBounds so Add↔edit doesn't crush the label.
                            scaleContent = destinationKey == NavSharedKeys.edit(null),
                            zIndexInOverlay = if (destinationKey == NavSharedKeys.edit(null)) 2f else 0f,
                        )
                } else {
                    Modifier.fillMaxSize()
                }

                Box(modifier = screenModifier) {
                    when (targetScreen) {
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
                                onAppShortcutsClick = { screen = AppScreen.AppShortcuts },
                                onWatchTilesClick = { screen = AppScreen.WatchTiles },
                                onWatchComplicationsClick = { screen = AppScreen.WatchComplications },
                                onAboutClick = { screen = AppScreen.About },
                                onSetVisibleOnWatch = viewModel::setVisibleOnWatch,
                                onCommitItemOrder = viewModel::commitItemOrder,
                                onSyncClick = { viewModel.syncWithWatch(manual = true) },
                                syncFeedback = viewModel.syncFeedback,
                                onSyncFeedbackShown = viewModel::clearSyncFeedback,
                                onExportBackup = viewModel::exportBackup,
                                onImportBackupSelected = viewModel::loadImportBackup,
                                onConfirmImport = viewModel::confirmImport,
                                onCancelPendingImport = viewModel::cancelPendingImport,
                                importExportFeedback = viewModel.importExportFeedback,
                                onImportExportFeedbackShown = viewModel::clearImportExportFeedback,
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
                            val editingItemId = targetScreen.itemId
                            EditItemScreen(
                                existingItem = editItem,
                                onSave = { title, content, type, icon ->
                                    viewModel.saveItem(
                                        id = editingItemId ?: editItem?.id,
                                        title = title,
                                        content = content,
                                        type = type,
                                        icon = icon,
                                    ) { savedItem ->
                                        detailItem = savedItem
                                        editItem = null
                                        screen = when (savedItem.type) {
                                            ItemType.TEXT -> AppScreen.TextDetail(savedItem.id)
                                            ItemType.QR_CODE, ItemType.BOTH -> AppScreen.QrDetail(savedItem.id)
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

                        AppScreen.PhoneWidgets -> {
                            PhoneWidgetsScreen(
                                items = items,
                                slotAssignments = slotAssignments,
                                onSlotClick = { slot -> screen = AppScreen.PickSlotItem(slot) },
                                onBack = ::navigateBack,
                            )
                        }

                        AppScreen.AppShortcuts -> {
                            AppShortcutsScreen(
                                items = items,
                                slotAssignments = slotAssignments,
                                onSlotClick = { slot -> screen = AppScreen.PickSlotItem(slot) },
                                onBack = ::navigateBack,
                            )
                        }

                        AppScreen.WatchTiles -> {
                            WatchTilesScreen(
                                items = watchVisibleItems,
                                slotAssignments = slotAssignments,
                                onSlotClick = { slot -> screen = AppScreen.PickSlotItem(slot) },
                                onBack = ::navigateBack,
                            )
                        }

                        AppScreen.WatchComplications -> {
                            WatchComplicationsScreen(
                                items = watchVisibleItems,
                                slotAssignments = slotAssignments,
                                onSlotClick = { slot -> screen = AppScreen.PickSlotItem(slot) },
                                onBack = ::navigateBack,
                            )
                        }

                        is AppScreen.PickSlotItem -> {
                            val pickSlotScreen = targetScreen
                            val pickerItems = when {
                                pickSlotScreen.slot.isPhoneWidget || pickSlotScreen.slot.isShortcut -> items
                                else -> watchVisibleItems
                            }
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
            }
        }
    }
}
