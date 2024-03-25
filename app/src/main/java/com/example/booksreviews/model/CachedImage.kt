package com.example.booksreviews.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_images")
data class CachedImage(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val imageUrl: String,
    val imageData: ByteArray
)
