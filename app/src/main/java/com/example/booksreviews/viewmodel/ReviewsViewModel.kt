package com.example.booksreviews.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.booksreviews.model.Review
import com.example.booksreviews.model.ReviewsRepository

class ReviewsViewModel : ViewModel() {

    // Initialize _reviews with an empty ArrayList
    val reviewsLiveData = MutableLiveData<ArrayList<Review>>().apply { value = ArrayList() }
    val isLoading = MutableLiveData(false)

    var currEditedReviewIndex: Int = -1

    fun deleteReviewAtIndex(index: Int) {
        val currentReviews = reviewsLiveData.value ?: ArrayList()

        if (index in 0 until currentReviews.size) {
            ReviewsRepository.deleteBookReview(currentReviews[index].id)

            getAllBookReviews()
        }
    }

    fun getAllBookReviews() {
        isLoading.value = true

        ReviewsRepository.getAllBookReviews().addOnSuccessListener {
            reviewsLiveData.value = it
            isLoading.value = false
        }
    }

    fun getReviewsByUser(userId: String) : List<Review> {
        return reviewsLiveData.value!!.filter { it.userId == userId }
    }

    fun startEditing(index: Int) {
        currEditedReviewIndex = index
    }

    fun finishEditing() {
        currEditedReviewIndex = -1
    }

}
