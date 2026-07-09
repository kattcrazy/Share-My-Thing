package kattcrazy.sharemything.sync

import org.json.JSONObject

enum class ImportMode {
    REPLACE,
    MERGE,
    ADD,
}

data class BackupPayload(
    val exportVersion: Int = CURRENT_VERSION,
    val exportedAtMillis: Long,
    val includesSlotAssignments: Boolean,
    val syncPayload: SyncPayload,
) {
    fun toJson(): String {
        val root = JSONObject(syncPayload.toJson())
        root.put("exportVersion", exportVersion)
        root.put("app", APP_ID)
        root.put("exportedAtMillis", exportedAtMillis)
        root.put("includesSlotAssignments", includesSlotAssignments)
        return root.toString(2)
    }

    companion object {
        const val CURRENT_VERSION = 1
        const val APP_ID = "share-my-thing"
        const val DEFAULT_FILENAME = "share-my-thing-backup.json"

        fun fromJson(json: String): BackupPayload {
            val root = JSONObject(json)
            return if (root.has("exportVersion")) {
                val syncPayload = SyncPayload.fromJson(json)
                BackupPayload(
                    exportVersion = root.getInt("exportVersion"),
                    exportedAtMillis = root.optLong("exportedAtMillis", 0L),
                    includesSlotAssignments = root.optBoolean(
                        "includesSlotAssignments",
                        syncPayload.slotAssignments.isNotEmpty(),
                    ),
                    syncPayload = syncPayload,
                )
            } else {
                val syncPayload = SyncPayload.fromJson(json)
                BackupPayload(
                    exportedAtMillis = 0L,
                    includesSlotAssignments = syncPayload.slotAssignments.isNotEmpty(),
                    syncPayload = syncPayload,
                )
            }
        }
    }
}
