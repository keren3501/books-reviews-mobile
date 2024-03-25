package com.example.booksreviews.model

import android.net.Uri
import android.os.Environment
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.net.URL

private const val TAG: String = "BookReviewsRepository"

/**
 * Data class representing book details fetched from an external API.
 *
 * @property title The title of the book.
 * @property authors The list of authors of the book.
 * @property coverUrl The URL of the book's cover image.
 */
data class BookDetails(val title: String, val authors: List<String>, val coverUrl: String)

/**
 * Repository responsible for managing book reviews and related operations.
 */
object ReviewsRepository {

    private val db = FirebaseFirestore.getInstance()
    private val bookReviewsCollection = db.collection("bookReviews")

    /**
     * Adds a new book review to the Firestore collection.
     *
     * @param newReview The review to be added.
     * @return The ID of the newly added review.
     */
    suspend fun addBookReview(newReview: Review): String = withContext(Dispatchers.IO) {
        try {
            // Fetch book details from API
            val bookDetails = fetchBookDetailsFromAPI(newReview.bookTitle, newReview.authorName)

            if (bookDetails.title.isNotEmpty() && bookDetails.authors.isNotEmpty()) {
                // Update review with fetched details if needed
                newReview.bookTitle = bookDetails.title
                newReview.authorName = bookDetails.authors.joinToString(", ")
            }

            if (bookDetails.coverUrl.isNotEmpty()) {
                // Save cover image locally and upload to Firebase Storage
                saveCoverImageLocally(bookDetails.coverUrl)

                newReview.bookCoverUrl = bookDetails.coverUrl
            }

            // Save review to Firestore collection
            val documentReference = bookReviewsCollection.add(newReview).await()
            val reviewId = documentReference.id
            newReview.id = reviewId

            // Update the review in Firestore with the new ID
            bookReviewsCollection.document(reviewId).set(newReview).await()

            Log.d(TAG, "DocumentSnapshot added with ID: $reviewId")

            reviewId
        } catch (e: Exception) {
            Log.e(TAG, "Error adding document", e)
            "-1"
        }
    }

    /**
     * Saves the cover image locally.
     *
     * @param coverImageUrl The URL of the cover image.
     * @return The URL of the saved cover image.
     */
    private suspend fun saveCoverImageLocally(coverImageUrl: String): String {
        if (ImageRepository.getCachedImage(coverImageUrl) == null) {
            downloadImageLocally(coverImageUrl)
        }

        return coverImageUrl
    }

    /**
     * Fetches book details from an external API.
     *
     * @param title The title of the book.
     * @param authors The author(s) of the book.
     * @return BookDetails object containing details fetched from the API.
     */
    private fun fetchBookDetailsFromAPI(title: String, authors: String): BookDetails {
        try {
            val apiUrl =
                "https://www.googleapis.com/books/v1/volumes?q=intitle:$title+inauthor:${authors}"
            val response = URL(apiUrl).readText()
            val jsonObject = JSONObject(response)
            val items = jsonObject.optJSONArray("items")
            if (items != null && items.length() > 0) {
                val volumeInfo = items.getJSONObject(0).optJSONObject("volumeInfo")
                val fetchedTitle = volumeInfo.optString("title")
                val fetchedAuthors = volumeInfo.optJSONArray("authors")?.let { authorsArray ->
                    (0 until authorsArray.length()).map { authorsArray.optString(it) }
                } ?: emptyList()
                val fetchedCoverUrl =
                    volumeInfo.optJSONObject("imageLinks")?.optString("thumbnail") ?: ""
                return BookDetails(fetchedTitle, fetchedAuthors, fetchedCoverUrl)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching book details from API", e)
        }
        return BookDetails("", emptyList(), "")
    }

    /**
     * Downloads an image locally and saves to db of cached images.
     *
     * @param imageUrl The URL of the image to download.
     */
    private suspend fun downloadImageLocally(imageUrl: String) {
        withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, imageUrl)
                val url = URL(imageUrl)
                val connection = url.openConnection()
                connection.connect()
                val inputStream = connection.getInputStream()
                val outputStream = ByteArrayOutputStream()
                val buffer = ByteArray(1024)
                var bytesRead = inputStream.read(buffer)
                while (bytesRead != -1) {
                    outputStream.write(buffer, 0, bytesRead)
                    bytesRead = inputStream.read(buffer)
                }
                inputStream.close()

                // Convert the ByteArrayOutputStream to a ByteArray
                val imageData = outputStream.toByteArray()
                outputStream.close()

                // Add the image data to your local cache (Room database)
                // Replace `addImageDataToCache` with the appropriate method to add the data to your database
                ImageRepository.addCacheImage(imageUrl, imageData)
            } catch (e: Exception) {
                Log.e(TAG, "Error downloading image", e)
                throw e
            }
        }
    }

    /**
     * Retrieves all book reviews from the Firestore collection.
     *
     * @return A list of all book reviews.
     */
    suspend fun getAllBookReviews(): List<Review> = withContext(Dispatchers.IO) {
        try {
            UserRepository.clearCache()

            val reviews = bookReviewsCollection.orderBy("timestamp", Query.Direction.DESCENDING)
                .get().await().toObjects(Review::class.java)

            // Iterate through each review to check and fetch images if needed
            for (review in reviews) {
                UserRepository.fetchUserDataWithCache(review.userId)

                if (ImageRepository.getCachedImage(review.bookCoverUrl) == null) {
                    downloadImageLocally(review.bookCoverUrl)
                }
            }

            reviews
        } catch (e: Exception) {
            throw e
        }
    }

    /**
     * Edits an existing book review.
     *
     * @param reviewId The ID of the review to edit.
     * @param updatedReview The updated review data.
     * @return True if the review was successfully edited, false otherwise.
     */
    suspend fun editBookReview(reviewId: String, updatedReview: Review): Boolean =
        withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "review id" + reviewId)
                val currentReview = bookReviewsCollection.document(reviewId).get().await().toObject(Review::class.java)

                if (currentReview != null) {
                    // Check if book title or author has changed
                    val isBookDetailsChanged = updatedReview.bookTitle != currentReview.bookTitle ||
                            updatedReview.authorName != currentReview.authorName

                    if (isBookDetailsChanged) {
                        // Fetch book details from API
                        val bookDetails = fetchBookDetailsFromAPI(updatedReview.bookTitle, updatedReview.authorName)

                        if (bookDetails.title.isNotEmpty() && bookDetails.authors.isNotEmpty()) {
                            // Update review with fetched details if needed
                            updatedReview.bookTitle = bookDetails.title
                            updatedReview.authorName = bookDetails.authors.joinToString(", ")
                        }

                        if (bookDetails.coverUrl.isNotEmpty()) {
                            // Save cover image locally and upload to Firebase Storage
                            saveCoverImageLocally(bookDetails.coverUrl)

                            updatedReview.bookCoverUrl = bookDetails.coverUrl
                        }
                    }

                    // Update review text
                    val updates = hashMapOf(
                        "bookCoverUrl" to updatedReview.bookCoverUrl,
                        "bookTitle" to updatedReview.bookTitle,
                        "authorName" to updatedReview.authorName,
                        "reviewText" to updatedReview.reviewText
                    )

                    // Update review in Firestore
                    bookReviewsCollection.document(reviewId)
                        .update(updates as Map<String, Any>)
                        .await()

                    true
                } else {
                    Log.e(TAG, "Review not found")
                    false
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error updating document", e)
                false
            }
        }

    /**
     * Deletes a book review from the Firestore collection.
     *
     * @param reviewId The ID of the review to delete.
     * @return True if the review was successfully deleted, false otherwise.
     */
    suspend fun deleteBookReview(reviewId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            bookReviewsCollection.document(reviewId)
                .delete()
                .await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting document", e)
            false
        }
    }
}