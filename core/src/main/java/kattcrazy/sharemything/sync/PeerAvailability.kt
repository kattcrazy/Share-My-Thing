package kattcrazy.sharemything.sync

import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.Node
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

object PeerAvailability {
    private fun capabilityUri(): Uri = Uri.parse("wear://*/${SyncPaths.CAPABILITY}")

    fun observePeerConnected(context: Context): Flow<Boolean> {
        if (!WearSyncSupport.isSupported(context)) {
            return flowOf(false)
        }
        return flow {
            if (!WearSyncSupport.isWearDataLayerAvailable(context)) {
                emit(false)
                return@flow
            }
            emitAll(observePeerConnectedInternal(context))
        }.distinctUntilChanged()
    }

    private fun observePeerConnectedInternal(context: Context): Flow<Boolean> = callbackFlow {
        val appContext = context.applicationContext
        val capabilityClient = Wearable.getCapabilityClient(appContext)
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

        suspend fun emitCurrentState() {
            val connected = runCatching {
                hasReachablePeer(appContext, findReachablePeerNodeIds(appContext))
            }.getOrDefault(false)
            trySend(connected)
        }

        val capabilityListener = CapabilityClient.OnCapabilityChangedListener {
            scope.launch { emitCurrentState() }
        }

        val listenerRegistered = runCatching {
            capabilityClient.addListener(
                capabilityListener,
                capabilityUri(),
                CapabilityClient.FILTER_REACHABLE,
            )
        }.isSuccess

        if (!listenerRegistered) {
            trySend(false)
            awaitClose { scope.cancel() }
            return@callbackFlow
        }

        val pollIntervalMs = peerPollIntervalMs(appContext)
        scope.launch {
            emitCurrentState()
            if (pollIntervalMs <= 0L) return@launch
            while (true) {
                delay(pollIntervalMs)
                emitCurrentState()
            }
        }

        awaitClose {
            scope.cancel()
            runCatching { capabilityClient.removeListener(capabilityListener) }
        }
    }

    suspend fun findPeerNode(context: Context): Node? {
        if (!WearSyncSupport.isWearDataLayerAvailable(context)) {
            return null
        }
        return runCatching {
            val appContext = context.applicationContext
            val localNodeId = Wearable.getNodeClient(appContext).localNode.await().id
            findPeerInCapability(appContext, localNodeId, CapabilityClient.FILTER_REACHABLE)
                ?: if (isWatch(appContext)) {
                    null
                } else {
                    findNearbyConnectedPeer(appContext, localNodeId)
                }
        }.getOrNull()
    }

    suspend fun hasSyncPeer(context: Context): Boolean = findPeerNode(context) != null

    private suspend fun findPeerInCapability(
        context: Context,
        localNodeId: String,
        filter: Int,
    ): Node? {
        return runCatching {
            Wearable.getCapabilityClient(context)
                .getCapability(SyncPaths.CAPABILITY, filter)
                .await()
                .nodes
                .firstOrNull { it.id != localNodeId }
        }.getOrNull()
    }

    private suspend fun findReachablePeerNodeIds(context: Context): Set<String> {
        return runCatching {
            val appContext = context.applicationContext
            val localNodeId = Wearable.getNodeClient(appContext).localNode.await().id
            val capabilityNodes = capabilityPeerIds(appContext, localNodeId, CapabilityClient.FILTER_REACHABLE)
            if (capabilityNodes.isNotEmpty()) return capabilityNodes
            if (isWatch(appContext)) return emptySet()
            findNearbyConnectedPeerIds(appContext, localNodeId)
        }.getOrDefault(emptySet())
    }

    private suspend fun findNearbyConnectedPeer(context: Context, localNodeId: String): Node? {
        val appContext = context.applicationContext
        val connectedNodes = Wearable.getNodeClient(appContext).connectedNodes.await()
        return connectedNodes.firstOrNull { node ->
            node.id != localNodeId && node.isNearby
        }
    }

    private suspend fun findNearbyConnectedPeerIds(context: Context, localNodeId: String): Set<String> {
        val appContext = context.applicationContext
        return Wearable.getNodeClient(appContext).connectedNodes.await()
            .filter { node -> node.id != localNodeId && node.isNearby }
            .map { it.id }
            .toSet()
    }

    private suspend fun capabilityPeerIds(
        context: Context,
        localNodeId: String,
        filter: Int,
    ): Set<String> {
        return runCatching {
            Wearable.getCapabilityClient(context)
                .getCapability(SyncPaths.CAPABILITY, filter)
                .await()
                .nodes
                .map { it.id }
                .filter { it != localNodeId }
                .toSet()
        }.getOrDefault(emptySet())
    }

    private fun peerPollIntervalMs(context: Context): Long {
        return if (isWatch(context)) WATCH_PEER_POLL_INTERVAL_MS else PHONE_PEER_POLL_INTERVAL_MS
    }

    private fun isWatch(context: Context): Boolean =
        context.packageManager.hasSystemFeature(PackageManager.FEATURE_WATCH)

    private suspend fun hasReachablePeer(context: Context, nodeIds: Set<String>): Boolean =
        nodeIds.isNotEmpty()

    private const val WATCH_PEER_POLL_INTERVAL_MS = 60_000L
    private const val PHONE_PEER_POLL_INTERVAL_MS = 30_000L
}
