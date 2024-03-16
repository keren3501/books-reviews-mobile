package com.example.booksreviews.model

import android.os.Environment
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.File
import java.util.concurrent.CompletableFuture

data class BookDetailsResponse(val items: List<BookDetailsItem>)
data class BookDetailsItem(val volumeInfo: VolumeInfo)
data class VolumeInfo(val title: String, val authors: List<String>, val description: String, val imageLinks: ImageLinks?)
data class ImageLinks(val thumbnail: String?)

data class Userr(val id: String, val username: String, val imageUrl: String)
data class Bookk(val id: String, val title: String, val author: String, val description: String, val imageUrl: String)
data class Revieww(val id: String, val bookId: String, val userId: String, val reviewText: String) // Include bookId and userId

// Additional class for displaying combined data (optional)
data class ReviewWithDetails(val review: Revieww, val user: Userr, val book: Bookk)


object ReviewsRepo {
    val firebase: FirebaseDatabase by lazy {
        FirebaseDatabase.getInstance()
    }

    private val firestore: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }

    private val storage: FirebaseStorage by lazy {
        FirebaseStorage.getInstance()
    }

    private val booksCollection = firebase.getReference("books")
    private val usersCollection = firebase.getReference("users")
    private val imagesRef = storage.getReference("images")

    suspend fun fetchReviewWithDetails(reviewId: String): ReviewWithDetails? {
        val review = FirebaseRepository.getReviewById(reviewId) // Fetch the review document

        return if (review != null) {
            val userId = review.userId
            val bookId = review.bookId

            // Fetch user and book details concurrently using coroutines
            val userDeferred = coroutineScope { async { FirebaseRepository.getUserById(userId) } }
            val bookDeferred = coroutineScope { async { FirebaseRepository.getBookById(bookId) } }

            val user = userDeferred.await()
            val book = bookDeferred.await()

            if (user != null && book != null) {
                ReviewWithDetails(review, user, book)
            } else {
                null // Handle case where user or book details are not found
            }
        } else {
            null // Handle case where review is not found
        }
    }

    suspend fun saveReviewWithDetails(reviewWithDetails: ReviewWithDetails): CompletableFuture<String> {
        return if (isNewBook(reviewWithDetails.book)) {
            fetchBookInfoAndImage(reviewWithDetails.book.title, reviewWithDetails.book.author).thenCompose { (description, imageUrl) ->
                if (description != null && imageUrl != null) {
                    val bookId = FirebaseRepository.addBook(reviewWithDetails.book.copy(description = description, imageUrl = imageUrl))

                    // Save the review with the retrieved description and image URL
                    FirebaseRepository.addReview(reviewWithDetails.review.copy(bookId = bookId, userId = reviewWithDetails.user.userId))
                } else {
                    CompletableFuture.failedFuture(Exception("Failed to fetch book info or image"))
                }
            }
        } else {
            // Save the review without fetching book info and image
            FirebaseRepository.addReview(reviewWithDetails.review)
        }
    }

    private suspend fun isNewBook(book: Bookk): Boolean {
        return if (book.id.isEmpty()) {
            // If the review doesn't have an ID, it's a new book
            true
        } else {
            // Check if the book title and author have changed
            val existingBook = FirebaseRepository.getBookById(book.id)
            existingBook?.let {
                it.id != book.title || it.author != book.author
            } ?: false
        }
    }

    private suspend fun fetchBookInfoAndImage(bookTitle: String, bookAuthor: String): CompletableFuture<Pair<String?, String?>> {
        return CompletableFuture.supplyAsync {
            try {
                val apiUrl = "https://www.googleapis.com/books/v1/volumes?q=intitle:$bookTitle+inauthor:$bookAuthor"
                val client = OkHttpClient()
                val request = Request.Builder().url(apiUrl).build()
                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()
                var description: String? = null
                var coverImageUrl: String? = null

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
                        coverImageUrl = imageLinks?.optString("thumbnail")

                        // Fetch image and save locally
                        coverImageUrl?.let { imageUrl ->
                            val imageName = "${bookTitle}_${bookAuthor.replace(" ", "_")}.png"
                            val imageFile = File(Environment.getExternalStorageDirectory(), "imgs/$imageName")
                            val inputStream = fetchImageAsStream(imageUrl)
                            saveImageToFile(inputStream, imageFile)

                            // Save the book cover image with the book ID as the file name
                            FirebaseRepository.saveBookCoverImage(imageFile, bookId)
                        }
                    }
                }
                Pair(description, coverImageUrl)
            } catch (e: IOException) {
                e.printStackTrace()
                Pair(null, null)
            }
        }
    }


}
