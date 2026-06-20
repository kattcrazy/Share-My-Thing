package com.sharemyththing.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import java.util.UUID

@Database(
    entities = [DisplayItem::class],
    version = 4,
    exportSchema = false,
)
@TypeConverters(ItemTypeConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun displayItemDao(): DisplayItemDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE display_items ADD COLUMN uuid TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE display_items ADD COLUMN updatedAtMillis INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE display_items ADD COLUMN deleted INTEGER NOT NULL DEFAULT 0")

                val now = System.currentTimeMillis()
                db.query("SELECT id FROM display_items").use { cursor ->
                    while (cursor.moveToNext()) {
                        val id = cursor.getLong(0)
                        db.execSQL(
                            "UPDATE display_items SET uuid = ?, updatedAtMillis = ? WHERE id = ?",
                            arrayOf(UUID.randomUUID().toString(), now, id),
                        )
                    }
                }
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                val duplicateUuids = mutableListOf<String>()
                db.query(
                    "SELECT uuid FROM display_items WHERE uuid != '' GROUP BY uuid HAVING COUNT(*) > 1",
                ).use { cursor ->
                    while (cursor.moveToNext()) {
                        duplicateUuids.add(cursor.getString(0))
                    }
                }
                duplicateUuids.forEach { uuid ->
                    db.query(
                        """
                        SELECT id FROM display_items
                        WHERE uuid = ?
                        ORDER BY updatedAtMillis DESC, id ASC
                        """.trimIndent(),
                        arrayOf(uuid),
                    ).use { cursor ->
                        if (cursor.moveToFirst()) {
                            val keepId = cursor.getLong(0)
                            db.execSQL(
                                "DELETE FROM display_items WHERE uuid = ? AND id != ?",
                                arrayOf(uuid, keepId),
                            )
                        }
                    }
                }
                db.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS index_display_items_uuid ON display_items(uuid)",
                )
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE display_items ADD COLUMN visibleOnWatch INTEGER NOT NULL DEFAULT 1",
                )
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "share_my_thing.db",
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                    .build()
                    .also { instance = it }
            }
        }
    }
}
