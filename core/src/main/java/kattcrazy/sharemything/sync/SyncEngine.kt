package kattcrazy.sharemything.sync

import kattcrazy.sharemything.data.ItemsRepository
import kattcrazy.sharemything.data.SurfaceSlot
import kattcrazy.sharemything.data.SurfaceUpdateListener

object SyncEngine {
    suspend fun performSync(
        repository: ItemsRepository,
        remotePayload: SyncPayload,
        surfaceUpdateListener: SurfaceUpdateListener? = null,
        updateSurfaces: Boolean = true,
    ): SyncPayload {
        val localPayload = repository.buildSyncPayload()
        val mergedPayload = SyncMerger.merge(localPayload, remotePayload)
        val affectedSlots = repository.applySyncPayload(mergedPayload)
        if (updateSurfaces && affectedSlots.isNotEmpty()) {
            surfaceUpdateListener?.onSurfaceUpdatesNeeded(affectedSlots)
        }
        return mergedPayload
    }

    suspend fun performSyncWithAffectedSlots(
        repository: ItemsRepository,
        remotePayload: SyncPayload,
    ): Pair<SyncPayload, Set<SurfaceSlot>> {
        val localPayload = repository.buildSyncPayload()
        val mergedPayload = SyncMerger.merge(localPayload, remotePayload)
        val affectedSlots = repository.applySyncPayload(mergedPayload)
        return mergedPayload to affectedSlots
    }
}
