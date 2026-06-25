package kattcrazy.sharemything.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "display_items",
    indices = [Index(value = ["uuid"], unique = true)],
)
data class DisplayItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val uuid: String = "",
    val title: String,
    val content: String,
    val type: ItemType,
    val icon: ItemIcon,
    val sortOrder: Int = 0,
    val updatedAtMillis: Long = 0L,
    val deleted: Boolean = false,
    val visibleOnWatch: Boolean = true,
) {
    companion object {
        fun newItem(
            title: String,
            content: String,
            type: ItemType,
            icon: ItemIcon = ItemIcon.defaultFor(type),
            sortOrder: Int = 0,
            nowMillis: Long = System.currentTimeMillis(),
        ): DisplayItem = DisplayItem(
            uuid = UUID.randomUUID().toString(),
            title = title,
            content = content,
            type = type,
            icon = icon,
            sortOrder = sortOrder,
            updatedAtMillis = nowMillis,
        )
    }
}
