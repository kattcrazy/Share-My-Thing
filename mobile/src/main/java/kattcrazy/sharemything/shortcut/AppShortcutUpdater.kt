package kattcrazy.sharemything.shortcut

import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import kattcrazy.sharemything.R
import kattcrazy.sharemything.data.DisplayItem
import kattcrazy.sharemything.data.ItemType
import kattcrazy.sharemything.data.ItemsRepository
import kattcrazy.sharemything.data.SurfaceSlot
import kattcrazy.sharemything.data.SurfaceUpdateListener
import kattcrazy.sharemything.presentation.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class AppShortcutUpdater(
    private val context: Context,
    private val scope: CoroutineScope,
    private val repositoryProvider: () -> ItemsRepository,
) : SurfaceUpdateListener {
    override fun onSurfaceUpdatesNeeded(slots: Collection<SurfaceSlot>) {
        if (slots.none { it.isShortcut }) return
        scope.launch { publishShortcuts() }
    }

    fun requestUpdateAll() {
        scope.launch { publishShortcuts() }
    }

    private suspend fun publishShortcuts() {
        val shortcutManager = context.getSystemService(ShortcutManager::class.java) ?: return
        val repository = repositoryProvider()
        val shortcuts = SurfaceSlot.phoneShortcuts.mapNotNull { slot ->
            buildShortcut(slot, repository.surfacePreferences.getItemId(slot)?.let { repository.getItem(it) })
        }
        shortcutManager.dynamicShortcuts = shortcuts
    }

    private fun buildShortcut(slot: SurfaceSlot, item: DisplayItem?): ShortcutInfo? {
        if (item == null) return null
        val intent = MainActivity.launchIntent(context, item.id).apply {
            action = Intent.ACTION_VIEW
        }
        return ShortcutInfo.Builder(context, slot.name)
            .setShortLabel(item.title)
            .setLongLabel(item.title)
            .setIcon(Icon.createWithResource(context, item.type.shortcutIconRes()))
            .setIntent(intent)
            .build()
    }

    private fun ItemType.shortcutIconRes(): Int = when (this) {
        ItemType.TEXT -> R.drawable.ic_item_text
        ItemType.QR_CODE, ItemType.BOTH -> R.drawable.ic_item_qr
    }
}
