package kattcrazy.sharemything.wear

import android.content.ComponentName
import android.content.Context
import androidx.wear.tiles.TileService
import androidx.wear.watchface.complications.datasource.ComplicationDataSourceUpdateRequester
import kattcrazy.sharemything.complication.Complication1Service
import kattcrazy.sharemything.complication.Complication2Service
import kattcrazy.sharemything.complication.Complication3Service
import kattcrazy.sharemything.complication.Complication4Service
import kattcrazy.sharemything.complication.Complication5Service
import kattcrazy.sharemything.data.SurfaceSlot
import kattcrazy.sharemything.data.SurfaceUpdateListener
import kattcrazy.sharemything.tile.Tile1Service
import kattcrazy.sharemything.tile.Tile2Service
import kattcrazy.sharemything.tile.Tile3Service
import kattcrazy.sharemything.tile.Tile4Service
import kattcrazy.sharemything.tile.Tile5Service

class WearSurfaceUpdater(
    private val context: Context,
) : SurfaceUpdateListener {
    override fun onSurfaceUpdatesNeeded(slots: Collection<SurfaceSlot>) {
        slots.forEach { requestSurfaceUpdate(it) }
    }

    private fun requestSurfaceUpdate(slot: SurfaceSlot) {
        val tileIndex = SurfaceSlot.tiles.indexOf(slot)
        if (tileIndex >= 0) {
            TileService.getUpdater(context).requestUpdate(tileServiceClasses[tileIndex])
            return
        }
        val complicationIndex = SurfaceSlot.complications.indexOf(slot)
        if (complicationIndex >= 0) {
            ComplicationDataSourceUpdateRequester.create(
                context,
                ComponentName(context, complicationServiceClasses[complicationIndex]),
            ).requestUpdateAll()
        }
    }

    companion object {
        private val tileServiceClasses = listOf(
            Tile1Service::class.java,
            Tile2Service::class.java,
            Tile3Service::class.java,
            Tile4Service::class.java,
            Tile5Service::class.java,
        )
        private val complicationServiceClasses = listOf(
            Complication1Service::class.java,
            Complication2Service::class.java,
            Complication3Service::class.java,
            Complication4Service::class.java,
            Complication5Service::class.java,
        )
    }
}
