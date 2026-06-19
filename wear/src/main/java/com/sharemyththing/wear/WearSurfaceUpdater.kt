package com.sharemyththing.wear

import android.content.ComponentName
import android.content.Context
import androidx.wear.tiles.TileService
import androidx.wear.watchface.complications.datasource.ComplicationDataSourceUpdateRequester
import com.sharemyththing.complication.Complication1Service
import com.sharemyththing.complication.Complication2Service
import com.sharemyththing.complication.Complication3Service
import com.sharemyththing.complication.Complication4Service
import com.sharemyththing.complication.Complication5Service
import com.sharemyththing.data.SurfaceSlot
import com.sharemyththing.data.SurfaceUpdateListener
import com.sharemyththing.tile.Tile1Service
import com.sharemyththing.tile.Tile2Service
import com.sharemyththing.tile.Tile3Service
import com.sharemyththing.tile.Tile4Service
import com.sharemyththing.tile.Tile5Service

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
