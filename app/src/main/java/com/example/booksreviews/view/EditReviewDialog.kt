package com.example.booksreviews.view

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.example.booksreviews.R
import com.example.booksreviews.databinding.DialogEditReviewBinding
import com.example.booksreviews.model.Review
import com.example.booksreviews.viewmodel.ReviewsViewModel
import com.example.booksreviews.viewmodel.UserViewModel

class EditReviewDialog(context: Context,
                       title: String,
                       reviewsViewModel: ReviewsViewModel,
                       userViewModel: UserViewModel) {

    // region Members

    private val binding = DialogEditReviewBinding.inflate(LayoutInflater.from(context))
    private var alertDialog: AlertDialog
    private var newReview: Review

    // endregion

    // region C'tor

    init {
        newReview = Review()

        val editedReview = reviewsViewModel.currEditedReview

        if (editedReview != null) {
            newReview = Review(editedReview)

            binding.etReviewText.setText(newReview.reviewText)
            binding.etAuthorName.setText(newReview.authorName)
            binding.etBookTitle.setText(newReview.bookTitle)
        }
        else {
            newReview = Review()
            newReview.userId = userViewModel.user.uid
        }

        alertDialog = AlertDialog.Builder(context)
            .setTitle(title)
            .setView(binding.root)
            .setPositiveButton("Post", null)
            .setNeutralButton("Cancel", null)
            .setCancelable(false)
            .create()

        // Set dialog background color
        alertDialog.window?.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.dialog_background))

        alertDialog.setOnShowListener {
            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val bookTitle = binding.etBookTitle.text.toString()
                val authorName = binding.etAuthorName.text.toString()
                val reviewText = binding.etReviewText.text.toString()

                if (bookTitle.isNotEmpty() && authorName.isNotEmpty() && reviewText.isNotEmpty()) {
                    newReview.bookTitle = bookTitle
                    newReview.authorName = authorName
                    newReview.reviewText = reviewText
                    reviewsViewModel.isLoading.value = true

                    // Call ViewModel to post review
                    if (reviewsViewModel.isEditing()) {
                        reviewsViewModel.editReview(newReview).thenAccept { success ->
                            reviewsViewModel.isLoading.value = false

                            if (success) {
                                // Handle success
                                reviewsViewModel.finishEditing()
                                alertDialog.dismiss()
                            } else {
                                // Handle failure
                                Toast.makeText(
                                    context,
                                    "Something went wrong :(",
                                    Toast.LENGTH_SHORT
                                )
                                    .show()
                            }
                        }
                    } else {
                        reviewsViewModel.postReview(newReview).thenAccept { reviewId ->
                            reviewsViewModel.isLoading.value = false

                            if (reviewId != "-1") {
                                // Handle success
                                newReview.id = reviewId
                                reviewsViewModel.finishEditing()
                                alertDialog.dismiss()
                            } else {
                                // Handle failure
                                Toast.makeText(
                                    context,
                                    "Something went wrong :(",
                                    Toast.LENGTH_SHORT
                                )
                                    .show()
                            }
                        }
                    }
                } else {
                    // Show error message if any field is empty
                    Toast.makeText(context, "All fields are required", Toast.LENGTH_SHORT).show()
                }
            }

            reviewsViewModel.isLoading.observeForever {
                // Show spinner
                binding.spinner.visibility = if (it) View.VISIBLE else View.GONE

                // Disable all views in the dialog until posting is done
                binding.etBookTitle.isEnabled = !it
                binding.etAuthorName.isEnabled = !it
                binding.etReviewText.isEnabled = !it
                alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = !it
                alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL).isEnabled = !it
            }
        }
    }

    // endregion

    // region Public Methods

    fun show() {
        alertDialog.show()
    }

    // endregion

}
