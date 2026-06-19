package com.sharemyththing.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface DisplayItemDao {
    @Query("SELECT * FROM display_items ORDER BY sortOrder ASC, id ASC")
    fun observeAll(): Flow<List<DisplayItem>>

    @Query("SELECT * FROM display_items WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): DisplayItem?

    @Query("SELECT COUNT(*) FROM display_items")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: DisplayItem): Long

    @Update
    suspend fun update(item: DisplayItem)

    @Delete
    suspend fun delete(item: DisplayItem)
}
