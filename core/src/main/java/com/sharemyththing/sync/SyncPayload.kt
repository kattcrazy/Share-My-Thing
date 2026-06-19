package com.sharemyththing.sync

import com.sharemyththing.data.ItemType
import org.json.JSONArray
import org.json.JSONObject

data class SyncItemRecord(
    val uuid: String,
    val title: String,
    val content: String,
    val type: ItemType,
    val sortOrder: Int,
    val updatedAtMillis: Long,
    val deleted: Boolean,
)

data class SyncSlotAssignment(
    val slot: String,
    val itemUuid: String?,
    val updatedAtMillis: Long,
)

data class SyncPayload(
    val items: List<SyncItemRecord>,
    val slotAssignments: List<SyncSlotAssignment>,
) {
    fun toJsonBytes(): ByteArray = toJson().toByteArray(Charsets.UTF_8)

    fun toJson(): String {
        val root = JSONObject()
        val itemsArray = JSONArray()
        items.forEach { item ->
            itemsArray.put(
                JSONObject().apply {
                    put("uuid", item.uuid)
                    put("title", item.title)
                    put("content", item.content)
                    put("type", item.type.name)
                    put("sortOrder", item.sortOrder)
                    put("updatedAtMillis", item.updatedAtMillis)
                    put("deleted", item.deleted)
                },
            )
        }
        root.put("items", itemsArray)

        val slotsArray = JSONArray()
        slotAssignments.forEach { assignment ->
            slotsArray.put(
                JSONObject().apply {
                    put("slot", assignment.slot)
                    put("itemUuid", assignment.itemUuid ?: JSONObject.NULL)
                    put("updatedAtMillis", assignment.updatedAtMillis)
                },
            )
        }
        root.put("slotAssignments", slotsArray)
        return root.toString()
    }

    companion object {
        fun fromJson(json: String): SyncPayload {
            val root = JSONObject(json)
            val items = buildList {
                val itemsArray = root.getJSONArray("items")
                for (index in 0 until itemsArray.length()) {
                    val itemJson = itemsArray.getJSONObject(index)
                    add(
                        SyncItemRecord(
                            uuid = itemJson.getString("uuid"),
                            title = itemJson.getString("title"),
                            content = itemJson.getString("content"),
                            type = ItemType.valueOf(itemJson.getString("type")),
                            sortOrder = itemJson.getInt("sortOrder"),
                            updatedAtMillis = itemJson.getLong("updatedAtMillis"),
                            deleted = itemJson.getBoolean("deleted"),
                        ),
                    )
                }
            }
            val slotAssignments = buildList {
                val slotsArray = root.getJSONArray("slotAssignments")
                for (index in 0 until slotsArray.length()) {
                    val slotJson = slotsArray.getJSONObject(index)
                    add(
                        SyncSlotAssignment(
                            slot = slotJson.getString("slot"),
                            itemUuid = slotJson.optString("itemUuid").takeIf { it.isNotBlank() },
                            updatedAtMillis = slotJson.getLong("updatedAtMillis"),
                        ),
                    )
                }
            }
            return SyncPayload(items = items, slotAssignments = slotAssignments)
        }

        fun fromJsonBytes(bytes: ByteArray): SyncPayload = fromJson(bytes.toString(Charsets.UTF_8))
    }
}
