package kattcrazy.sharemything

import android.app.Application
import android.content.ComponentCallbacks2
import android.content.res.Configuration
import kattcrazy.sharemything.data.ItemsRepository
import kattcrazy.sharemything.sync.SyncBootstrap
import kattcrazy.sharemything.sync.SyncFeedbackBridge
import kattcrazy.sharemything.sync.SyncRepository
import kattcrazy.sharemything.sync.WearSyncSupport
import kattcrazy.sharemything.widget.PhoneWidgetUpdater
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
