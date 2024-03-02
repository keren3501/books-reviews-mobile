package com.example.booksreviews.model

import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot

private const val TAG: String = "BookReviewsRepository"

object ReviewsRepository {

    private val db = FirebaseFirestore.getInstance()
    private val bookReviewsCollection = db.collection("bookReviews")

    fun addBookReview(bookReview: Review) {
        bookReviewsCollection.add(bookReview)
            .addOnSuccessListener { documentReference ->
                bookReview.id = documentReference.id
                Log.d(TAG, "DocumentSnapshot added with ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error adding document", e)
            }
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

    fun getBookReviewsByUserId(userId: String): Task<QuerySnapshot> {
        return bookReviewsCollection.whereEqualTo("userId", userId).get()
    }

    fun editBookReview(reviewId: String, updatedReview: Review) {
        val updates = hashMapOf<String, Any>(
            "bookCoverUrl" to updatedReview.bookCoverUrl,
            "bookTitle" to updatedReview.bookTitle,
            "authorName" to updatedReview.authorName,
            "reviewText" to updatedReview.reviewText)

        bookReviewsCollection.document(reviewId)
            .update(updates)
            .addOnSuccessListener {
                Log.d(TAG, "DocumentSnapshot successfully updated!")
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error updating document", e)
            }
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
