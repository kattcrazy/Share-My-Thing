package com.sharemyththing.sync

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
            if (!WearSyncSupport.isSupportedAsync(context)) return@launch
            syncRepository.ensureReady()
            runCatching {
                Wearable.getCapabilityClient(context.applicationContext)
                    .addLocalCapability(SyncPaths.CAPABILITY)
                    .await()
            }
            delay(1_000)
            val result = syncRepository.syncWithWatch(force = true)
            onInitialSyncComplete?.invoke(result)
        }
    }
}
