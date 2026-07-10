package kattcrazy.sharemything

import android.app.Application
import kattcrazy.sharemything.data.ItemsRepository
import kattcrazy.sharemything.sync.PeerAvailability
import kattcrazy.sharemything.sync.SyncBootstrap
import kattcrazy.sharemything.sync.SyncRepository
import kattcrazy.sharemything.wear.WearSurfaceUpdater
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class ShareMyThingApplication : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    lateinit var repository: ItemsRepository
        private set
    lateinit var surfaceUpdater: WearSurfaceUpdater
        private set
    lateinit var syncRepository: SyncRepository
        private set

    override fun onCreate() {
        super.onCreate()
        surfaceUpdater = WearSurfaceUpdater(this)
        repository = ItemsRepository(this, surfaceUpdater)
        syncRepository = SyncRepository(
            context = this,
            itemsRepository = repository,
            surfaceUpdateListener = surfaceUpdater,
            minOutgoingSyncIntervalMs = 30_000L,
        )
        repository.onLocalDataChanged = {
            applicationScope.launch {
                if (PeerAvailability.hasSyncPeer(this@ShareMyThingApplication)) {
                    runCatching { syncRepository.syncWithWatch(force = true) }
                }
            }
        }
        SyncBootstrap.start(
            scope = applicationScope,
            context = this,
            syncRepository = syncRepository,
        )
    }
}
