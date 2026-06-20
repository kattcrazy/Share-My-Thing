package com.sharemyththing.sync

import android.content.Context
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.tasks.await

object WearSyncSupport {
    fun isSupported(context: Context): Boolean {
        val appContext = context.applicationContext
        return runCatching {
            Tasks.await(
                GoogleApiAvailability.getInstance()
                    .checkApiAvailability(Wearable.getNodeClient(appContext)),
            )
            true
        }.getOrDefault(false)
    }

    suspend fun isSupportedAsync(context: Context): Boolean = runCatching {
        GoogleApiAvailability.getInstance()
            .checkApiAvailability(Wearable.getNodeClient(context.applicationContext))
            .await()
        true
    }.getOrDefault(false)
}
