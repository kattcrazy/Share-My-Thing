package com.sharemyththing.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "display_items")
data class DisplayItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val content: String,
    val type: ItemType,
    val sortOrder: Int = 0,
)
