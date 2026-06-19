package com.sharemyththing.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.surfaceDataStore: DataStore<Preferences> by preferencesDataStore(name = "surface_preferences")

class SurfacePreferences(private val context: Context) {
    private fun keyFor(slot: SurfaceSlot) = longPreferencesKey(slot.prefKeySuffix)
    private fun placedKeyFor(slot: SurfaceSlot) = booleanPreferencesKey("${slot.prefKeySuffix}_placed")

    val assignments: Flow<Map<SurfaceSlot, Long?>> =
        context.surfaceDataStore.data.map { prefs ->
            SurfaceSlot.all.associateWith { slot ->
                prefs[keyFor(slot)]?.takeIf { it >= 0 }
            }
        }

    val placedOnWatch: Flow<Set<SurfaceSlot>> =
        context.surfaceDataStore.data.map { prefs ->
            SurfaceSlot.all.filterTo(mutableSetOf()) { slot ->
                prefs[placedKeyFor(slot)] == true
            }
        }

    fun itemIdFor(slot: SurfaceSlot): Flow<Long?> =
        assignments.map { it[slot] }

    suspend fun getItemId(slot: SurfaceSlot): Long? =
        itemIdFor(slot).first()

    suspend fun setPlacedOnWatch(slot: SurfaceSlot, placed: Boolean) {
        context.surfaceDataStore.edit { prefs ->
            prefs[placedKeyFor(slot)] = placed
        }
    }

    suspend fun setItemId(slot: SurfaceSlot, id: Long?) {
        context.surfaceDataStore.edit { prefs ->
            val key = keyFor(slot)
            if (id == null) {
                prefs.remove(key)
            } else {
                prefs[key] = id
            }
        }
    }
}
