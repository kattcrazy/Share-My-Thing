package kattcrazy.sharemything.data

import android.content.Context
import kattcrazy.sharemything.sync.BackupPayload
import kattcrazy.sharemything.sync.ImportMode
import kattcrazy.sharemything.sync.SyncItemRecord
import kattcrazy.sharemything.sync.SyncMerger
import kattcrazy.sharemything.sync.SyncPayload
import kattcrazy.sharemything.sync.SyncSlotAssignment
import kattcrazy.sharemything.util.QrCodeGenerator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class ItemsRepository(
    context: Context,
    private val surfaceUpdateListener: SurfaceUpdateListener? = null,
) {
    var onLocalDataChanged: (suspend () -> Unit)? = null
    private val appContext = context.applicationContext
    private val dao = AppDatabase.getInstance(appContext).displayItemDao()
    val surfacePreferences = SurfacePreferences(appContext)

    val items: Flow<List<DisplayItem>> = dao.observeAll()
    val watchVisibleItems: Flow<List<DisplayItem>> =
        items.map { list -> list.filter { it.visibleOnWatch } }
    val slotAssignments: Flow<Map<SurfaceSlot, Long?>> = surfacePreferences.assignments
    val surfacesPlacedOnWatch: Flow<Set<SurfaceSlot>> = surfacePreferences.placedOnWatch

    suspend fun getItem(id: Long): DisplayItem? = dao.getById(id)

    suspend fun setVisibleOnWatch(itemId: Long, visible: Boolean) {
        val item = dao.getById(itemId) ?: return
        if (item.visibleOnWatch == visible) return
        val now = System.currentTimeMillis()
        dao.update(item.copy(visibleOnWatch = visible, updatedAtMillis = now))
        val affectedSlots = if (!visible) {
            clearWatchSlotsForItem(itemId, now)
        } else {
            emptyList()
        }
        requestSurfaceUpdates(affectedSlots)
        notifyLocalDataChanged()
    }

    suspend fun upsert(item: DisplayItem): Long {
        val now = System.currentTimeMillis()
        val trimmedContent = item.content.trim()
        val existing = when {
            item.id != 0L -> dao.getById(item.id)
            item.uuid.isNotBlank() -> dao.getByUuid(item.uuid)
            else -> null
        }
        val id = if (existing == null) {
            val toInsert = DisplayItem.newItem(
                title = item.title.trim(),
                content = trimmedContent,
                type = item.type,
                icon = item.icon,
                sortOrder = item.sortOrder,
                nowMillis = now,
            )
            dao.insert(toInsert)
        } else {
            dao.update(
                existing.copy(
                    title = item.title.trim(),
                    content = trimmedContent,
                    type = item.type,
                    icon = item.icon,
                    sortOrder = item.sortOrder,
                    updatedAtMillis = now,
                    visibleOnWatch = item.visibleOnWatch,
                ),
            )
            existing.id
        }
        existing?.content?.let { QrCodeGenerator.invalidateCacheForContent(it) }
        QrCodeGenerator.invalidateCacheForContent(trimmedContent)
        requestSurfaceUpdatesForItem(id)
        notifyLocalDataChanged()
        return id
    }

    suspend fun delete(item: DisplayItem) {
        val affectedSlots = slotsAssignedTo(item.id)
        val now = System.currentTimeMillis()
        dao.softDelete(item.id, now)
        SurfaceSlot.all.forEach { slot ->
            if (surfacePreferences.getItemId(slot) == item.id) {
                surfacePreferences.setItemId(slot, null, updatedAtMillis = now)
            }
        }
        QrCodeGenerator.invalidateCacheForContent(item.content)
        requestSurfaceUpdates(affectedSlots)
        notifyLocalDataChanged()
    }

    suspend fun reorderItems(orderedIds: List<Long>) {
        if (orderedIds.isEmpty()) return
        dao.reorderItems(orderedIds, System.currentTimeMillis())
        notifyLocalDataChanged()
    }

    suspend fun setSlotItemId(slot: SurfaceSlot, id: Long?) {
        surfacePreferences.setItemId(slot, id)
        requestSurfaceUpdate(slot)
        notifyLocalDataChanged()
    }

    suspend fun buildSyncPayload(): SyncPayload {
        val itemsByUuid = dao.getAllIncludingDeleted()
            .filter { it.uuid.isNotBlank() }
            .associate { item ->
            item.uuid to SyncItemRecord(
                uuid = item.uuid,
                title = item.title,
                content = item.content,
                type = item.type,
                icon = item.icon,
                sortOrder = item.sortOrder,
                updatedAtMillis = item.updatedAtMillis,
                deleted = item.deleted,
                visibleOnWatch = item.visibleOnWatch,
            )
        }
        val slotAssignments = SurfaceSlot.syncableSlots.mapNotNull { slot ->
            val state = surfacePreferences.getAssignmentState(slot)
            val itemUuid = state.itemId?.let { dao.getById(it)?.uuid }
            SyncSlotAssignment(
                slot = slot.name,
                itemUuid = itemUuid,
                updatedAtMillis = state.updatedAtMillis,
            )
        }
        return SyncPayload(items = itemsByUuid.values.toList(), slotAssignments = slotAssignments)
    }

    suspend fun buildBackupPayload(includeSlotAssignments: Boolean): BackupPayload {
        val sync = buildSyncPayload()
        return BackupPayload(
            exportedAtMillis = System.currentTimeMillis(),
            includesSlotAssignments = includeSlotAssignments,
            syncPayload = if (includeSlotAssignments) {
                sync
            } else {
                sync.copy(
                    items = sync.items.filter { !it.deleted },
                    slotAssignments = emptyList(),
                )
            },
        )
    }

    suspend fun importBackup(backup: BackupPayload, mode: ImportMode) {
        val payload = backup.syncPayload.copy(
            items = backup.syncPayload.items.filter { !it.deleted && it.uuid.isNotBlank() },
        )
        val applySlotAssignments = backup.includesSlotAssignments && payload.slotAssignments.isNotEmpty()
        val affectedSlots = when (mode) {
            ImportMode.REPLACE -> replaceWithImport(payload, applySlotAssignments)
            ImportMode.MERGE -> mergeWithImport(payload, applySlotAssignments)
            ImportMode.ADD -> addAsNewImport(payload)
        }
        requestSurfaceUpdates(affectedSlots)
        notifyLocalDataChanged()
    }

    private suspend fun replaceWithImport(payload: SyncPayload, applySlotAssignments: Boolean): Set<SurfaceSlot> {
        val now = System.currentTimeMillis()
        dao.getAllActive().forEach { item ->
            dao.softDelete(item.id, now)
        }
        SurfaceSlot.all.forEach { slot ->
            surfacePreferences.setItemId(slot, null, updatedAtMillis = now)
        }
        val stampedItems = payload.items.mapIndexed { index, item ->
            item.copy(
                deleted = false,
                updatedAtMillis = now + index + 1,
            )
        }
        val stampedSlots = if (applySlotAssignments) {
            val slotTimestamp = now + stampedItems.size + 1
            payload.slotAssignments.map { assignment ->
                assignment.copy(updatedAtMillis = slotTimestamp)
            }
        } else {
            emptyList()
        }
        return applySyncPayload(
            SyncPayload(
                items = stampedItems,
                slotAssignments = stampedSlots,
            ),
        )
    }

    private suspend fun mergeWithImport(payload: SyncPayload, mergeSlotAssignments: Boolean): Set<SurfaceSlot> {
        val local = buildSyncPayload()
        val importPayload = if (mergeSlotAssignments) {
            payload
        } else {
            payload.copy(slotAssignments = emptyList())
        }
        val merged = SyncMerger.merge(local, importPayload)
        return applySyncPayload(merged)
    }

    private suspend fun addAsNewImport(payload: SyncPayload): Set<SurfaceSlot> {
        val now = System.currentTimeMillis()
        val nextSortOrder = (dao.getAllActive().maxOfOrNull { it.sortOrder } ?: -1) + 1
        val remappedItems = payload.items.mapIndexed { index, item ->
            item.copy(
                uuid = UUID.randomUUID().toString(),
                deleted = false,
                sortOrder = nextSortOrder + index,
                updatedAtMillis = now + index + 1,
            )
        }
        return applySyncPayload(
            SyncPayload(
                items = remappedItems,
                slotAssignments = emptyList(),
            ),
        )
    }

    suspend fun applySyncPayload(payload: SyncPayload): Set<SurfaceSlot> {
        val affectedSlots = mutableSetOf<SurfaceSlot>()
        val uuidToId = mutableMapOf<String, Long>()

        payload.items.forEach { record ->
            dao.applySyncRecord(
                SyncRecordApply(
                    uuid = record.uuid,
                    title = record.title,
                    content = record.content.trim(),
                    type = record.type,
                    icon = record.icon,
                    sortOrder = record.sortOrder,
                    updatedAtMillis = record.updatedAtMillis,
                    deleted = record.deleted,
                    visibleOnWatch = record.visibleOnWatch,
                ),
            )
            if (!record.deleted && record.uuid.isNotBlank()) {
                dao.getByUuid(record.uuid)?.let { item ->
                    uuidToId[record.uuid] = item.id
                    if (!record.visibleOnWatch) {
                        affectedSlots.addAll(clearWatchSlotsForItem(item.id, record.updatedAtMillis))
                    }
                }
                QrCodeGenerator.invalidateCacheForContent(record.content.trim())
            } else if (record.deleted && record.uuid.isNotBlank()) {
                dao.getByUuid(record.uuid)?.let { existing ->
                    SurfaceSlot.all.filter { surfacePreferences.getItemId(it) == existing.id }.forEach { slot ->
                        surfacePreferences.applyAssignment(slot, null, record.updatedAtMillis)
                        affectedSlots.add(slot)
                    }
                }
            }
        }

        dao.getAllIncludingDeleted().filter { !it.deleted }.forEach { item ->
            uuidToId.putIfAbsent(item.uuid, item.id)
        }

        payload.slotAssignments.forEach { assignment ->
            val slot = SurfaceSlot.fromName(assignment.slot) ?: return@forEach
            val itemId = assignment.itemUuid?.let { uuidToId[it] }
            val previousItemId = surfacePreferences.getItemId(slot)
            surfacePreferences.applyAssignment(
                slot = slot,
                itemId = itemId,
                updatedAtMillis = assignment.updatedAtMillis,
            )
            if (previousItemId != itemId) {
                affectedSlots.add(slot)
            }
        }

        return affectedSlots
    }

    private suspend fun clearWatchSlotsForItem(itemId: Long, updatedAtMillis: Long): List<SurfaceSlot> {
        val cleared = mutableListOf<SurfaceSlot>()
        SurfaceSlot.watchSurfaces.forEach { slot ->
            if (surfacePreferences.getItemId(slot) == itemId) {
                surfacePreferences.setItemId(slot, null, updatedAtMillis = updatedAtMillis)
                cleared.add(slot)
            }
        }
        return cleared
    }

    private suspend fun requestSurfaceUpdatesForItem(itemId: Long) {
        requestSurfaceUpdates(slotsAssignedTo(itemId))
    }

    private suspend fun slotsAssignedTo(itemId: Long): List<SurfaceSlot> =
        SurfaceSlot.all.filter { surfacePreferences.getItemId(it) == itemId }

    private fun requestSurfaceUpdate(slot: SurfaceSlot) {
        requestSurfaceUpdates(listOf(slot))
    }

    private fun requestSurfaceUpdates(slots: Collection<SurfaceSlot>) {
        if (slots.isEmpty()) return
        surfaceUpdateListener?.onSurfaceUpdatesNeeded(slots)
    }

    private suspend fun notifyLocalDataChanged() {
        onLocalDataChanged?.invoke()
    }
}
