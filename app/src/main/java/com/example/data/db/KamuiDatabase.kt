package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [SessionEntity::class, MessageEntity::class], version = 1, exportSchema = false)
abstract class KamuiDatabase : RoomDatabase() {
    abstract fun chatDao(): ChatDao

    companion object {
        @Volatile
        private var INSTANCE: KamuiDatabase? = null

        fun getInstance(context: Context): KamuiDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    KamuiDatabase::class.java,
                    "kamui_vault.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
