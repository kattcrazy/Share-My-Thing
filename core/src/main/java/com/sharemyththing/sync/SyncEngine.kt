package com.sharemyththing.sync

import com.sharemyththing.data.ItemsRepository
import com.sharemyththing.data.SurfaceSlot
import com.sharemyththing.data.SurfaceUpdateListener

object SyncEngine {
    suspend fun performSync(
        repository: ItemsRepository,
        remotePayload: SyncPayload,
        surfaceUpdateListener: SurfaceUpdateListener? = null,
    ): SyncPayload {
        val localPayload = repository.buildSyncPayload()
        val mergedPayload = SyncMerger.merge(localPayload, remotePayload)
        val affectedSlots = repository.applySyncPayload(mergedPayload)
        if (affectedSlots.isNotEmpty()) {
            surfaceUpdateListener?.onSurfaceUpdatesNeeded(affectedSlots)
        }
        return mergedPayload
    }
}
