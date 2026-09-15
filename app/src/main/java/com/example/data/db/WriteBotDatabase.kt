package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [SettingsEntity::class], version = 1, exportSchema = false)
abstract class WriteBotDatabase : RoomDatabase() {
    abstract fun settingsDao(): SettingsDao

    companion object {
        @Volatile
        private var INSTANCE: WriteBotDatabase? = null

        fun getInstance(context: Context): WriteBotDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WriteBotDatabase::class.java,
                    "writebot_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
