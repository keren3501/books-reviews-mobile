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
import java.io.File
import java.io.FileOutputStream
import java.net.URL

private const val TAG: String = "BookReviewsRepository"

data class BookDetails(val title: String, val authors: List<String>, val coverUrl: String)

object ReviewsRepository {

    private val db = FirebaseFirestore.getInstance()
    private val bookReviewsCollection = db.collection("bookReviews")

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
                val localImagePath = saveCoverImageLocally(
                    newReview.bookTitle,
                    newReview.authorName,
                    bookDetails.coverUrl
                )

                // Upload cover image to Firebase Storage if it doesn't exist
                uploadCoverImageToStorage(localImagePath)
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

    private suspend fun saveCoverImageLocally(
        title: String,
        author: String,
        coverImageUrl: String
    ): String {
        // Check if the cover image exists locally
        val localImageName = "${title}_${author}"
        val localImageFile =
            File(Environment.getExternalStorageDirectory(), "covers/${localImageName}.png")

        return if (!localImageFile.exists()) {
            // If the cover image doesn't exist locally, download and save it
            downloadImageLocally(coverImageUrl, localImageFile)
            localImageFile.absolutePath
        } else {
            // If the cover image exists locally, return its path
            localImageFile.absolutePath
        }
    }

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

    private suspend fun downloadImageLocally(imageUrl: String, file: File) {
        withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, imageUrl)
                val url = URL(imageUrl)
                val connection = url.openConnection()
                connection.connect()
                val inputStream = connection.getInputStream()
                val outputStream = FileOutputStream(file)
                val buffer = ByteArray(1024)
                var bytesRead = inputStream.read(buffer)
                while (bytesRead != -1) {
                    outputStream.write(buffer, 0, bytesRead)
                    bytesRead = inputStream.read(buffer)
                }
                outputStream.close()
                inputStream.close()
            } catch (e: Exception) {
                Log.e(TAG, "Error downloading image", e)
                throw e
            }
        }
    }

    private suspend fun uploadCoverImageToStorage(localImagePath: String) {
        try {
            val storageReference =
                FirebaseStorage.getInstance().reference.child("covers/${File(localImagePath).name}")
            val uri = Uri.fromFile(File(localImagePath))
            storageReference.putFile(uri).await()
            Log.d(TAG, "Cover image uploaded successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error uploading cover image to Firebase Storage", e)
            throw e
        }
    }

    suspend fun getAllBookReviews(): List<Review> = withContext(Dispatchers.IO) {
        try {
            UserRepository.clearCache()

            val reviews = bookReviewsCollection.orderBy("timestamp", Query.Direction.DESCENDING)
                .get().await().toObjects(Review::class.java)

            // Iterate through each review to check and fetch images if needed
            for (review in reviews) {
                UserRepository.fetchUserDataWithCache(review.userId)

                val imageFileName = "${review.bookTitle}_${review.authorName}"
                val localImageFile =
                    File(Environment.getExternalStorageDirectory().absolutePath + "/covers/" + imageFileName + ".png")

                if (!localImageFile.exists()) {
                    fetchImageFromStorage(imageFileName)
                }
            }

            reviews
        } catch (e: Exception) {
            throw e
        }
    }

    private suspend fun fetchImageFromStorage(imageName: String): File? {
        // Implement fetching image from Firebase Storage
        // Save the fetched image locally and return the file path
        // Use coroutine to perform asynchronous operations
        return withContext(Dispatchers.IO) {
            try {
                // Fetch image from Firebase Storage
                val storageReference =
                    FirebaseStorage.getInstance().reference.child("covers/${imageName}.png")
                val localFile = File(Environment.getExternalStorageDirectory().absolutePath + "/covers/" + imageName + ".png")
                localFile.createNewFile()

                storageReference.getFile(localFile).await()

                localFile // Return the local image file
            } catch (e: Exception) {
                Log.e("ReviewsViewModel", "Error fetching image from Firebase Storage", e)
                null
            }
        }
    }

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
                            val localImagePath = saveCoverImageLocally(
                                updatedReview.bookTitle,
                                updatedReview.authorName,
                                bookDetails.coverUrl
                            )

                            // Upload cover image to Firebase Storage if it doesn't exist
                            uploadCoverImageToStorage(localImagePath)
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