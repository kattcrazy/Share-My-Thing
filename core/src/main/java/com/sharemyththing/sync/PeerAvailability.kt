package com.sharemyththing.sync

import android.content.Context
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

        scope.launch {
            emitCurrentState()
            delay(2_000)
            emitCurrentState()
            delay(3_000)
            emitCurrentState()
            while (true) {
                delay(10_000)
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
                ?: findPeerInCapability(appContext, localNodeId, CapabilityClient.FILTER_ALL)
                ?: run {
                    val connectedNodes = Wearable.getNodeClient(appContext).connectedNodes.await()
                    connectedNodes.firstOrNull { it.isNearby && it.id != localNodeId }
                        ?: connectedNodes.firstOrNull { it.id != localNodeId }
                }
        }.getOrNull()
    }

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
            val allCapabilityNodes = capabilityPeerIds(appContext, localNodeId, CapabilityClient.FILTER_ALL)
            if (allCapabilityNodes.isNotEmpty()) return allCapabilityNodes
            val connectedNodes = Wearable.getNodeClient(appContext).connectedNodes.await()
            connectedNodes
                .firstOrNull { it.isNearby && it.id != localNodeId }
                ?.let { setOf(it.id) }
                ?: connectedNodes.firstOrNull { it.id != localNodeId }?.let { setOf(it.id) }
                ?: emptySet()
        }.getOrDefault(emptySet())
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

    private suspend fun hasReachablePeer(context: Context, nodeIds: Set<String>): Boolean {
        if (nodeIds.isEmpty()) return false
        return runCatching {
            val localNodeId = Wearable.getNodeClient(context.applicationContext).localNode.await().id
            nodeIds.any { it != localNodeId }
        }.getOrDefault(false)
    }
}
