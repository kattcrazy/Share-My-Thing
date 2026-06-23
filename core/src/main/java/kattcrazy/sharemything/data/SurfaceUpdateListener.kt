package kattcrazy.sharemything.data

fun interface SurfaceUpdateListener {
    fun onSurfaceUpdatesNeeded(slots: Collection<SurfaceSlot>)
}
