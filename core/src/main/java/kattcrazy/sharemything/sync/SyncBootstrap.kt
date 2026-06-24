package kattcrazy.sharemything.sync

import android.content.Context
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

object SyncBootstrap {
    fun start(
        scope: CoroutineScope,
        context: Context,
        syncRepository: SyncRepository,
        onInitialSyncComplete: (suspend (SyncResult) -> Unit)? = null,
    ) {
        scope.launch {
            if (!WearSyncSupport.isWearDataLayerAvailable(context)) return@launch
            syncRepository.ensureReady()
            runCatching {
                Wearable.getCapabilityClient(context.applicationContext)
                    .addLocalCapability(SyncPaths.CAPABILITY)
                    .await()
            }
            delay(1_000)
            if (!PeerAvailability.hasSyncPeer(context)) return@launch
            val result = syncRepository.syncWithWatch(force = true)
            if (result != SyncResult.NoWatchConnected) {
                onInitialSyncComplete?.invoke(result)
            }
        }
    }
}
