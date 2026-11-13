package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        ChatSessionEntity::class,
        ChatMessageEntity::class,
        SavedCreationEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class VikasDatabase : RoomDatabase() {
    abstract fun vikasDao(): VikasDao

    companion object {
        @Volatile
        private var INSTANCE: VikasDatabase? = null

        fun getInstance(context: Context): VikasDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    VikasDatabase::class.java,
                    "vikas_ai_local.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
