package kattcrazy.sharemything.data

class CompositeSurfaceUpdateListener(
    private vararg val listeners: SurfaceUpdateListener,
) : SurfaceUpdateListener {
    override fun onSurfaceUpdatesNeeded(slots: Collection<SurfaceSlot>) {
        listeners.forEach { listener -> listener.onSurfaceUpdatesNeeded(slots) }
    }
}
