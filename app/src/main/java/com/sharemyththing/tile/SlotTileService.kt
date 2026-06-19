package com.sharemyththing.tile

import androidx.wear.protolayout.ResourceBuilders
import androidx.wear.protolayout.TimelineBuilders
import androidx.wear.tiles.EventBuilders
import androidx.wear.tiles.RequestBuilders
import androidx.wear.tiles.TileBuilders
import androidx.wear.tiles.TileService
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.sharemyththing.R
import com.sharemyththing.data.DisplayItem
import com.sharemyththing.data.ItemsRepository
import com.sharemyththing.data.SurfaceSlot
import kotlinx.coroutines.runBlocking

abstract class SlotTileService(
    private val slot: SurfaceSlot,
) : TileService() {
    override fun onTileRequest(requestParams: RequestBuilders.TileRequest): ListenableFuture<TileBuilders.Tile> {
        markTilePlacedOnWatch()
        val item = loadAssignedItem()
        val resourcesVersion = tileResourcesVersion(item)
        val layoutElement = buildTileLayout(
            context = this,
            deviceConfiguration = requestParams.deviceConfiguration,
            item = item,
            configureText = getString(R.string.tile_configure),
        )
        return Futures.immediateFuture(
            TileBuilders.Tile.Builder()
                .setResourcesVersion(resourcesVersion)
                .setTileTimeline(TimelineBuilders.Timeline.fromLayoutElement(layoutElement))
                .build(),
        )
    }

    override fun onTileResourcesRequest(
        requestParams: RequestBuilders.ResourcesRequest,
    ): ListenableFuture<ResourceBuilders.Resources> {
        val item = loadAssignedItem()
        return Futures.immediateFuture(buildTileResources(item))
    }

    override fun onTileAddEvent(requestParams: EventBuilders.TileAddEvent) {
        markTilePlacedOnWatch()
    }

    override fun onTileRemoveEvent(requestParams: EventBuilders.TileRemoveEvent) {
        runBlocking {
            ItemsRepository(this@SlotTileService).surfacePreferences.setPlacedOnWatch(
                slot,
                placed = false,
            )
        }
    }

    private fun loadAssignedItem(): DisplayItem? = runBlocking {
        ItemsRepository(this@SlotTileService).let { repository ->
            repository.surfacePreferences.getItemId(slot)?.let { repository.getItem(it) }
        }
    }

    private fun markTilePlacedOnWatch() {
        runBlocking {
            ItemsRepository(this@SlotTileService).surfacePreferences.setPlacedOnWatch(
                slot,
                placed = true,
            )
        }
    }
}

class Tile1Service : SlotTileService(SurfaceSlot.TILE_1)

class Tile2Service : SlotTileService(SurfaceSlot.TILE_2)

class Tile3Service : SlotTileService(SurfaceSlot.TILE_3)

class Tile4Service : SlotTileService(SurfaceSlot.TILE_4)

class Tile5Service : SlotTileService(SurfaceSlot.TILE_5)
