package com.sharemyththing.sync

import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import com.sharemyththing.ShareMyThingApplication
import kotlinx.coroutines.runBlocking

class MobileSyncListenerService : WearableListenerService() {
    override fun onMessageReceived(messageEvent: MessageEvent) {
        if (messageEvent.path != SyncPaths.REQUEST) {
            return
        }
        val app = application as ShareMyThingApplication
        runBlocking {
            app.syncRepository.handleIncomingSyncRequest(
                sourceNodeId = messageEvent.sourceNodeId,
                requestBytes = messageEvent.data,
            )
        }
    }
}
