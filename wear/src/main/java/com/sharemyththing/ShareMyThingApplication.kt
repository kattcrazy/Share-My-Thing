package com.sharemyththing

import android.app.Application
import com.sharemyththing.data.ItemsRepository
import com.sharemyththing.sync.SyncRepository
import com.sharemyththing.wear.WearSurfaceUpdater
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
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
        syncRepository = SyncRepository(this, repository, surfaceUpdater)
        repository.onLocalDataChanged = {
            applicationScope.launch {
                runCatching { syncRepository.syncWithWatch() }
            }
        }
        applicationScope.launch {
            delay(1_500)
            runCatching { syncRepository.syncWithWatch() }
        }
    }
}
