package com.sharemyththing.data

import android.content.ComponentName
import android.content.Context
import androidx.wear.tiles.TileService
import androidx.wear.watchface.complications.datasource.ComplicationDataSourceUpdateRequester
import com.sharemyththing.complication.Complication1Service
import com.sharemyththing.complication.Complication2Service
import com.sharemyththing.complication.Complication3Service
import com.sharemyththing.complication.Complication4Service
import com.sharemyththing.complication.Complication5Service
import com.sharemyththing.tile.Tile1Service
import com.sharemyththing.tile.Tile2Service
import com.sharemyththing.tile.Tile3Service
import com.sharemyththing.tile.Tile4Service
import com.sharemyththing.tile.Tile5Service
import com.sharemyththing.util.QrCodeGenerator
import kotlinx.coroutines.flow.Flow

class ItemsRepository(context: Context) {
    private val appContext = context.applicationContext
    private val dao = AppDatabase.getInstance(appContext).displayItemDao()
    val surfacePreferences = SurfacePreferences(appContext)

    val items: Flow<List<DisplayItem>> = dao.observeAll()
    val slotAssignments: Flow<Map<SurfaceSlot, Long?>> = surfacePreferences.assignments
    val surfacesPlacedOnWatch: Flow<Set<SurfaceSlot>> = surfacePreferences.placedOnWatch

    suspend fun getItem(id: Long): DisplayItem? = dao.getById(id)

    suspend fun upsert(item: DisplayItem): Long {
        val id = if (item.id == 0L) {
            dao.insert(item)
        } else {
            dao.update(item)
            item.id
        }
        QrCodeGenerator.invalidateCache()
        requestSurfaceUpdates()
        return id
    }

    suspend fun delete(item: DisplayItem) {
        dao.delete(item)
        SurfaceSlot.all.forEach { slot ->
            if (surfacePreferences.getItemId(slot) == item.id) {
                surfacePreferences.setItemId(slot, null)
            }
        }
        QrCodeGenerator.invalidateCache()
        requestSurfaceUpdates()
    }

    suspend fun initializeOnLaunch() {
        surfacePreferences.migrateFromLegacyPinsIfNeeded()
    }

    suspend fun setSlotItemId(slot: SurfaceSlot, id: Long?) {
        surfacePreferences.setItemId(slot, id)
        requestSurfaceUpdates()
    }

    fun requestSurfaceUpdates() {
        tileServiceClasses.forEach { serviceClass ->
            TileService.getUpdater(appContext).requestUpdate(serviceClass)
        }
        complicationServiceClasses.forEach { serviceClass ->
            ComplicationDataSourceUpdateRequester.create(
                appContext,
                ComponentName(appContext, serviceClass),
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
