package kattcrazy.sharemything.sync

import android.content.Context
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.Wearable
import kattcrazy.sharemything.data.ItemsRepository
import kattcrazy.sharemything.data.SurfaceSlot
import kattcrazy.sharemything.data.SurfaceUpdateListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout

sealed interface SyncResult {
    data object Success : SyncResult
    data object NoWatchConnected : SyncResult
    data class Error(val message: String) : SyncResult
}

class SyncRepository(
    private val context: Context,
    private val itemsRepository: ItemsRepository,
    private val surfaceUpdateListener: SurfaceUpdateListener? = null,
    private val minOutgoingSyncIntervalMs: Long = DEFAULT_MIN_OUTGOING_SYNC_INTERVAL_MS,
) {
    private val appContext = context.applicationContext
    // Must not be held while waiting on the data layer.
    private val dbMutex = Mutex()
    private val outgoingMutex = Mutex()
    private val readyMutex = Mutex()
    private val syncScope = CoroutineScope(SupervisorJob() + Dispatchers.IO.limitedParallelism(1))
    @Volatile
    private var isReady = false
    @Volatile
    private var lastOutgoingSyncMs = 0L

    suspend fun ensureReady() {
        if (isReady) return
        readyMutex.withLock {
            if (isReady) return
            if (!WearSyncSupport.isWearDataLayerAvailable(appContext)) return
            val rpcService = MessageClient.RpcService { _, _, request ->
                Tasks.forResult(
                    runBlocking(Dispatchers.IO) {
                        handleIncomingRequest(request)
                    },
                )
            }
            runCatching {
                Wearable.getMessageClient(appContext).addRpcService(rpcService, SyncPaths.REQUEST)
                isReady = true
            }
        }
    }

    suspend fun syncWithWatch(force: Boolean = false): SyncResult {
        if (!force) {
            val elapsed = System.currentTimeMillis() - lastOutgoingSyncMs
            if (elapsed in 1 until minOutgoingSyncIntervalMs) {
                return SyncResult.Success
            }
            if (!outgoingMutex.tryLock()) {
                return SyncResult.Success
            }
        } else {
            outgoingMutex.lock()
        }
        return try {
            performOutgoingSync()
        } finally {
            lastOutgoingSyncMs = System.currentTimeMillis()
            outgoingMutex.unlock()
        }
    }

    private suspend fun performOutgoingSync(): SyncResult {
        if (!WearSyncSupport.isWearDataLayerAvailable(appContext)) {
            return SyncResult.NoWatchConnected
        }
        ensureReady()
        return runCatching {
            val node = PeerAvailability.findPeerNode(appContext) ?: return SyncResult.NoWatchConnected
            val localPayload = itemsRepository.buildSyncPayload()
            val responseBytes = withTimeout(SYNC_RESPONSE_TIMEOUT_MS) {
                Wearable.getMessageClient(appContext)
                    .sendRequest(node.id, SyncPaths.REQUEST, localPayload.toJsonBytes())
                    .await()
            }
            val affectedSlots = dbMutex.withLock {
                val mergedPayload = SyncPayload.fromJsonBytes(responseBytes)
                itemsRepository.applySyncPayload(mergedPayload)
            }
            scheduleSurfaceUpdates(affectedSlots)
            SyncResult.Success
        }.getOrElse { error ->
            SyncResult.Error(error.message ?: "Sync failed")
        }
    }

    private suspend fun handleIncomingRequest(requestBytes: ByteArray): ByteArray {
        val (mergedPayload, affectedSlots) = dbMutex.withLock {
            val remotePayload = SyncPayload.fromJsonBytes(requestBytes)
            SyncEngine.performSyncWithAffectedSlots(
                repository = itemsRepository,
                remotePayload = remotePayload,
            )
        }
        scheduleSurfaceUpdates(affectedSlots)
        return mergedPayload.toJsonBytes()
    }

    private fun scheduleSurfaceUpdates(slots: Collection<SurfaceSlot>) {
        if (slots.isEmpty() || surfaceUpdateListener == null) return
        syncScope.launch {
            surfaceUpdateListener.onSurfaceUpdatesNeeded(slots)
        }
    }

    private companion object {
        const val SYNC_RESPONSE_TIMEOUT_MS = 30_000L
        const val DEFAULT_MIN_OUTGOING_SYNC_INTERVAL_MS = 5_000L
    }
}
