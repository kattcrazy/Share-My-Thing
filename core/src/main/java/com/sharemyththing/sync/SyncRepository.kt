package com.sharemyththing.sync

import android.content.Context
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.Wearable
import com.sharemyththing.data.ItemsRepository
import com.sharemyththing.data.SurfaceUpdateListener
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
) {
    private val appContext = context.applicationContext
    /** Protects local DB merge/apply only. Must not be held while waiting on the data layer. */
    private val dbMutex = Mutex()
    /** Prevents overlapping outgoing sync requests from the same device. */
    private val outgoingMutex = Mutex()

    init {
        val rpcService = MessageClient.RpcService { _, _, request ->
            Tasks.forResult(
                runBlocking {
                    mergeIncomingRequest(request).toJsonBytes()
                },
            )
        }
        Wearable.getMessageClient(appContext).addRpcService(rpcService, SyncPaths.REQUEST)
    }

    suspend fun syncWithWatch(): SyncResult = outgoingMutex.withLock {
        runCatching {
            val node = PeerAvailability.findPeerNode(appContext) ?: return SyncResult.NoWatchConnected
            val localPayload = itemsRepository.buildSyncPayload()
            val responseBytes = withTimeout(SYNC_RESPONSE_TIMEOUT_MS) {
                Wearable.getMessageClient(appContext)
                    .sendRequest(node.id, SyncPaths.REQUEST, localPayload.toJsonBytes())
                    .await()
            }
            dbMutex.withLock {
                val mergedPayload = SyncPayload.fromJsonBytes(responseBytes)
                val affectedSlots = itemsRepository.applySyncPayload(mergedPayload)
                if (affectedSlots.isNotEmpty()) {
                    surfaceUpdateListener?.onSurfaceUpdatesNeeded(affectedSlots)
                }
            }
            SyncResult.Success
        }.getOrElse { error ->
            SyncResult.Error(error.message ?: "Sync failed")
        }
    }

    private suspend fun mergeIncomingRequest(requestBytes: ByteArray): SyncPayload {
        return dbMutex.withLock {
            val remotePayload = SyncPayload.fromJsonBytes(requestBytes)
            SyncEngine.performSync(
                repository = itemsRepository,
                remotePayload = remotePayload,
                surfaceUpdateListener = surfaceUpdateListener,
            )
        }
    }

    private companion object {
        const val SYNC_RESPONSE_TIMEOUT_MS = 30_000L
    }
}
