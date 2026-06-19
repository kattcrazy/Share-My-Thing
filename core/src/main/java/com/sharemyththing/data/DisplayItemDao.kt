package com.sharemyththing.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface DisplayItemDao {
    @Query("SELECT * FROM display_items WHERE deleted = 0 ORDER BY sortOrder ASC, id ASC")
    fun observeAll(): Flow<List<DisplayItem>>

    @Query("SELECT * FROM display_items WHERE deleted = 0 ORDER BY sortOrder ASC, id ASC")
    suspend fun getAllActive(): List<DisplayItem>

    @Query("SELECT * FROM display_items ORDER BY sortOrder ASC, id ASC")
    suspend fun getAllIncludingDeleted(): List<DisplayItem>

    @Query("SELECT * FROM display_items WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): DisplayItem?

    @Query("SELECT * FROM display_items WHERE uuid = :uuid LIMIT 1")
    suspend fun getByUuid(uuid: String): DisplayItem?

    @Query("SELECT COUNT(*) FROM display_items WHERE deleted = 0")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(item: DisplayItem): Long

    @Update
    suspend fun update(item: DisplayItem)

    @Query("UPDATE display_items SET deleted = 1, updatedAtMillis = :updatedAtMillis WHERE id = :id")
    suspend fun softDelete(id: Long, updatedAtMillis: Long)

    @Transaction
    suspend fun applySyncRecord(record: SyncRecordApply) {
        if (record.uuid.isBlank()) {
            return
        }
        val existing = getByUuid(record.uuid)
        if (existing == null) {
            if (record.deleted) {
                return
            }
            insert(
                DisplayItem(
                    uuid = record.uuid,
                    title = record.title,
                    content = record.content,
                    type = record.type,
                    sortOrder = record.sortOrder,
                    updatedAtMillis = record.updatedAtMillis,
                    deleted = false,
                ),
            )
            return
        }
        if (record.updatedAtMillis < existing.updatedAtMillis) {
            return
        }
        if (record.deleted) {
            softDelete(existing.id, record.updatedAtMillis)
        } else {
            update(
                existing.copy(
                    title = record.title,
                    content = record.content,
                    type = record.type,
                    sortOrder = record.sortOrder,
                    updatedAtMillis = record.updatedAtMillis,
                    deleted = false,
                ),
            )
        }
    }
}

data class SyncRecordApply(
    val uuid: String,
    val title: String,
    val content: String,
    val type: ItemType,
    val sortOrder: Int,
    val updatedAtMillis: Long,
    val deleted: Boolean,
)
