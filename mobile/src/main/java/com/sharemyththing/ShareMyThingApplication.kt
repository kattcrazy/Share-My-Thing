package com.sharemyththing

import android.app.Application
import com.google.android.gms.wearable.Wearable
import com.sharemyththing.data.ItemsRepository
import com.sharemyththing.sync.SyncFeedbackBridge
import com.sharemyththing.sync.SyncPaths
import com.sharemyththing.sync.SyncRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ShareMyThingApplication : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    lateinit var repository: ItemsRepository
        private set
    lateinit var syncRepository: SyncRepository
        private set

    override fun onCreate() {
        super.onCreate()
        repository = ItemsRepository(this)
        syncRepository = SyncRepository(this, repository)
        repository.onLocalDataChanged = {
            applicationScope.launch {
                SyncFeedbackBridge.emitFailure(syncRepository.syncWithWatch())
            }
        }
        applicationScope.launch {
            delay(1_500)
            SyncFeedbackBridge.emitFailure(syncRepository.syncWithWatch())
        }
        applicationScope.launch {
            Wearable.getCapabilityClient(this@ShareMyThingApplication)
                .addLocalCapability(SyncPaths.CAPABILITY)
        }
    }
}
