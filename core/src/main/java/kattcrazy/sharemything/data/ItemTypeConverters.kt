package kattcrazy.sharemything.data

import androidx.room.TypeConverter

class ItemTypeConverters {
    @TypeConverter
    fun fromItemType(value: ItemType): String = value.name

    @TypeConverter
    fun toItemType(value: String): ItemType = ItemType.valueOf(value)

    @TypeConverter
    fun fromItemIcon(value: ItemIcon): String = value.name

    @TypeConverter
    fun toItemIcon(value: String): ItemIcon = ItemIcon.fromStoredName(value, ItemType.TEXT)
}
