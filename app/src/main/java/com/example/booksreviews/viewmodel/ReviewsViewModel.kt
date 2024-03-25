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

/**
 * ViewModel class responsible for managing reviews data and operations.
 */
class ReviewsViewModel : ViewModel() {

    // region Members

    /**
     * LiveData holding the list of reviews.
     */
    val reviewsLiveData = MutableLiveData<ArrayList<Review>>().apply { value = ArrayList() }

    /**
     * LiveData indicating whether data is being loaded.
     */
    val isLoading = MutableLiveData(false)

    /**
     * Index of the currently edited review.
     */
    var currEditedReviewIndex: Int = -1

    /**
     * The review currently being edited.
     */
    var currEditedReview: Review? = null

    // endregion

    // region Public Methods

    /**
     * Deletes a review at the specified index.
     *
     * @param index The index of the review to delete.
     */
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

    /**
     * Retrieves all book reviews.
     */
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

    /**
     * Retrieves reviews by user ID.
     *
     * @param userId The ID of the user.
     * @return The list of reviews by the user.
     */
    fun getReviewsByUser(userId: String): List<Review> {
        return reviewsLiveData.value?.filter { it.userId == userId } ?: emptyList()
    }

    /**
     * Starts editing a review at the specified index.
     *
     * @param index The index of the review to edit.
     */
    fun startEditing(index: Int) {
        currEditedReviewIndex = index
        currEditedReview = reviewsLiveData.value?.getOrNull(index)
    }

    /**
     * Finishes editing the current review.
     */
    fun finishEditing() {
        currEditedReviewIndex = -1
        currEditedReview = null
    }

    /**
     * Checks if a review is being edited.
     *
     * @return True if a review is being edited, false otherwise.
     */
    fun isEditing(): Boolean {
        return currEditedReview != null
    }

    /**
     * Posts a new review.
     *
     * @param newReview The new review to post.
     * @return A CompletableFuture containing the ID of the posted review.
     */
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

    /**
     * Edits an existing review.
     *
     * @param newReview The updated review.
     * @return A CompletableFuture indicating whether the editing was successful.
     */
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

    // endregion
}
