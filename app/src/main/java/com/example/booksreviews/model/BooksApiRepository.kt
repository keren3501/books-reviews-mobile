package com.example.booksreviews.model

import android.os.Environment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

object BooksApiRepository {

    suspend fun fetchBookInfoAndImage(bookTitle: String, bookAuthor: String): Pair<String?, File?> {
        return withContext(Dispatchers.IO) {
            try {
                val apiUrl = "https://www.googleapis.com/books/v1/volumes?q=intitle:${bookTitle}+inauthor:${bookAuthor}"
                val client = OkHttpClient()
                val request = Request.Builder().url(apiUrl).build()
                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()
                var description: String? = null
                var coverImageFile: File? = null

                if (response.isSuccessful && !responseBody.isNullOrEmpty()) {
                    val jsonObject = JSONObject(responseBody)
                    val itemsArray = jsonObject.getJSONArray("items")

                    // Assuming you want to fetch the first book's description and cover image
                    if (itemsArray.length() > 0) {
                        val firstBook = itemsArray.getJSONObject(0)
                        val volumeInfo = firstBook.getJSONObject("volumeInfo")

                        // Get book description
                        description = volumeInfo.optString("description")

                        // Get book cover image
                        val imageLinks = volumeInfo.optJSONObject("imageLinks")
                        val coverImageUrl = imageLinks?.optString("thumbnail")

                        // Fetch image and save locally
                        coverImageUrl?.let { imageUrl ->
                            val imageName = "${bookTitle}_${bookAuthor.replace(" ", "_")}.png"
                            val storageDir = File(Environment.getExternalStorageDirectory(), "images")
                            coverImageFile = downloadImage(imageUrl, storageDir, imageName)
                        }
                    }
                }
                Pair(description, coverImageFile)
            } catch (e: IOException) {
                e.printStackTrace()
                Pair(null, null)
            }
        }
    }

    private fun downloadImage(imageUrl: String, storageDir: File, imageName: String): File? {
        try {
            val client = OkHttpClient()
            val request = Request.Builder().url(imageUrl).build()
            val response = client.newCall(request).execute()
            val inputStream = response.body?.byteStream()

            val imageFile = File(storageDir, imageName)
            imageFile.createNewFile()
            inputStream?.use { input ->
                FileOutputStream(imageFile).use { output ->
                    input.copyTo(output)
                }
            }
            return imageFile
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }
}