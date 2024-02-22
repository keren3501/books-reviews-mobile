package com.example.booksreviews.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.booksreviews.model.Review

class ReviewsViewModel : ViewModel() {
    // Initialize _reviews with an empty ArrayList
    val reviews = MutableLiveData<ArrayList<Review>>().apply { value = ArrayList() }

    var currEditedReviewIndex: Int = -1

    // Function to update reviews
    fun updateReviews(newReviews: ArrayList<Review>) {
        reviews.value = newReviews
    }

    // Function to add one review to the existing list
    fun addReview(review: Review) {
        val currentReviews = reviews.value ?: ArrayList()
        currentReviews.add(0, review)
        reviews.value = currentReviews
    }

    fun deleteReviewAtIndex(index: Int) {
        val currentReviews = reviews.value ?: ArrayList()

        if (index in 0 until currentReviews.size) {
            currentReviews.removeAt(index)
            reviews.value = currentReviews
        }
    }

    fun updateReviewAtIndex(index: Int, updatedReview: Review) {
        val currentReviews =reviews.value ?: ArrayList()

        if (index in 0 until currentReviews.size) {
            currentReviews[index] = updatedReview
            reviews.value = currentReviews
        }

        currEditedReviewIndex = -1
    }

    fun getReviewsByUser(userId: String) : List<Review> {
        return reviews.value!!.filter { it.userId == userId }
    }
}
