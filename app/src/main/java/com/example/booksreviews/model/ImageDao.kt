package com.example.booksreviews.model

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ImageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertImage(image: CachedImage)

    @Query("SELECT * FROM cached_images")
    fun getAllImages(): Flow<List<CachedImage>>
}
