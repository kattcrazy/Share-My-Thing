package kattcrazy.sharemything.sync

import android.content.Context
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.tasks.await

object WearSyncSupport {
    fun isSupported(context: Context): Boolean {
        val result = GoogleApiAvailability.getInstance()
            .isGooglePlayServicesAvailable(context.applicationContext)
        return result == ConnectionResult.SUCCESS
    }

    // Play Services may be present without a usable Wear data layer on some phones.
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
