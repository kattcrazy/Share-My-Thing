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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

object PeerAvailability {
    private fun capabilityUri(): Uri = Uri.parse("wear://*/${SyncPaths.CAPABILITY}")

    fun observePeerConnected(context: Context): Flow<Boolean> {
        if (!WearSyncSupport.isSupported(context)) {
            return kotlinx.coroutines.flow.flowOf(false)
        }
        return callbackFlow {
        val appContext = context.applicationContext
        val capabilityClient = Wearable.getCapabilityClient(appContext)
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

        val listener = CapabilityClient.OnCapabilityChangedListener { capabilityInfo ->
            scope.launch {
                trySend(hasReachablePeer(appContext, capabilityInfo.nodes.map { it.id }.toSet()))
            }
        }
        capabilityClient.addListener(
            listener,
            capabilityUri(),
            CapabilityClient.FILTER_REACHABLE,
        )
        scope.launch {
            trySend(hasReachablePeer(appContext, findReachablePeerNodeIds(appContext)))
        }
        awaitClose {
            scope.cancel()
            capabilityClient.removeListener(listener)
        }
        }.distinctUntilChanged()
    }

    suspend fun findPeerNode(context: Context): Node? {
        if (!WearSyncSupport.isSupported(context)) {
            return null
        }
        val appContext = context.applicationContext
        val localNodeId = Wearable.getNodeClient(appContext).localNode.await().id
        val capabilityNodes = runCatching {
            Wearable.getCapabilityClient(appContext)
                .getCapability(SyncPaths.CAPABILITY, CapabilityClient.FILTER_REACHABLE)
                .await()
                .nodes
        }.getOrDefault(emptySet())
        capabilityNodes.firstOrNull { it.id != localNodeId }?.let { return it }
        val connectedNodes = Wearable.getNodeClient(appContext).connectedNodes.await()
        return connectedNodes.firstOrNull { it.isNearby && it.id != localNodeId }
            ?: connectedNodes.firstOrNull { it.id != localNodeId }
    }

    private suspend fun findReachablePeerNodeIds(context: Context): Set<String> {
        val appContext = context.applicationContext
        val localNodeId = Wearable.getNodeClient(appContext).localNode.await().id
        val capabilityNodes = runCatching {
            Wearable.getCapabilityClient(appContext)
                .getCapability(SyncPaths.CAPABILITY, CapabilityClient.FILTER_REACHABLE)
                .await()
                .nodes
                .map { it.id }
                .toSet()
        }.getOrDefault(emptySet())
        capabilityNodes.firstOrNull { it != localNodeId }?.let { return setOf(it) }
        val connectedNodes = Wearable.getNodeClient(appContext).connectedNodes.await()
        return connectedNodes
            .firstOrNull { it.isNearby && it.id != localNodeId }
            ?.let { setOf(it.id) }
            ?: connectedNodes.firstOrNull { it.id != localNodeId }?.let { setOf(it.id) }
            ?: emptySet()
    }

    private suspend fun hasReachablePeer(context: Context, nodeIds: Set<String>): Boolean {
        if (nodeIds.isEmpty()) return false
        val localNodeId = Wearable.getNodeClient(context.applicationContext).localNode.await().id
        return nodeIds.any { it != localNodeId }
    }
}
