package com.example.booksreviews.model

import kotlinx.coroutines.flow.Flow

object ImageRepository {

    lateinit var imageDao: ImageDao
    private lateinit var cachedImages: List<CachedImage>

    suspend fun addCacheImage(url: String, imageData: ByteArray) {
        val cachedImage = CachedImage(imageUrl = url, imageData = imageData)
        imageDao.insertImage(cachedImage)
    }

    fun getCachedImage(url: String): ByteArray? {
        val cachedImage = cachedImages.find { it.imageUrl == url }
        return cachedImage?.imageData
    }

    suspend fun getAllCachedImages() {
        imageDao.getAllImages().collect {
            cachedImages = it
        }
    }

}
