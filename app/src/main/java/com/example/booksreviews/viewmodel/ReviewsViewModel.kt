package com.example.booksreviews.viewmodel

import android.util.Log
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import com.example.booksreviews.model.Review
import com.example.booksreviews.model.ReviewsRepo
import com.example.booksreviews.model.ReviewsRepository
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.future.future
import java.util.UUID
import java.util.concurrent.CompletableFuture

class ReviewsViewModel : ViewModel() {

    private var isCurrSaving: MutableLiveData<Boolean> = MutableLiveData(false)

    // Initialize _reviews with an empty ArrayList
    val reviewsLiveData = MutableLiveData<ArrayList<Review>>().apply { value = ArrayList() }
    val isLoading = MutableLiveData(false)

    var currEditedReviewIndex: Int = -1
    var currEditedReview: Review? = null

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
        currEditedReview = reviewsLiveData.value?.get(index)
    }

    fun finishEditing() {
        currEditedReviewIndex = -1
        currEditedReview = null
    }

    fun isEditing() : Boolean {
        return currEditedReview != null
    }

    fun postReview(newReview: Review) : CompletableFuture<String> {
        newReview.timestamp = System.currentTimeMillis()

        return ReviewsRepository.addBookReview(newReview)
    }

    fun editReview(newReview: Review) : CompletableFuture<Boolean> {
        // Save book review details along with the cover image URL in Firebase Realtime Database
        return ReviewsRepository.editBookReview(newReview.id, newReview)
    }

//    fun saveReview(newReview: Review) {
//        isCurrSaving.value = true
//
//        if (isEditing()) {
//            if (currEditedReview!!.getIsBookChanged(newReview.bookTitle, newReview.authorName)) {
//                //fetch book details from api
//                editReview(newReview)
//            }
//            else {
//                editReview(newReview)
//            }
//        }
//        else {
//            // fetch book details from api
//            postReview(newReview)
//        }
//    }

    fun saveReview(review: Review): CompletableFuture<Unit> {
        return viewModelScope.future {
            ReviewsRepo.saveReview(review)
        }
    }

}
