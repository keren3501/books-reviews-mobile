package com.example.booksreviews.model

/**
 * Repository responsible for managing cached images.
 */
object ImageRepository {

    /**
     * Reference to the Data Access Object (DAO) for cached images.
     */
    lateinit var imageDao: ImageDao

    /**
     * List containing all cached images.
     */
    private lateinit var cachedImages: List<CachedImage>

    /**
     * Adds a new cached image to the local database.
     *
     * @param url The URL of the image to be cached.
     * @param imageData The raw image data stored as a byte array.
     */
    suspend fun addCacheImage(url: String, imageData: ByteArray) {
        val cachedImage = CachedImage(imageUrl = url, imageData = imageData)
        imageDao.insertImage(cachedImage)
    }

    /**
     * Retrieves the raw image data for a cached image.
     *
     * @param url The URL of the cached image.
     * @return The raw image data as a byte array, or null if the image is not cached.
     */
    fun getCachedImage(url: String): ByteArray? {
        val cachedImage = cachedImages.find { it.imageUrl == url }
        return cachedImage?.imageData
    }

    /**
     * Retrieves all cached images from the local database and stores them in memory.
     */
    suspend fun getAllCachedImages() {
        imageDao.getAllImages().collect {
            cachedImages = it
        }
    }
}
