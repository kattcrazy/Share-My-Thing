package com.sharemyththing.sync

import android.content.Context
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.Node
import com.google.android.gms.wearable.Wearable
import com.sharemyththing.data.ItemsRepository
import com.sharemyththing.data.SurfaceUpdateListener
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

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
    private val pendingResponses = ConcurrentHashMap<Int, PendingResponse>()
    private val syncMutex = Mutex()

    private data class PendingResponse(
        val onComplete: (Result<ByteArray>) -> Unit,
    )

    init {
        val messageClient = Wearable.getMessageClient(appContext)
        messageClient.addListener { messageEvent ->
            if (messageEvent.path != SyncPaths.RESPONSE) {
                return@addListener
            }
            pendingResponses.remove(messageEvent.requestId)?.onComplete?.invoke(
                Result.success(messageEvent.data),
            )
        }
    }

    suspend fun syncWithWatch(): SyncResult = syncMutex.withLock {
        runCatching {
            val node = findWatchNode() ?: return SyncResult.NoWatchConnected
            val localPayload = itemsRepository.buildSyncPayload()
            val responseBytes = sendMessageAndAwaitResponse(
                nodeId = node.id,
                path = SyncPaths.REQUEST,
                data = localPayload.toJsonBytes(),
            )
            val mergedPayload = SyncPayload.fromJsonBytes(responseBytes)
            val affectedSlots = itemsRepository.applySyncPayload(mergedPayload)
            if (affectedSlots.isNotEmpty()) {
                surfaceUpdateListener?.onSurfaceUpdatesNeeded(affectedSlots)
            }
            SyncResult.Success
        }.getOrElse { error ->
            SyncResult.Error(error.message ?: "Sync failed")
        }
    }

    suspend fun handleIncomingSyncRequest(sourceNodeId: String, requestBytes: ByteArray) =
        syncMutex.withLock {
            val remotePayload = SyncPayload.fromJsonBytes(requestBytes)
            val mergedPayload = SyncEngine.performSync(
                repository = itemsRepository,
                remotePayload = remotePayload,
                surfaceUpdateListener = surfaceUpdateListener,
            )
            Wearable.getMessageClient(appContext)
                .sendMessage(sourceNodeId, SyncPaths.RESPONSE, mergedPayload.toJsonBytes())
                .await()
        }

    private suspend fun findWatchNode(): Node? {
        val nodes = Wearable.getNodeClient(appContext).connectedNodes.await()
        return nodes.firstOrNull { it.isNearby } ?: nodes.firstOrNull()
    }

    private suspend fun sendMessageAndAwaitResponse(
        nodeId: String,
        path: String,
        data: ByteArray,
    ): ByteArray = withTimeout(SYNC_RESPONSE_TIMEOUT_MS) {
        suspendCoroutine { continuation ->
            val messageClient = Wearable.getMessageClient(appContext)
            messageClient.sendMessage(nodeId, path, data)
                .addOnSuccessListener { requestId ->
                    pendingResponses[requestId] = PendingResponse { result ->
                        pendingResponses.remove(requestId)
                        result.fold(
                            onSuccess = { continuation.resume(it) },
                            onFailure = { continuation.resumeWithException(it) },
                        )
                    }
                }
                .addOnFailureListener { error ->
                    continuation.resumeWithException(error)
                }
        }
    }

    private companion object {
        const val SYNC_RESPONSE_TIMEOUT_MS = 30_000L
    }
}
