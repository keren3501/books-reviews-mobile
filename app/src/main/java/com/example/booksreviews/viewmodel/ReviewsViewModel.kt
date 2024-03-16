package com.example.booksreviews.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.booksreviews.model.Review
import com.example.booksreviews.model.ReviewsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.CompletableFuture

class ReviewsViewModel : ViewModel() {

    val reviewsLiveData = MutableLiveData<ArrayList<Review>>().apply { value = ArrayList() }
    val isLoading = MutableLiveData(false)

    var currEditedReviewIndex: Int = -1
    var currEditedReview: Review? = null

    fun deleteReviewAtIndex(index: Int) {
        val currentReviews = reviewsLiveData.value ?: ArrayList()

        if (index in 0 until currentReviews.size) {
            val reviewId = currentReviews[index].id
            viewModelScope.launch(Dispatchers.Main) {
                val isSuccess = ReviewsRepository.deleteBookReview(reviewId)
                if (isSuccess) {
                    getAllBookReviews()
                } else {
                    Log.e("ReviewsViewModel", "Failed to delete review")
                    // Handle failure
                }
            }
        }
    }

    fun getAllBookReviews() {
        isLoading.value = true
        viewModelScope.launch(Dispatchers.Main) {
            try {
                val reviews = ReviewsRepository.getAllBookReviews()
                reviewsLiveData.value = ArrayList(reviews)
            } catch (e: Exception) {
                Log.e("ReviewsViewModel", "Error getting reviews", e)
                // Handle error
            } finally {
                isLoading.value = false
            }
        }
    }

    fun getReviewsByUser(userId: String): List<Review> {
        return reviewsLiveData.value?.filter { it.userId == userId } ?: emptyList()
    }

    fun startEditing(index: Int) {
        currEditedReviewIndex = index
        currEditedReview = reviewsLiveData.value?.getOrNull(index)
    }

    fun finishEditing() {
        currEditedReviewIndex = -1
        currEditedReview = null
    }

    fun isEditing(): Boolean {
        return currEditedReview != null
    }

    fun postReview(newReview: Review): CompletableFuture<String> {
        val completableFuture = CompletableFuture<String>()
        newReview.timestamp = System.currentTimeMillis()
        viewModelScope.launch(Dispatchers.Main) {
            try {
                val reviewId = ReviewsRepository.addBookReview(newReview)
                completableFuture.complete(reviewId)
            } catch (e: Exception) {
                Log.e("ReviewsViewModel", "Error posting review", e)
                completableFuture.completeExceptionally(e)
            }
        }
        return completableFuture
    }

    fun editReview(newReview: Review): CompletableFuture<Boolean> {
        val completableFuture = CompletableFuture<Boolean>()
        viewModelScope.launch(Dispatchers.Main) {
            try {
                val isSuccess = ReviewsRepository.editBookReview(newReview.id, newReview)
                completableFuture.complete(isSuccess)
            } catch (e: Exception) {
                Log.e("ReviewsViewModel", "Error editing review", e)
                completableFuture.completeExceptionally(e)
            }
        }
        return completableFuture
    }

}
