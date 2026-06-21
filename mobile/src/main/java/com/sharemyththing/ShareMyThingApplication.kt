package com.sharemyththing

import android.app.Application
import android.content.ComponentCallbacks2
import android.content.res.Configuration
import com.sharemyththing.data.ItemsRepository
import com.sharemyththing.sync.SyncBootstrap
import com.sharemyththing.sync.SyncFeedbackBridge
import com.sharemyththing.sync.SyncRepository
import com.sharemyththing.sync.WearSyncSupport
import com.sharemyththing.widget.PhoneWidgetUpdater
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class ShareMyThingApplication : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    lateinit var repository: ItemsRepository
        private set
    lateinit var surfaceUpdater: PhoneWidgetUpdater
        private set
    lateinit var syncRepository: SyncRepository
        private set

    override fun onCreate() {
        super.onCreate()
        surfaceUpdater = PhoneWidgetUpdater(this, applicationScope)
        repository = ItemsRepository(this, surfaceUpdater)
        syncRepository = SyncRepository(this, repository, surfaceUpdater)
        repository.onLocalDataChanged = {
            if (WearSyncSupport.isSupportedAsync(this@ShareMyThingApplication)) {
                SyncFeedbackBridge.emitFailure(syncRepository.syncWithWatch(force = true))
            }
        }
        SyncBootstrap.start(
            scope = applicationScope,
            context = this,
            syncRepository = syncRepository,
            onInitialSyncComplete = { result ->
                SyncFeedbackBridge.emitFailure(result)
            },
        )
        applicationScope.launch {
            surfaceUpdater.requestUpdateAll()
        }
        registerComponentCallbacks(object : ComponentCallbacks2 {
            override fun onConfigurationChanged(newConfig: Configuration) {
                surfaceUpdater.requestUpdateAll()
            }

            override fun onLowMemory() = Unit

            override fun onTrimMemory(level: Int) = Unit
        })
    }
}
