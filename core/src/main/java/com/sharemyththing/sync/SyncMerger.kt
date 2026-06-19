package com.sharemyththing.sync

object SyncMerger {
    fun merge(local: SyncPayload, remote: SyncPayload): SyncPayload {
        val mergedItems = mutableMapOf<String, SyncItemRecord>()
        (local.items + remote.items).forEach { record ->
            val existing = mergedItems[record.uuid]
            if (existing == null || record.updatedAtMillis >= existing.updatedAtMillis) {
                mergedItems[record.uuid] = record
            }
        }

        val mergedSlots = mutableMapOf<String, SyncSlotAssignment>()
        (local.slotAssignments + remote.slotAssignments).forEach { assignment ->
            val existing = mergedSlots[assignment.slot]
            if (existing == null || assignment.updatedAtMillis >= existing.updatedAtMillis) {
                mergedSlots[assignment.slot] = assignment
            }
        }

        return SyncPayload(
            items = mergedItems.values.sortedBy { it.sortOrder },
            slotAssignments = mergedSlots.values.sortedBy { it.slot },
        )
    }
}
