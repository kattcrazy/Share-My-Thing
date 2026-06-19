package com.sharemyththing.data

import android.content.Context
import com.sharemyththing.sync.SyncItemRecord
import com.sharemyththing.sync.SyncPayload
import com.sharemyththing.sync.SyncSlotAssignment
import com.sharemyththing.util.QrCodeGenerator
import kotlinx.coroutines.flow.Flow

class ItemsRepository(
    context: Context,
    private val surfaceUpdateListener: SurfaceUpdateListener? = null,
) {
    /** Invoked after any local mutation that should sync to the paired device. Set from Application. */
    var onLocalDataChanged: (suspend () -> Unit)? = null
    private val appContext = context.applicationContext
    private val dao = AppDatabase.getInstance(appContext).displayItemDao()
    val surfacePreferences = SurfacePreferences(appContext)

    val items: Flow<List<DisplayItem>> = dao.observeAll()
    val slotAssignments: Flow<Map<SurfaceSlot, Long?>> = surfacePreferences.assignments
    val surfacesPlacedOnWatch: Flow<Set<SurfaceSlot>> = surfacePreferences.placedOnWatch

    suspend fun getItem(id: Long): DisplayItem? = dao.getById(id)

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
                    sortOrder = item.sortOrder,
                    updatedAtMillis = now,
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
        val now = System.currentTimeMillis()
        orderedIds.forEachIndexed { index, id ->
            val item = dao.getById(id) ?: return@forEachIndexed
            if (item.sortOrder != index) {
                dao.update(item.copy(sortOrder = index, updatedAtMillis = now))
            }
        }
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
                sortOrder = item.sortOrder,
                updatedAtMillis = item.updatedAtMillis,
                deleted = item.deleted,
            )
        }
        val slotAssignments = SurfaceSlot.all.mapNotNull { slot ->
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
                    sortOrder = record.sortOrder,
                    updatedAtMillis = record.updatedAtMillis,
                    deleted = record.deleted,
                ),
            )
            if (!record.deleted && record.uuid.isNotBlank()) {
                dao.getByUuid(record.uuid)?.let { item ->
                    uuidToId[record.uuid] = item.id
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
