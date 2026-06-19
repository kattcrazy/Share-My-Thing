package com.sharemyththing

import android.app.Application
import com.sharemyththing.data.ItemsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class ShareMyThingApplication : Application() {
    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    lateinit var repository: ItemsRepository
        private set

    override fun onCreate() {
        super.onCreate()
        repository = ItemsRepository(this)
        applicationScope.launch {
            repository.initializeOnLaunch()
            runCatching { repository.syncPlacedSurfacesFromSystem() }
        }
    }
}
