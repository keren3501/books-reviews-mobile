package com.example.booksreviews.view

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
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
import com.bumptech.glide.Glide
import com.example.booksreviews.R
import com.example.booksreviews.databinding.FragmentEditReviewBinding
import com.example.booksreviews.model.Review
import com.example.booksreviews.viewmodel.ReviewsViewModel
import com.example.booksreviews.viewmodel.UserViewModel

class EditReviewFragment : Fragment() {

    private val PICK_IMAGE_REQUEST: Int = 4
    private lateinit var binding: FragmentEditReviewBinding
    private lateinit var reviewsViewModel: ReviewsViewModel
    private lateinit var userViewModel: UserViewModel
    private lateinit var newReview: Review

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

        newReview = Review()

        binding.coverImage.setOnClickListener { openGallery() }

        // if its edit mode
        if (reviewsViewModel.currEditedReviewIndex != -1) {
            newReview = Review(reviewsViewModel.reviews.value?.get(reviewsViewModel.currEditedReviewIndex)!!)

            binding.etReviewText.setText(newReview.reviewText)
            binding.etBookTitle.setText(newReview.bookTitle)
            binding.etAuthorName.setText(newReview.authorName)
            Glide.with(this)
                .load(newReview.bookCoverUri)
                .override(100, 150)
                .error(R.drawable.ic_launcher_foreground)
                .into(binding.coverImage)
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
                if (reviewText.isNotEmpty() && bookTitle.isNotEmpty() && authorName.isNotEmpty()) {
                    newReview.reviewText = reviewText
                    newReview.bookTitle = bookTitle
                    newReview.authorName = authorName

                    if (reviewsViewModel.currEditedReviewIndex !=-1) {
                        reviewsViewModel.updateReviewAtIndex(reviewsViewModel.currEditedReviewIndex, newReview)
                        findNavController().popBackStack()
                    }
                    else {
                        newReview.id = reviewsViewModel.reviews.value!!.size
                        newReview.userId = userViewModel.user.uid

                        // Create an intent to pass back the new review data
                        reviewsViewModel.addReview(newReview)

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

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            val selectedImage = data.data
            // Handle the selected image as needed
            // For example, update the profile image in your ViewModel
            if (selectedImage != null) {
                newReview.bookCoverUri = selectedImage

                Glide.with(this)
                    .load(selectedImage)
                    .error(R.drawable.ic_launcher_foreground)
                    .override(100, 150)
                    .into(binding.coverImage)
            }
        }
    }
}
