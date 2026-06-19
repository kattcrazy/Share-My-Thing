package com.sharemyththing.data

fun interface SurfaceUpdateListener {
    fun onSurfaceUpdatesNeeded(slots: Collection<SurfaceSlot>)
}
