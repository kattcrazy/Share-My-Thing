package com.sharemyththing.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [DisplayItem::class],
    version = 1,
    exportSchema = false,
)
@TypeConverters(ItemTypeConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun displayItemDao(): DisplayItemDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "share_my_thing.db",
                ).build().also { instance = it }
            }
        }
    }
}
