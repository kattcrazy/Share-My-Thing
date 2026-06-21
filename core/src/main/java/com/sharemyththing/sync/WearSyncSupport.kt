package com.sharemyththing.sync

import android.content.Context
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.tasks.await

object WearSyncSupport {
    /**
     * Fast, non-blocking check suitable for UI gating. Does not wait for Play Services to bind.
     */
    fun isSupported(context: Context): Boolean {
        val result = GoogleApiAvailability.getInstance()
            .isGooglePlayServicesAvailable(context.applicationContext)
        return result == ConnectionResult.SUCCESS
    }

    /**
     * Confirms the Wearable data layer is usable before sync or peer detection.
     * On some phones without the Wear OS companion app, Play Services is present but
     * [Wearable.getNodeClient] calls fail — this probes that path safely.
     */
    suspend fun isSupportedAsync(context: Context): Boolean = isWearDataLayerAvailable(context)

    suspend fun isWearDataLayerAvailable(context: Context): Boolean {
        if (!isSupported(context)) return false
        return runCatching {
            val appContext = context.applicationContext
            GoogleApiAvailability.getInstance()
                .checkApiAvailability(Wearable.getNodeClient(appContext))
                .await()
            Wearable.getNodeClient(appContext).localNode.await()
            true
        }.getOrDefault(false)
    }
}
