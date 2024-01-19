package com.example.booksreviews.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.booksreviews.R
import com.example.booksreviews.databinding.FragmentEditReviewBinding
import com.example.booksreviews.model.Review
import com.example.booksreviews.viewmodel.ReviewsViewModel
import com.example.booksreviews.viewmodel.UserViewModel

class EditReviewFragment : Fragment() {

    private lateinit var binding: FragmentEditReviewBinding
    private lateinit var reviewsViewModel: ReviewsViewModel
    private lateinit var userViewModel: UserViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentEditReviewBinding.inflate(inflater, container, false)

        setHasOptionsMenu(true)
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val viewModelProvider = ViewModelProvider(requireActivity())
        reviewsViewModel = viewModelProvider[ReviewsViewModel::class.java]
        userViewModel = viewModelProvider[UserViewModel::class.java]

        // if its edit mode
        if (reviewsViewModel.currEditedReviewIndex != -1) {
            val review = reviewsViewModel.reviews.value?.get(reviewsViewModel.currEditedReviewIndex)

            if (review != null) {
                binding.etReviewText.setText(review.reviewText)

            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.edit_post_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                // Navigate to the HomeFragment when the back button is pressed
                findNavController().popBackStack()
                true
            }
            R.id.action_save -> {
                val reviewText = binding.etReviewText.text.toString()
                val bookTitle = binding.etBookTitle.text.toString()
                val authorName = binding.etAuthorName.text.toString()
                // Check if both text and photo are provided
                if (reviewText.isNotEmpty() /* Add condition for selected photo existence */) {
                    val review = Review(
                        reviewsViewModel.reviews.value!!.size,
                        userViewModel.user.id,
                        "keren",
                        bookTitle,
                        authorName,
                        reviewText
                    )

                    if (reviewsViewModel.currEditedReviewIndex !=-1) {
                        reviewsViewModel.updateReviewAtIndex(reviewsViewModel.currEditedReviewIndex, review)
                        findNavController().popBackStack()
                    }
                    else {
                        // Create an intent to pass back the new review data
                        reviewsViewModel.addReview(review)

                        findNavController().popBackStack()
                    }
                } else {
                    // Show an error message or toast indicating that both text and photo are required
                }

                true
            }
            // Add more menu items as needed
            else -> super.onOptionsItemSelected(item)
        }
    }
}
