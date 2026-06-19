package com.sharemyththing.data

import androidx.room.TypeConverter

class ItemTypeConverters {
    @TypeConverter
    fun fromItemType(value: ItemType): String = value.name

    @TypeConverter
    fun toItemType(value: String): ItemType = ItemType.valueOf(value)
}
