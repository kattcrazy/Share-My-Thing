package kattcrazy.sharemything.sync

import kattcrazy.sharemything.core.BuildConfig

object SyncPaths {
    const val REQUEST = "/sync/request"
    val CAPABILITY: String = BuildConfig.SYNC_CAPABILITY
}
