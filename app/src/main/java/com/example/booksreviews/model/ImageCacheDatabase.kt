package com.example.booksreviews.model

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [CachedImage::class], version = 1)
abstract class ImageCacheDatabase : RoomDatabase() {
    abstract fun imageDao(): ImageDao

    companion object {
        @Volatile
        private var INSTANCE: ImageCacheDatabase? = null

        fun getDatabase(context: Context): ImageCacheDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ImageCacheDatabase::class.java,
                    "image_cache_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
