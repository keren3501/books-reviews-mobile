package com.example.booksreviews.model

import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.concurrent.CompletableFuture

private const val TAG: String = "BookReviewsRepository"

object ReviewsRepository {

    private val db = FirebaseFirestore.getInstance()
    private val bookReviewsCollection = db.collection("bookReviews")


    fun addBookReview(newReview: Review): CompletableFuture<String> {
        val resultFuture = CompletableFuture<String>()

        GlobalScope.launch(Dispatchers.IO) {
            try {
                bookReviewsCollection.add(newReview)
                    .addOnSuccessListener { documentReference ->
                        val reviewId = documentReference.id
                        newReview.id = reviewId
                        Log.d(TAG, "DocumentSnapshot added with ID: $reviewId")
                        resultFuture.complete(reviewId) // Notify success
                    }
                    .addOnFailureListener { e ->
                        Log.w(TAG, "Error adding document", e)
                        resultFuture.complete("-1") // Notify failure
                    }
            } catch (e: Exception) {
                Log.e(TAG, "Error adding document", e)
                resultFuture.complete("-1") // Notify failure if any exception occurs
            }
        }

        return resultFuture
    }

    fun getAllBookReviews(): Task<ArrayList<Review>> {
        return bookReviewsCollection
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get().continueWith { task ->
            if (task.isSuccessful) {
                val result = ArrayList<Review>()
                for (document in task.result!!) {
                    val bookReview = document.toObject(Review::class.java)
                    bookReview.id = document.id
                    result.add(bookReview)

                    UserRepository.getUserById(bookReview.userId)
                }
                result
            } else {
                throw task.exception ?: Exception("Unknown error occurred")
            }
        }
    }

    fun editBookReview(reviewId: String, updatedReview: Review): CompletableFuture<Boolean> {
        val resultFuture = CompletableFuture<Boolean>()

        GlobalScope.launch(Dispatchers.IO) {
            val updates = hashMapOf<String, Any>(
                "bookCoverUrl" to updatedReview.bookCoverUrl,
                "description" to updatedReview.description,
                "bookTitle" to updatedReview.bookTitle,
                "authorName" to updatedReview.authorName,
                "reviewText" to updatedReview.reviewText
            )

            bookReviewsCollection.document(reviewId)
                .update(updates)
                .addOnSuccessListener {
                    Log.d(TAG, "DocumentSnapshot successfully updated!")
                    resultFuture.complete(true) // Notify success
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Error updating document", e)
                    resultFuture.completeExceptionally(e) // Notify failure
                }
        }

        return resultFuture
    }


    fun deleteBookReview(reviewId: String) {
        bookReviewsCollection.document(reviewId)
            .delete()
            .addOnSuccessListener {
                Log.d(TAG, "DocumentSnapshot successfully deleted!")
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error deleting document", e)
            }
    }
}
